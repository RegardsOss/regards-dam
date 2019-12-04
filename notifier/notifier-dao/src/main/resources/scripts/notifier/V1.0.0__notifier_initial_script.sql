create table t_rule (id int8 not null, type varchar(30) not null, rule_plugin_id int8 not null, enable boolean not null, primary key (id));
alter table t_rule add constraint fk_rule_plugin_id foreign key (rule_plugin_id) references t_plugin_configuration(id);
create table t_recipient (id int8 not null, rule_id int8, recipient_plugin_id int8 not null, primary key (id));
alter table t_recipient add constraint fk_rule_id foreign key (rule_id) references t_rule(id);
alter table t_recipient add constraint fk_recipient_plugin_id foreign key (recipient_plugin_id) references t_plugin_configuration(id);

create sequence seq_rule start 1 increment 50;
create sequence seq_recipient start 1 increment 50;

-- tables for notification errors
--create table feature_error (id int8 not null, feature jsonb not null, primary key (id);
--create table recipient_error (id int8 not null, );
