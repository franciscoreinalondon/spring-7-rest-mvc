create table milk_orders (
                      id binary(16) not null,
                      version integer,
                      created_at datetime(6) not null,
                      updated_at datetime(6) not null,
                      customer_ref varchar(50) not null,
                      payment_amount decimal(14,2) not null,
                      milk_order_status enum ('NEW', 'CONFIRMED', 'SHIPPED', 'CANCELLED') not null,
                      customer_id binary(16) not null,
                      primary key (id),

                      constraint uk_milk_orders_customer_ref
                          unique (customer_ref),

                      constraint chk_milk_orders_customer_ref
                          check (customer_ref regexp '^[A-Z0-9-]+$'),

                      constraint chk_milk_orders_payment_amount
                          check (payment_amount >= 0)
) engine=InnoDB;

create table milk_order_lines (
                      id binary(16) not null,
                      version integer,
                      created_at datetime(6) not null,
                      updated_at datetime(6) not null,
                      requested_quantity integer not null,
                      assigned_quantity integer not null,
                      order_line_status enum ('NEW', 'PARTIALLY_ALLOCATED', 'FULLY_ALLOCATED', 'OUT_OF_STOCK') not null,
                      price_at_purchase decimal(12,2) not null,
                      milk_order_id binary(16) not null,
                      milk_id binary(16) not null,
                      primary key (id),

                      constraint chk_milk_order_lines_requested_quantity
                          check (requested_quantity > 0),

                      constraint chk_milk_order_lines_assigned_quantity
                          check (assigned_quantity >= 0),

                      constraint chk_milk_order_lines_price_at_purchase
                          check (price_at_purchase > 0),

                      constraint chk_milk_order_lines_assigned_lte_requested
                          check (assigned_quantity <= requested_quantity)
) engine=InnoDB;

alter table milk_orders
    add constraint fk_milk_orders_customer_id
        foreign key (customer_id) references customers(id);

alter table milk_order_lines
    add constraint fk_milk_order_lines_milk_order_id
        foreign key (milk_order_id) references milk_orders(id) on delete cascade,
    add constraint fk_milk_order_lines_milk_id
        foreign key (milk_id) references milks(id);

create index idx_milk_orders_customer_id on milk_orders(customer_id);
create index idx_milk_order_lines_milk_order_id on milk_order_lines(milk_order_id);
create index idx_milk_order_lines_milk_id on milk_order_lines(milk_id);
