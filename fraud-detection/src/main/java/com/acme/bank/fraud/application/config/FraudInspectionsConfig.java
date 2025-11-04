package com.acme.bank.fraud.application.config;

import com.acme.bank.fraud.domain.inspection.FraudInspectionStep;
import com.acme.bank.fraud.domain.inspection.steps.UnexpectedHugeTransfer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class FraudInspectionsConfig {

    @Bean
    public FraudInspectionStep unexpectedHugeTransfer() {
        return new UnexpectedHugeTransfer(10000L);
    }

}
