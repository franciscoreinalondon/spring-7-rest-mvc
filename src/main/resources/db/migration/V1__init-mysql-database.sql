create table customers (
                      id binary(16) not null,
                      version integer,
                      created_at datetime(6) not null,
                      updated_at datetime(6) not null,
                      name varchar(50) not null,
                      email varchar(120) not null,
                      primary key (id),

                      constraint uk_customers_email
                          unique (email)
) engine=InnoDB;

create table milks (
                       id binary(16) not null,
                       version integer,
                       created_at datetime(6) not null,
                       updated_at datetime(6) not null,
                       name varchar(50) not null,
                       milk_type enum ('A2', 'GOAT', 'HIGH_PROTEIN', 'LACTOSE_FREE', 'ORGANIC_WHOLE', 'SEMI_SKIMMED', 'SKIMMED', 'WHOLE') not null,
                       upc varchar(50) not null,
                       price decimal(12,2) not null,
                       stock integer not null,
                       primary key (id),

                       constraint uk_milks_upc
                           unique (upc),

                       constraint chk_milks_upc
                           check (upc regexp '^[A-Za-z0-9]+$'),

                       constraint chk_milks_price
                           check (price > 0),

                       constraint chk_milks_stock
                           check (stock >= 0)
) engine=InnoDB;
