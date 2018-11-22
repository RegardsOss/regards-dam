/* Data Access Group */
ALTER TABLE t_access_right ADD data_access_plugin int8;
alter table t_access_right add constraint fk_access_right_data_access_plugin foreign key (data_access_plugin) references t_plugin_configuration;
