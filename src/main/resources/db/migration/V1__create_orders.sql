create table if not exists orders (
    id bigserial primary key,
    customer_email varchar(255) not null,
    total_amount numeric(19,2) not null,
    status varchar(32) not null,
    created_at timestamptz not null
);

create index if not exists idx_orders_customer_email on orders(customer_email);
