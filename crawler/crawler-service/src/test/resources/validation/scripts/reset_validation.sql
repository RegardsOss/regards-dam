SET search_path TO dam;

DELETE FROM ta_model_att_att;
DELETE FROM t_plugin_parameter;
DELETE FROM t_plugin_configuration;
DELETE FROM t_model;
DELETE FROM t_attribute_model;
DELETE FROM t_fragment;

SELECT pg_catalog.setval('seq_att_model', 1, true);

SELECT pg_catalog.setval('seq_fragment', 1, true);

SELECT pg_catalog.setval('seq_model', 1, true);

SELECT pg_catalog.setval('seq_model_att', 1, true);

SELECT pg_catalog.setval('seq_plugin_conf', 1, true);

SELECT pg_catalog.setval('seq_plugin_parameter', 1, true);

