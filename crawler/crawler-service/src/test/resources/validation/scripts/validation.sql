SET search_path TO dam;

DELETE FROM ta_model_att_att;
DELETE FROM t_plugin_parameter;
DELETE FROM t_plugin_configuration;
DELETE FROM t_model;
DELETE FROM t_attribute_model;
DELETE FROM t_fragment;

ALTER SEQUENCE seq_fragment RESTART WITH 604;
ALTER SEQUENCE seq_att_model RESTART WITH 616;
ALTER SEQUENCE seq_model RESTART WITH 607;
ALTER SEQUENCE seq_plugin_conf RESTART WITH 606;
ALTER SEQUENCE seq_plugin_parameter RESTART WITH 606;
ALTER SEQUENCE seq_model_att RESTART WITH 618;

INSERT INTO t_fragment VALUES (602, 'Default fragment', 'default', NULL);
INSERT INTO t_fragment VALUES (603, NULL, 'fragment1', NULL);

INSERT INTO t_attribute_model VALUES (602, false, NULL, NULL, NULL, false, NULL, 'name', false, NULL, false, 'STRING', 'unitless', 602, NULL);
INSERT INTO t_attribute_model VALUES (603, false, NULL, NULL, 'date at which the acquisition of the data has started', false, NULL, 'start_date', false, NULL, false, 'DATE_ISO8601', 'unitless', 602, NULL);
INSERT INTO t_attribute_model VALUES (604, false, NULL, NULL, 'date at which the acquisition of the data has ended', false, NULL, 'end_date', false, NULL, false, 'DATE_ISO8601', 'unitless', 602, NULL);
INSERT INTO t_attribute_model VALUES (605, false, NULL, NULL, NULL, false, NULL, 'values_l1_sum', false, NULL, false, 'LONG', 'unitless', 602, NULL);
INSERT INTO t_attribute_model VALUES (606, false, NULL, NULL, NULL, false, NULL, 'count', false, NULL, false, 'LONG', 'unitless', 602, NULL);
INSERT INTO t_attribute_model VALUES (607, false, NULL, NULL, NULL, false, NULL, 'weight', false, NULL, false, 'INTEGER', 'unitless', 602, NULL);
INSERT INTO t_attribute_model VALUES (608, false, NULL, NULL, NULL, false, NULL, 'date', false, NULL, false, 'DATE_ISO8601', 'unitless', 602, NULL);
INSERT INTO t_attribute_model VALUES (609, false, NULL, NULL, NULL, false, NULL, 'description', false, NULL, false, 'STRING', 'unitless', 602, NULL);
INSERT INTO t_attribute_model VALUES (610, false, NULL, NULL, NULL, false, NULL, 'value_l1', false, NULL, false, 'LONG', 'unitless', 602, NULL);
INSERT INTO t_attribute_model VALUES (611, false, NULL, NULL, NULL, false, NULL, 'value_d1', false, NULL, false, 'DOUBLE', 'unitless', 602, NULL);
INSERT INTO t_attribute_model VALUES (612, false, NULL, NULL, NULL, false, NULL, 'activated', false, NULL, false, 'BOOLEAN', 'unitless', 603, NULL);
INSERT INTO t_attribute_model VALUES (613, false, NULL, NULL, NULL, false, NULL, 'state', false, NULL, false, 'STRING', 'unitless', 603, NULL);
INSERT INTO t_attribute_model VALUES (614, false, NULL, NULL, NULL, false, NULL, 'country', false, NULL, false, 'STRING', 'unitless', 602, NULL);
INSERT INTO t_attribute_model VALUES (615, false, NULL, NULL, NULL, false, NULL, 'area', false, NULL, false, 'DOUBLE', 'unitless', 602, NULL);

INSERT INTO t_model VALUES (602, 'Validation collection model', 'VALIDATION_COLLECTION_MODEL', 'COLLECTION', '1');
INSERT INTO t_model VALUES (603, 'Validation dataset model', 'VALIDATION_DATASET_MODEL_1', 'DATASET', '1');
INSERT INTO t_model VALUES (604, 'validation data model', 'VALIDATION_DATA_MODEL_1', 'DATA', '1');
INSERT INTO t_model VALUES (605, 'Validation dataset model', 'VALIDATION_DATASET_MODEL_2', 'DATASET', '1');
INSERT INTO t_model VALUES (606, 'validation data model with geometries', 'VALIDATION_DATA_MODEL_2', 'DATA', '1');

INSERT INTO t_plugin_configuration VALUES (602, true, 'fr.cnes.regards.modules.models.domain.IComputedAttribute', 'CountValidationConf', 'fr.cnes.regards.modules.entities.plugin.CountPlugin', 'CountPlugin', 0, '1');
INSERT INTO t_plugin_configuration VALUES (603, true, 'fr.cnes.regards.modules.models.domain.IComputedAttribute', 'MinDateValidationConf', 'fr.cnes.regards.modules.entities.plugin.MinDateComputePlugin', 'MinDateComputePlugin', 0, '1');
INSERT INTO t_plugin_configuration VALUES (604, true, 'fr.cnes.regards.modules.models.domain.IComputedAttribute', 'MaxDateValidationConf', 'fr.cnes.regards.modules.entities.plugin.MaxDateComputePlugin', 'MaxDateComputePlugin', 0, '1');
INSERT INTO t_plugin_configuration VALUES (605, true, 'fr.cnes.regards.modules.models.domain.IComputedAttribute', 'SumLongValidationConf', 'fr.cnes.regards.modules.entities.plugin.LongSumComputePlugin', 'LongSumComputePlugin', 0, '1');

INSERT INTO t_plugin_parameter VALUES (602, false, 'resultAttributeName', 'count', NULL, 602);
INSERT INTO t_plugin_parameter VALUES (603, false, 'resultAttributeName', 'start_date', NULL, 603);
INSERT INTO t_plugin_parameter VALUES (604, false, 'resultAttributeName', 'end_date', NULL, 604);
INSERT INTO t_plugin_parameter VALUES (605, false, 'resultAttributeName', 'values_l1_sum', NULL, 605);

INSERT INTO ta_model_att_att VALUES (602, 'GIVEN', 0, 602, NULL, 602);
INSERT INTO ta_model_att_att VALUES (603, 'GIVEN', 0, 602, NULL, 603);
INSERT INTO ta_model_att_att VALUES (604, 'COMPUTED', 0, 603, 603, 603);
INSERT INTO ta_model_att_att VALUES (605, 'COMPUTED', 0, 604, 604, 603);
INSERT INTO ta_model_att_att VALUES (606, 'COMPUTED', 0, 605, 605, 603);
INSERT INTO ta_model_att_att VALUES (607, 'COMPUTED', 0, 606, 602, 603);
INSERT INTO ta_model_att_att VALUES (608, 'GIVEN', 0, 607, NULL, 604);
INSERT INTO ta_model_att_att VALUES (609, 'GIVEN', 0, 608, NULL, 604);
INSERT INTO ta_model_att_att VALUES (610, 'GIVEN', 0, 609, NULL, 604);
INSERT INTO ta_model_att_att VALUES (611, 'GIVEN', 0, 610, NULL, 604);
INSERT INTO ta_model_att_att VALUES (612, 'GIVEN', 0, 611, NULL, 604);
INSERT INTO ta_model_att_att VALUES (613, 'GIVEN', 0, 612, NULL, 604);
INSERT INTO ta_model_att_att VALUES (614, 'GIVEN', 0, 613, NULL, 604);
INSERT INTO ta_model_att_att VALUES (615, 'COMPUTED', 0, 606, 602, 605);
INSERT INTO ta_model_att_att VALUES (616, 'GIVEN', 0, 614, NULL, 606);
INSERT INTO ta_model_att_att VALUES (617, 'GIVEN', 0, 615, NULL, 606);