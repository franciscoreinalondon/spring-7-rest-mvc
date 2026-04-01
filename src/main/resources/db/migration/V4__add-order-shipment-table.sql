
drop table if exists milk_order_shipments;

create table milk_order_shipments (
                      id binary(16) not null,
                      version integer,
                      milk_order_id binary(16) not null unique,
                      tracking_number varchar(50) not null,
                      created_at datetime(6) not null,
                      updated_at datetime(6) not null,
                      primary key (id)
) engine=InnoDB;

alter table milk_order_shipments
    add constraint fk_milk_orders_shipments_milk_order_id
        foreign key (milk_order_id) references milk_orders(id) ON DELETE CASCADE,
    add constraint uk_order_shipments_tracking_number unique (tracking_number);