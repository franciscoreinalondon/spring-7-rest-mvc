create table milk_order_shipments (
                      id binary(16) not null,
                      version integer,
                      created_at datetime(6) not null,
                      updated_at datetime(6) not null,
                      tracking_number varchar(50) not null,
                      milk_order_id binary(16) not null,
                      primary key (id),

                      constraint uk_milk_order_shipments_milk_order_id
                          unique (milk_order_id),

                      constraint uk_milk_order_shipments_tracking_number
                          unique (tracking_number),

                      constraint chk_milk_order_shipments_tracking_number
                          check (tracking_number regexp '^[A-Za-z0-9]+$')
) engine=InnoDB;

alter table milk_order_shipments
    add constraint fk_milk_order_shipments_milk_order_id
        foreign key (milk_order_id) references milk_orders(id) on delete cascade;
