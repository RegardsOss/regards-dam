/* Data Accesses */
create table t_access_group (id int8 not null, public boolean, name varchar(32), internal boolean DEFAULT false, primary key (id));
create table t_access_right (id int8 not null, access_level varchar(30), data_access_level varchar(30), max_score int4, min_score int4, quality_level varchar(30), access_group_id int8, dataset_id int8, data_access_plugin int8, primary key (id));
create table ta_access_group_users (access_group_id int8 not null, users varchar(255));
alter table t_access_group add constraint uk_access_group_name unique (name);
alter table t_access_right add constraint uk_access_right_access_group_id_dataset_id unique (access_group_id, dataset_id);
create sequence seq_access_group start 1 increment 50;
create sequence seq_access_right start 1 increment 50;
alter table t_access_right add constraint fk_access_right_access_group_id foreign key (access_group_id) references t_access_group;
alter table t_access_right add constraint fk_access_right_access_dataset_id foreign key (dataset_id) references t_entity;
alter table t_access_right add constraint fk_access_right_data_access_plugin foreign key (data_access_plugin) references t_plugin_configuration;
alter table ta_access_group_users add constraint fk_access_group_users foreign key (access_group_id) references t_access_group;
