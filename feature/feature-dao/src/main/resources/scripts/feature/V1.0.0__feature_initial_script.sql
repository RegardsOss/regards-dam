create table t_feature (id int8 not null, feature jsonb, last_update timestamp not null, state varchar(50) not null, primary key (id));
create table t_feature_creation_request (id int8 not null, errors jsonb, request_id varchar(36) not null, request_time timestamp not null, state varchar(50) not null, feature jsonb not null, primary key (id));

create index idx_feature_last_update on t_feature (last_update);
create index idx_feature_creation_request_id on t_feature_creation_request (request_id);
create index idx_feature_creation_request_state on t_feature_creation_request (state);

alter table t_feature_creation_request add constraint uk_feature_creation_request_id unique (request_id);
create sequence seq_feature_creation_request start 1 increment 50;
create sequence seq_feature start 1 increment 50;