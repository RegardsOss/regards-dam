/* Entities */
create table t_dataset_quotation (dataset_id int8 not null, quotations varchar(255));
create table t_deleted_entity (id int8 not null, creation_date timestamp, deletion_date timestamp, ipId varchar(255) not null, update_date timestamp, primary key (id));
create table t_entity (dtype varchar(10) not null, id int8 not null, creation_date timestamp not null, geometry jsonb, ipId varchar(128) not null, label varchar(128) not null, update_date timestamp, properties jsonb, sipId varchar(255), description_file_content bytea, description text, description_file_type varchar(255), data_model_id int8, licence text, openSearchSubsettingClause text, score int4, sub_setting_clause jsonb, model_id int8 not null, ds_plugin_conf_id int8, primary key (id));
create table t_entity_group (entity_id int8 not null, name varchar(200));
create table t_entity_tag (entity_id int8 not null, value varchar(200));
alter table t_deleted_entity add constraint uk_deleted_entity_ipId unique (ipId);
create index idx_entity_ipId on t_entity (ipId);
alter table t_entity add constraint uk_entity_ipId unique (ipId);
create sequence seq_del_entity start 1 increment 50;
create sequence seq_entity start 1 increment 50;
alter table t_dataset_quotation add constraint fk_dataset_quotation_dataset_id foreign key (dataset_id) references t_entity;
alter table t_entity add constraint fk_entity_model_id foreign key (model_id) references t_model;
alter table t_entity add constraint fk_ds_plugin_conf_id foreign key (ds_plugin_conf_id) references t_plugin_configuration;
alter table t_entity_group add constraint fk_entity_group_entity_id foreign key (entity_id) references t_entity;
alter table t_entity_tag add constraint fk_entity_tag_entity_id foreign key (entity_id) references t_entity;
