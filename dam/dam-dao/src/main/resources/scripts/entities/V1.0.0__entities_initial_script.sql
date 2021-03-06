/* Entities */
create table t_deleted_entity (id int8 not null, creation_date timestamp, deletion_date timestamp, ipId varchar(255) not null, update_date timestamp, primary key (id));
create table t_entity (dtype varchar(10) not null, id int8 not null, creation_date timestamp not null, ipId varchar(128) not null, update_date timestamp, data_model_name varchar(32) DEFAULT null, sub_setting_clause_as_string text, sub_setting_clause jsonb, model_id int8 not null, ds_plugin_conf_id int8, feature jsonb, wgs84 jsonb, primary key (id));
create table t_entity_group (entity_id int8 not null, name varchar(200));
create table t_entity_tag (entity_id int8 not null, value varchar(200));
create table t_local_storage (id int8 not null, file_checksum varchar(255) not null, entity_id int8 not null, primary key (id));
create table t_entity_request (id int8, urn varchar(255) not null, group_id varchar(128) not null, primary key(id));
create index idx_entity_ipId on t_entity (ipId);
create index idx_group_id ON t_entity_request (group_id);
create sequence seq_del_entity start 1 increment 50;
create sequence seq_description_file start 1 increment 50;
create sequence seq_entity start 1 increment 50;
create sequence documentLS_Sequence start 1 increment 50;
create sequence seq_entity_request start 1 increment 50;
alter table t_entity add constraint fk_entity_model_id foreign key (model_id) references t_model;
alter table t_entity add constraint fk_ds_plugin_conf_id foreign key (ds_plugin_conf_id) references t_plugin_configuration;
alter table t_entity_group add constraint fk_entity_group_entity_id foreign key (entity_id) references t_entity;
alter table t_entity_tag add constraint fk_entity_tag_entity_id foreign key (entity_id) references t_entity;
alter table t_local_storage add constraint uk_t_local_storage_document_file_checksum unique (entity_id, file_checksum);
alter table t_local_storage add constraint fk_ls_entity_id foreign key (entity_id) references t_entity;
alter table t_entity_request add constraint uk_group_id unique (group_id);