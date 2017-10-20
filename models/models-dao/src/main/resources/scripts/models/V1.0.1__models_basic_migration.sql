alter table t_attribute_model drop constraint uk_attribute_model_name_fragment_id;
alter table t_fragment drop constraint uk_fragment_name;
alter table ta_model_att_att drop column mode;
alter table t_model drop constraint uk_model_name;
alter table ta_model_att_att drop constraint uk_model_att_att_id_model_id;
