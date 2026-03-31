
drop table if exists milk_order_line;

drop table if exists milk_order;

create table milk_order (
                      version integer,
                      created_at datetime(6) not null,
                      updated_at datetime(6) not null,
                      id binary(16) not null,
                      customers_id binary(16) not null,
                      customer_ref varchar(50) not null unique,
                      payment_amount decimal(14,2) not null,
                      milk_order_status enum ('NEW', 'CONFIRMED', 'SHIPPED', 'CANCELLED') not null,

                      primary key (id)
) engine=InnoDB;

create table milk_order_line (
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

alter table milk_order
    add constraint fk_milk_order_customers_id
        foreign key (customers_id) references customers(id);

alter table milk_order_line
    add constraint fk_milk_order_line_milk_order_id
        foreign key (milk_order_id) references milk_order(id) ON DELETE CASCADE,
    add constraint fk_milk_order_line_milk_id
        foreign key (milk_id) references milk(id);

create index idx_milk_order_customers_id on milk_order(customers_id);
create index idx_milk_order_line_milk_order_id on milk_order_line(milk_order_id);
create index idx_milk_order_line_milk_id on milk_order_line(milk_id);
