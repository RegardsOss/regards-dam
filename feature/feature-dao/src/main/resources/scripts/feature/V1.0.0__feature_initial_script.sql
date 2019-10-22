create table t_feature (id int8 not null, session_owner varchar(128) NOT NULL, session_name varchar(128) NOT NULL, feature jsonb, last_update timestamp not null, state varchar(50) not null, provider_id varchar(100) not null, version numeric not null,primary key (id));
create table t_feature_creation_request (id int8 not null, session_owner varchar(128) not null, session_name varchar(128) not null, storages jsonb, errors jsonb, request_id varchar(36) not null, state varchar(50) not null, feature jsonb not null, registration_date timestamp not null, request_date timestamp not null, step varchar(50) not null, primary key (id));

create index idx_feature_last_update on t_feature (last_update);
create index idx_feature_creation_request_id on t_feature_creation_request (request_id);
create index idx_feature_creation_request_state on t_feature_creation_request (state);
create index idx_feature_session on t_feature (session_owner, session_name);

alter table t_feature_creation_request add constraint uk_feature_creation_request_id unique (request_id);
create sequence seq_feature_creation_request start 1 increment 50;
create sequence seq_feature start 1 increment 50;

alter table t_feature_creation_request add column group_id varchar(255);
alter table t_feature_creation_request add column feature_id int8;

alter table t_feature_creation_request add constraint fk_feature_id foreign key (feature_id) references t_feature;

-- Update requests
create table t_feature_update_request (id int8 not null, errors jsonb, registration_date timestamp not null, request_date timestamp not null, request_id varchar(36) not null, state varchar(50) not null, feature jsonb not null, group_id varchar(255), step varchar(50) not null, urn varchar(132) not null, feature_id int8, primary key (id));
create index idx_feature_update_request_id on t_feature_update_request (request_id);
create index idx_feature_update_request_state on t_feature_update_request (state);
create index idx_feature_update_request_urn on t_feature_update_request (urn);

alter table t_feature_update_request add constraint uk_feature_update_request_id unique (request_id);
create sequence seq_feature_update_request start 1 increment 50;
alter table t_feature_update_request add constraint fk_feature_id foreign key (feature_id) references t_feature;
