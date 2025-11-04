create table if not exists client
(
    id         bigserial  not null constraint client_pk primary key,
    name       text       not null
);

create table if not exists account
(
    id         bigserial  not null constraint account_pk primary key,
    client_id  bigint     not null,
    balance    bigint     default 0 not null,
    version    integer    default 0 not null
);
create index if not exists account_client_id_index on account (client_id);

create table if not exists transaction
(
    id                    bigserial         not null constraint transaction_pk primary key,
    request_id            uuid              not null,
    sender_id             bigint            not null,
    receiver_id           bigint            not null,
    amount                bigint  default 0 not null,
    new_sender_balance    bigint  default 0 not null,
    new_receiver_balance  bigint  default 0 not null,
    status                text,
    reason                text,
    version               integer default 0 not null,
    created_at            timestamptz default now()
);
create index if not exists transaction_request_id_index on transaction(request_id);
create index if not exists transaction_sender_id_index on transaction(sender_id);
create index if not exists transaction_receiver_id_index on transaction(receiver_id);

create table if not exists transaction_event
(
    id                    bigserial         not null constraint transaction_event_pk primary key,
    request_id            uuid              not null,
    sender_id             bigint            not null,
    receiver_id           bigint            not null,
    amount                bigint  default 0 not null,
    new_sender_balance    bigint  default 0 not null,
    new_receiver_balance  bigint  default 0 not null,
    status                text,
    reason                text,
    version               integer default 0 not null,
    created_at            timestamptz default now()
);