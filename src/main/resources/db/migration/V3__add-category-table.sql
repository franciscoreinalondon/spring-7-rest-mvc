
drop table if exists milk_categories;

drop table if exists categories;

create table categories (
                      id binary(16) not null,
                      version integer,
                      description varchar(50) not null,
                      created_at datetime(6) not null,
                      updated_at datetime(6) not null,
                      primary key (id)
) engine=InnoDB;

create table milk_categories (
                      milk_id binary(16) not null,
                      category_id binary(16) not null,
                      primary key (milk_id, category_id)
) engine=InnoDB;

alter table categories
    add constraint uk_categories_description unique (description);

alter table milk_categories
    add constraint fk_milk_categories_milk_id
        foreign key (milk_id) references milks(id) ON DELETE CASCADE,
    add constraint fk_milk_categories_category_id
        foreign key (category_id) references categories(id) ON DELETE CASCADE;
