alter table t_datasource_ingestion alter column status TYPE varchar(32);
alter table t_datasource_ingestion add column error_objects_count int4;
