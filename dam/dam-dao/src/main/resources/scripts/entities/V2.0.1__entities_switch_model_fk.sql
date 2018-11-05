alter table t_entity ADD COLUMN data_model_name varchar(32) DEFAULT null;
alter table t_entity DROP COLUMN data_model_id;
