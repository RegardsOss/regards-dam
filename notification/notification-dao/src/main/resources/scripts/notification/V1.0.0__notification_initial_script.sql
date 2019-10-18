create table t_rule (id int8 not null, type varchar(30) not null, plugin_configuration_id int8, enable boolean not null, primary key (id));
alter table t_rule add constraint fk_rule_plugin_configuration_id foreign key (plugin_configuration_id) references t_plugin_configuration(id);
create table t_recipient (id int8 not null, rule_id int8 not null, plugin_configuration_id int8 not null, primary key (id));
alter table t_recipient add constraint fk_rule_id foreign key (rule_id) references t_rule(id);
alter table t_recipient add constraint fk_recipient_plugin_configuration_id foreign key (plugin_configuration_id) references t_plugin_configuration(id);

create sequence seq_rule start 1 increment 50;
create sequence seq_recipient start 1 increment 50;

