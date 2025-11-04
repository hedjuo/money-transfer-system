package com.acme.bank.application.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class IdempotencyGatewayFilter implements GlobalFilter {
    private static final Logger log = LoggerFactory.getLogger(IdempotencyGatewayFilter.class);
    private final ObjectMapper om = new ObjectMapper();
    private final ReactiveStringRedisTemplate redisTemplate;

    public IdempotencyGatewayFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        String method = req.getMethod().toString();
        if (!Set.of("POST").contains(method)) {
            return chain.filter(exchange); // only dedupe mutating methods
        }

        String key = req.getHeaders().getFirst("X-Request-ID");
        if (key == null || key.isBlank()) {
            return chain.filter(exchange); // no key -> no dedupe (or you can reject)
        }

        var reqKey = "idp:" + key + ":req";
        var respKey = "idp:" + key + ":resp";

        return redisTemplate.opsForValue().get(respKey)
                .flatMap(cachedResponse -> returnCachedResponse(exchange, cachedResponse))
                .switchIfEmpty(setIdempotencyKey(reqKey))
                .flatMap(success -> {
                    if (!success) {
                        return pollForResponse(respKey, Duration.ofMillis(500))
                                    .flatMap(cachedResponse -> {
                                        return returnCachedResponse(exchange, cachedResponse);
                                    });
                    }
                    return proceedAndCache(exchange, reqKey, respKey, chain);
                });
    }

    private Mono<CachedResponse> pollForResponse(String respKey, Duration wait) {
        return Mono.defer(() -> redisTemplate.opsForValue().get(respKey))
                   .repeatWhenEmpty(flux -> flux
                       .delayElements(Duration.ofMillis(30))
                       .timeout(wait)
                   )
                   .mapNotNull(cachedResponse -> {
                       try {
                           return om.readValue(cachedResponse, CachedResponse.class);
                       } catch (JsonProcessingException e) {
                           log.error("Error deserializing cached response", e);
                           return null;
                       }
                   })
                   .onErrorResume(TimeoutException.class, e -> Mono.empty());
    }

    private Mono<Boolean> returnCachedResponse(ServerWebExchange exchange, String cachedResponseJson) {
        try {
            var cachedResponse = om.readValue(cachedResponseJson, CachedResponse.class);
            return returnCachedResponse(exchange, cachedResponse).thenReturn(true);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing cached response", e);
            return serverError(exchange).thenReturn(true);
        }
    }
    private Mono<Void> returnCachedResponse(ServerWebExchange exchange, CachedResponse cachedResponse) {
        var resp = exchange.getResponse();
        resp.setStatusCode(HttpStatus.OK);

        cachedResponse.getHeaders()
                      .forEach((headerName, value) -> resp.getHeaders().add(headerName, value));

        byte[] bodyBytes = Optional.ofNullable(cachedResponse.getBody())
                              .map(body -> body.getBytes(StandardCharsets.UTF_8))
                              .orElse(new byte[0]);

        if (bodyBytes.length == 0)
            return resp.setComplete();

        var buf = resp.bufferFactory().wrap(bodyBytes);

        return resp.writeWith(Mono.just(buf));
    }

    private Mono<Void> proceedAndCache(ServerWebExchange exchange, String reqKey, String respKey, GatewayFilterChain chain) {
        return chain.filter(exchange);
    }

    private Mono<Void> serverError(ServerWebExchange ex) {
        ex.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return ex.getResponse().setComplete();
    }

    private Mono<Void> conflict(ServerWebExchange ex) {
        ex.getResponse().setStatusCode(HttpStatus.CONFLICT);
        return ex.getResponse().setComplete();
    }

    private Mono<Boolean> setIdempotencyKey(String key) {
        return redisTemplate.opsForValue()
                .setIfAbsent(key, "1");
    }

    private static class ServerCachedHttpResponseDecorator extends ServerHttpResponseDecorator {
        private static final ObjectMapper om = new ObjectMapper();
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private final ReactiveStringRedisTemplate redisTemplate;
        private final DataBufferFactory dataBufferFactory;
        private final String reqKey;
        private final String respKey;

        public ServerCachedHttpResponseDecorator(
            ReactiveStringRedisTemplate redisTemplate,
            String reqKey,
            String respKey,
            ServerHttpResponse delegate
        ) {
            super(delegate);

            this.dataBufferFactory = delegate.bufferFactory();
            this.redisTemplate = redisTemplate;
            this.reqKey = reqKey;
            this.respKey = respKey;
        }

        @Override
        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
            return super.writeWith(Flux.from(body).map(dataBuf -> {
                byte[] bytes = new byte[dataBuf.readableByteCount()];
                dataBuf.read(bytes);
                DataBufferUtils.release(dataBuf);
                return dataBufferFactory.wrap(bytes);
            }));
        }

        @Override
        public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
            return super.writeAndFlushWith(body);
        }

        @Override
        public Mono<Void> setComplete() {
            Mono<Void> store = Mono.defer(() -> {
                int status = getStatusCode() != null ? getStatusCode().value() : 200;
                Map<String, String> headers = getHeaders().toSingleValueMap();
                String body = baos.toString(StandardCharsets.UTF_8);

                String json;
                try {
                    var cachedResponse = new CachedResponse(status, headers, body);
                    json = om.writeValueAsString(cachedResponse);
                } catch (JsonProcessingException e) {
                    json = "{}";
                }

                return redisTemplate.opsForValue()
                        .set(respKey, json, Duration.ofMinutes(10))
                        .then(redisTemplate.delete(reqKey))
                        .then();
            }).onErrorResume(ex -> redisTemplate.delete(reqKey).then());

            return store.then(super.setComplete());
        }
    }

    private static class CachedResponse {
        private int status;
        private Map<String, String> headers;
        private String body;

        public CachedResponse() {}

        public CachedResponse(int status, Map<String, String> headers, String body) {
            this.status = status;
            this.headers = headers;
            this.body = body;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }
}
