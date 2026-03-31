
drop table if exists milk_categories;

drop table if exists category;

create table category (
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

alter table milk_categories
    add constraint fk_milks_category_milk_id
        foreign key (milk_id) references milks(id) ON DELETE CASCADE,
    add constraint fk_milks_category_category_id
        foreign key (category_id) references category(id) ON DELETE CASCADE;
