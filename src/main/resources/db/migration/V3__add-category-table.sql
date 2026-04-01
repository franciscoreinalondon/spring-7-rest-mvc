create table categories (
                      id binary(16) not null,
                      version integer,
                      created_at datetime(6) not null,
                      updated_at datetime(6) not null,
                      description varchar(50) not null,
                      primary key (id),

                      constraint uk_categories_description
                          unique (description)
) engine=InnoDB;

create table milk_categories (
                      milk_id binary(16) not null,
                      category_id binary(16) not null,
                      primary key (milk_id, category_id)
) engine=InnoDB;

alter table milk_categories
    add constraint fk_milk_categories_milk_id
        foreign key (milk_id) references milks(id) on delete cascade,
    add constraint fk_milk_categories_category_id
        foreign key (category_id) references categories(id) on delete cascade;

create index idx_milk_categories_category_id on milk_categories(category_id);
