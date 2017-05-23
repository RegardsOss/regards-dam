/* Model */
create table t_attribute_model (id int8 not null, alterable boolean, arraysize int4, default_value varchar(255), description text, group_name varchar(32), label varchar(20), name varchar(32) not null, optional boolean, precision int4, type varchar(32) not null, unit varchar(16), fragment_id int8 not null, restriction_id int8, primary key (id));
create table t_attribute_property (id int8 not null, ppty_key varchar(32) not null, ppty_value varchar(255) not null, attribute_id int8, primary key (id));
create table t_fragment (id int8 not null, description text, name varchar(32) not null, version varchar(16), primary key (id));
create table t_model (id int8 not null, description text, name varchar(32) not null, type varchar(10) not null, version varchar(16), primary key (id));
create table t_restriction (dtype varchar(16) not null, id int8 not null, type varchar(32) not null, maxi int8, maxi_excluded boolean, mini int8, mini_excluded boolean, pattern varchar(255), maxf float8, maxf_excluded boolean, minf float8, minf_excluded boolean, primary key (id));
create table ta_enum_restr_accept_values (restriction_id int8 not null, value varchar(255));
create table ta_model_att_att (id int8 not null, mode varchar(20), pos int4, attribute_id int8 not null, compute_conf_id int8, model_id int8 not null, primary key (id));
alter table t_attribute_model add constraint uk_attribute_model_name_fragment_id unique (name, fragment_id);
create index idx_name on t_fragment (name);
alter table t_fragment add constraint uk_fragment_name unique (name);
create index idx_model_name on t_model (name);
alter table t_model add constraint uk_model_name unique (name);
alter table ta_model_att_att add constraint uk_model_att_att_id_model_id unique (attribute_id, model_id);
create sequence seq_att_model start 1 increment 50;
create sequence seq_att_ppty start 1 increment 50;
create sequence seq_fragment start 1 increment 50;
create sequence seq_model start 1 increment 50;
create sequence seq_model_att start 1 increment 50;
create sequence seq_restriction start 1 increment 50;
alter table t_attribute_model add constraint fk_fragment_id foreign key (fragment_id) references t_fragment;
alter table t_attribute_model add constraint fk_restriction_id foreign key (restriction_id) references t_restriction;
alter table t_attribute_property add constraint fk_att_ppty_att foreign key (attribute_id) references t_attribute_model;
alter table ta_enum_restr_accept_values add constraint fk_enum_restr_accept_values_restriction_id foreign key (restriction_id) references t_restriction;
alter table ta_model_att_att add constraint fk_attribute_id foreign key (attribute_id) references t_attribute_model;
alter table ta_model_att_att add constraint fk_plugin_id foreign key (compute_conf_id) references t_plugin_configuration;
alter table ta_model_att_att add constraint fk_model_id foreign key (model_id) references t_model;
