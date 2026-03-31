
drop table if exists customers;

drop table if exists milks;

create table customers (
                      version integer,
                      created_at datetime(6) not null,
                      updated_at datetime(6) not null,
                      id binary(16) not null,
                      name varchar(50) not null,
                      email varchar(120) not null,
                      primary key (id)
) engine=InnoDB;

create table milks (
                      price decimal(12,2) not null,
                      stock integer not null,
                      version integer,
                      created_at datetime(6) not null,
                      updated_at datetime(6) not null,
                      id binary(16) not null,
                      name varchar(50) not null,
                      upc varchar(50) not null,
                      milk_type enum ('A2', 'GOAT', 'HIGH_PROTEIN', 'LACTOSE_FREE', 'ORGANIC_WHOLE', 'SEMI_SKIMMED', 'SKIMMED', 'WHOLE') not null,
                      primary key (id)
) engine=InnoDB;

alter table customers
    add constraint uk_customers_email unique (email);

alter table milks
    add constraint uk_milks_upc unique (upc);
