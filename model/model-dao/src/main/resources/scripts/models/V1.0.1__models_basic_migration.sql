alter table ta_model_att_att drop column mode;
alter table t_attribute_model alter column label type varchar(255);
alter table t_attribute_model drop column default_value;