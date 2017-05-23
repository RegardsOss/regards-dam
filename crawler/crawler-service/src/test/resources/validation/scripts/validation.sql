SET search_path TO dam;

DELETE FROM ta_model_att_att;
DELETE FROM t_plugin_parameter;
DELETE FROM t_plugin_configuration;
DELETE FROM t_model;
DELETE FROM t_attribute_model;
DELETE FROM t_fragment;

SELECT pg_catalog.setval('seq_att_model', 351, true);

SELECT pg_catalog.setval('seq_fragment', 351, true);

SELECT pg_catalog.setval('seq_model', 351, true);

SELECT pg_catalog.setval('seq_model_att', 351, true);

SELECT pg_catalog.setval('seq_plugin_conf', 351, true);

SELECT pg_catalog.setval('seq_plugin_parameter', 351, true);

INSERT INTO t_fragment VALUES (302, 'Default fragment', 'default', NULL);
INSERT INTO t_fragment VALUES (303, NULL, 'fragment1', NULL);

INSERT INTO t_attribute_model VALUES (302, false, NULL, NULL, NULL, NULL, 'Name of collection', 'name', false, NULL, 'STRING', 'unitless', 302, NULL);
INSERT INTO t_attribute_model VALUES (303, false, NULL, NULL, 'date at which the acquisition of the data has started', NULL, 'data start date', 'start_date', false, NULL, 'DATE_ISO8601', 'unitless', 302, NULL);
INSERT INTO t_attribute_model VALUES (304, false, NULL, NULL, 'date at which the acquisition of the data has ended', NULL, 'data end date', 'end_date', false, NULL, 'DATE_ISO8601', 'unitless', 302, NULL);
INSERT INTO t_attribute_model VALUES (305, false, NULL, NULL, NULL, NULL, 'long sum', 'values_l1_sum', false, NULL, 'LONG', 'unitless', 302, NULL);
INSERT INTO t_attribute_model VALUES (306, false, NULL, NULL, NULL, NULL, 'number of data', 'count', false, NULL, 'LONG', 'unitless', 302, NULL);
INSERT INTO t_attribute_model VALUES (307, false, NULL, NULL, NULL, NULL, 'weight', 'weight', false, NULL, 'INTEGER', 'unitless', 302, NULL);
INSERT INTO t_attribute_model VALUES (308, false, NULL, NULL, NULL, NULL, 'date UTC', 'date', false, NULL, 'DATE_ISO8601', 'unitless', 302, NULL);
INSERT INTO t_attribute_model VALUES (309, false, NULL, NULL, NULL, NULL, 'description', 'description', false, NULL, 'STRING', 'unitless', 302, NULL);
INSERT INTO t_attribute_model VALUES (310, false, NULL, NULL, NULL, NULL, 'long value', 'value_l1', false, NULL, 'LONG', 'unitless', 302, NULL);
INSERT INTO t_attribute_model VALUES (311, false, NULL, NULL, NULL, NULL, 'double value', 'value_d1', false, NULL, 'DOUBLE', 'unitless', 302, NULL);
INSERT INTO t_attribute_model VALUES (312, false, NULL, NULL, NULL, NULL, 'activated', 'activated', false, NULL, 'BOOLEAN', 'unitless', 303, NULL);
INSERT INTO t_attribute_model VALUES (313, false, NULL, NULL, NULL, NULL, 'data state', 'state', false, NULL, 'STRING', 'unitless', 303, NULL);
INSERT INTO t_attribute_model VALUES (314, false, NULL, NULL, NULL, NULL, 'pays', 'country', false, NULL, 'STRING', 'unitless', 302, NULL);
INSERT INTO t_attribute_model VALUES (315, false, NULL, NULL, NULL, NULL, 'superficie', 'area', false, NULL, 'DOUBLE', 'unitless', 302, NULL);

INSERT INTO t_model VALUES (302, 'Validation collection model', 'VALIDATION_COLLECTION_MODEL', 'COLLECTION', '1');
INSERT INTO t_model VALUES (303, 'Validation dataset model', 'VALIDATION_DATASET_MODEL_1', 'DATASET', '1');
INSERT INTO t_model VALUES (304, 'validation data model', 'VALIDATION_DATA_MODEL_1', 'DATA', '1');
INSERT INTO t_model VALUES (305, 'Validation dataset model', 'VALIDATION_DATASET_MODEL_2', 'DATASET', '1');
INSERT INTO t_model VALUES (306, 'validation data model with geometries', 'VALIDATION_DATA_MODEL_2', 'DATA', '1');

INSERT INTO t_plugin_configuration VALUES (302, true, 'fr.cnes.regards.modules.models.domain.IComputedAttribute', 'CountValidationConf', 'fr.cnes.regards.modules.entities.plugin.CountPlugin', 'CountPlugin', 0, '1');
INSERT INTO t_plugin_configuration VALUES (303, true, 'fr.cnes.regards.modules.models.domain.IComputedAttribute', 'MinDateValidationConf', 'fr.cnes.regards.modules.entities.plugin.MinDateComputePlugin', 'MinDateComputePlugin', 0, '1');
INSERT INTO t_plugin_configuration VALUES (304, true, 'fr.cnes.regards.modules.models.domain.IComputedAttribute', 'MaxDateValidationConf', 'fr.cnes.regards.modules.entities.plugin.MaxDateComputePlugin', 'MaxDateComputePlugin', 0, '1');
INSERT INTO t_plugin_configuration VALUES (305, true, 'fr.cnes.regards.modules.models.domain.IComputedAttribute', 'SumLongValidationConf', 'fr.cnes.regards.modules.entities.plugin.LongSumComputePlugin', 'LongSumComputePlugin', 0, '1');

INSERT INTO t_plugin_parameter VALUES (302, false, 'resultAttributeName', 'count', NULL, 302);
INSERT INTO t_plugin_parameter VALUES (303, false, 'resultAttributeName', 'start_date', NULL, 303);
INSERT INTO t_plugin_parameter VALUES (304, false, 'resultAttributeName', 'end_date', NULL, 304);
INSERT INTO t_plugin_parameter VALUES (305, false, 'resultAttributeName', 'values_l1_sum', NULL, 305);

INSERT INTO ta_model_att_att VALUES (302, 'GIVEN', 0, 302, NULL, 302);
INSERT INTO ta_model_att_att VALUES (303, 'GIVEN', 0, 302, NULL, 303);
INSERT INTO ta_model_att_att VALUES (304, 'COMPUTED', 0, 303, 303, 303);
INSERT INTO ta_model_att_att VALUES (305, 'COMPUTED', 0, 304, 304, 303);
INSERT INTO ta_model_att_att VALUES (306, 'COMPUTED', 0, 305, 305, 303);
INSERT INTO ta_model_att_att VALUES (307, 'COMPUTED', 0, 306, 302, 303);
INSERT INTO ta_model_att_att VALUES (308, 'GIVEN', 0, 307, NULL, 304);
INSERT INTO ta_model_att_att VALUES (309, 'GIVEN', 0, 308, NULL, 304);
INSERT INTO ta_model_att_att VALUES (310, 'GIVEN', 0, 309, NULL, 304);
INSERT INTO ta_model_att_att VALUES (311, 'GIVEN', 0, 310, NULL, 304);
INSERT INTO ta_model_att_att VALUES (312, 'GIVEN', 0, 311, NULL, 304);
INSERT INTO ta_model_att_att VALUES (313, 'GIVEN', 0, 312, NULL, 304);
INSERT INTO ta_model_att_att VALUES (314, 'GIVEN', 0, 313, NULL, 304);
INSERT INTO ta_model_att_att VALUES (315, 'COMPUTED', 0, 306, 302, 305);
INSERT INTO ta_model_att_att VALUES (316, 'GIVEN', 0, 314, NULL, 306);
INSERT INTO ta_model_att_att VALUES (317, 'GIVEN', 0, 315, NULL, 306);
