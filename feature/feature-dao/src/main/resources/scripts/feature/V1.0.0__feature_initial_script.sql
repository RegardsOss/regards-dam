create table t_feature (id int8 not null, urn varchar(132), previous_version_urn varchar(132), session_owner varchar(128) NOT NULL,
session_name varchar(128) NOT NULL, feature jsonb, last_update timestamp not null, provider_id varchar(100) not null,
version numeric not null, creation_date timestamp not null, primary key (id));
create table t_feature_creation_request (id int8 not null, provider_id varchar(100) not null, session_owner varchar(128) not null, 
session_name varchar(128) not null, storages jsonb, errors jsonb, request_id varchar(36) not null, state varchar(50) not null, 
feature jsonb not null, registration_date timestamp not null, request_date timestamp not null, step varchar(50) not null, 
priority numeric not null, group_id varchar(255), feature_id int8, override_previous_version boolean, primary key (id));

ALTER TABLE t_feature ADD CONSTRAINT uk_feature_urn UNIQUE(urn);

create index idx_feature_last_update on t_feature (last_update);
create index idx_feature_creation_request_id on t_feature_creation_request (request_id);
create index idx_feature_creation_request_state on t_feature_creation_request (state);
create index idx_feature_step_registration_priority on t_feature_creation_request (step, registration_date, priority);
create index idx_feature_creation_group_id on t_feature_creation_request (group_id);
create index idx_feature_entity_provider_id on t_feature (provider_id);

create index idx_feature_urn on t_feature (urn);
create index idx_feature_session on t_feature (session_owner, session_name);

alter table t_feature_creation_request add constraint uk_feature_creation_request_id unique (request_id);
create sequence seq_feature_creation_request start 1 increment 50;
create sequence seq_feature start 1 increment 50;

alter table t_feature_creation_request add constraint fk_feature_id foreign key (feature_id) references t_feature;

-- Update requests
create table t_feature_update_request (id int8 not null, provider_id varchar(100) not null, errors jsonb, 
registration_date timestamp not null, request_date timestamp not null, request_id varchar(36) not null, 
state varchar(50) not null, feature jsonb not null, group_id varchar(255), 
step varchar(50) not null, urn varchar(132) not null, feature_id int8, priority numeric not null, primary key (id));
create index idx_feature_update_request_id on t_feature_update_request (request_id);
create index idx_feature_update_request_state on t_feature_update_request (state);
create index idx_feature_update_request_urn on t_feature_update_request (urn);
create index idx_feature_update_step_registration_priority on t_feature_update_request (step, registration_date, priority);

alter table t_feature_update_request add constraint uk_feature_update_request_id unique (request_id);
create sequence seq_feature_update_request start 1 increment 50;
alter table t_feature_update_request add constraint fk_feature_id foreign key (feature_id) references t_feature;

-- Delete requests
create table t_feature_deletion_request (id int8 not null, errors jsonb, registration_date timestamp not null, 
request_date timestamp not null, request_id varchar(36) not null, state varchar(50) not null, group_id varchar(255), 
step varchar(50) not null, urn varchar(132) not null, priority numeric not null, primary key (id));
create index idx_feature_deletion_request_id on t_feature_deletion_request (request_id);
create index idx_feature_deletion_request_state on t_feature_deletion_request (state);
create index idx_feature_deletion_request_urn on t_feature_deletion_request (urn);
create index idx_feature_deletion_step_registration_priority on t_feature_deletion_request (step, registration_date, priority);

alter table t_feature_deletion_request add constraint uk_feature_deletion_request_id unique (request_id);
create sequence seq_feature_deletion_request start 1 increment 50;


-- Notification request
create table t_notification_request (id int8 not null, registration_date timestamp not null, 
request_date timestamp not null, request_id varchar(36) not null, step varchar(50) not null, 
urn varchar(132) not null, priority numeric not null, primary key (id));

create index idx_notification_request_urn on t_notification_request (urn);
alter table t_notification_request add constraint uk_notification_request_id unique (urn);

-- Reference request
create table t_feature_reference_request (id int8 not null, session_owner varchar(128) not null, 
session_name varchar(128) not null, storages jsonb, errors jsonb, request_id varchar(36) not null, state varchar(50) not null, 
registration_date timestamp not null, request_date timestamp not null, step varchar(50) not null, 
priority numeric not null, location varchar(128) not null, plugin_business_id varchar(128) not null, primary key (id));
alter table t_feature_reference_request add constraint uk_feature_reference_request_id unique (request_id);
create sequence seq_feature_reference_request start 1 increment 50;


