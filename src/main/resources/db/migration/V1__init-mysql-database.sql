
drop table if exists customer;

drop table if exists milk;

create table customer (
                      version integer,
                      created_at datetime(6) not null,
                      updated_at datetime(6) not null,
                      id binary(16) not null,
                      name varchar(50) not null,
                      email varchar(120) not null,
                      primary key (id)
) engine=InnoDB;

create table milk (
                      price decimal(12,2) not null,
                      stock integer not null,
                      version integer,
                      created_at datetime(6) not null,
                      updated_at datetime(6) not null,
                      id binary(16) not null,
                      name varchar(50) not null,
                      upc varchar(50) not null,
                      milk_type enum ('WHOLE','SEMI_SKIMMED','SKIMMED','LACTOSE_FREE','A2','ORGANIC_WHOLE','GOAT','HIGH_PROTEIN') not null,
                      primary key (id)
) engine=InnoDB;

alter table customer
    add constraint uk_customer_email unique (email);

alter table milk
    add constraint uk_milk_upc unique (upc);
