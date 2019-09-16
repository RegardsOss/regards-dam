drop table  t_datasource_ingestion;
create table t_datasource_ingestion (ds_id varchar(36) not null, label varchar(255), last_ingest_date timestamp, next_planned_ingest_date timestamp, duration varchar(20), saved_objects_count int4, stackTrace text, status varchar(32), error_objects_count int4, error_page_nb int4, status_date timestamp, primary key (ds_id));