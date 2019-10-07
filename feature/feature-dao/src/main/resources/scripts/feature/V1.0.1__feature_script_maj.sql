alter table t_feature_creation_request add column group_id varchar(255);
alter table t_feature_creation_request add column feature_id int8;

alter table t_feature_creation_request add constraint fk_feature_id foreign key (feature_id) references t_feature(id);