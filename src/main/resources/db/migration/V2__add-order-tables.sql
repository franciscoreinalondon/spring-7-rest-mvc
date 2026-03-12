
drop table if exists milk_order_line;

drop table if exists milk_order;

create table milk_order (
                      version integer,
                      created_at datetime(6) not null,
                      updated_at datetime(6) not null,
                      id binary(16) not null,
                      customer_id binary(16) not null,
                      customer_ref varchar(50) not null unique,
                      primary key (id)
) engine=InnoDB;

create table milk_order_line (
                      version integer,
                      created_at datetime(6) not null,
                      updated_at datetime(6) not null,
                      id binary(16) not null,
                      milk_id binary(16) not null,
                      milk_order_id binary(16) not null,
                      order_quantity integer not null,
                      stock_allocated integer not null,
                      primary key (id)
) engine=InnoDB;

alter table milk_order
    add constraint fk_milk_order_customer_id foreign key (customer_id) references customer(id);

alter table milk_order_line
    add constraint fk_milk_order_line_milkOrder_id foreign key (milk_order_id) references milk_order(id),
    add constraint fk_milk_order_line_milk_id foreign key (milk_id) references milk(id);
