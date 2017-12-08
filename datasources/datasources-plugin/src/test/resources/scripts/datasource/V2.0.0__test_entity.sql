create table t_test_plugin_data_source (
    id int8 not null,
    altitude integer,
    date date,
    label varchar(255),
    latitude float8,
    longitude float8,
    timestampwithtimezone timestamp,
    timestampwithouttimezone timestamp,
    timewithouttimezone time,
    update boolean
);
create index index_test on t_test_plugin_data_source (altitude);
create sequence seq_test_plugin start 1 increment 50;