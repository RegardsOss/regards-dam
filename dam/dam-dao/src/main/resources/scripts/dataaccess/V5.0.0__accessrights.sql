/* Data Access Group */
ALTER TABLE t_access_right DROP CONSTRAINT fk_access_right_plugin_conf;
ALTER TABLE t_access_right DROP COLUMN plugin_conf_id;
