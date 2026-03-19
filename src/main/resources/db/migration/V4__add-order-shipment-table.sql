
drop table if exists order_shipment;

create table milk_order_shipment (
                      id binary(16) not null,
                      version integer,
                      milk_order_id binary(16) not null unique,
                      tracking_number varchar(50) not null,
                      created_at datetime(6) not null,
                      updated_at datetime(6) not null,
                      primary key (id)
) engine=InnoDB;

alter table milk_order_shipment
    add constraint fk_milk_order_shipment_milk_order_id
        foreign key (milk_order_id) references milk_order(id) ON DELETE CASCADE;
