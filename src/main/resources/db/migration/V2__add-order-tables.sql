
drop table if exists milk_order_lines;

drop table if exists milk_orders;

create table milk_orders (
                      version integer,
                      created_at datetime(6) not null,
                      updated_at datetime(6) not null,
                      id binary(16) not null,
                      customer_id binary(16) not null,
                      customer_ref varchar(50) not null unique,
                      payment_amount decimal(14,2) not null,
                      milk_order_status enum ('NEW', 'CONFIRMED', 'SHIPPED', 'CANCELLED') not null,
                      primary key (id)
) engine=InnoDB;

create table milk_order_lines (
                      version integer,
                      created_at datetime(6) not null,
                      updated_at datetime(6) not null,
                      id binary(16) not null,
                      milk_id binary(16) not null,
                      milk_order_id binary(16) not null,
                      requested_quantity integer not null,
                      assigned_quantity integer not null,
                      order_line_status enum ('NEW', 'ALLOCATED', 'OUT_OF_STOCK') not null,
                      price_at_purchase decimal(12,2) not null,
                      primary key (id)
) engine=InnoDB;

alter table milk_orders
    add constraint fk_milk_orders_customer_id
        foreign key (customer_id) references customers(id);

alter table milk_order_lines
    add constraint fk_milk_order_lines_milk_order_id
        foreign key (milk_order_id) references milk_orders(id) ON DELETE CASCADE,
    add constraint fk_milk_order_lines_milks_id
        foreign key (milk_id) references milks(id);

create index idx_milk_orders_customer_id on milk_orders(customer_id);
create index idx_milk_order_lines_milk_order_id on milk_order_lines(milk_order_id);
create index idx_milk_order_lines_milks_id on milk_order_lines(milk_id);
