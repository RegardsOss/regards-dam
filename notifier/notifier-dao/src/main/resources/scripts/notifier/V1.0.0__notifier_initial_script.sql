create table t_rule (id int8 not null, rule_plugin_id int8 not null, enable boolean not null, primary key (id));
alter table t_rule add constraint fk_rule_plugin_id foreign key (rule_plugin_id) references t_plugin_configuration(id);
create table t_recipient (id int8 not null, rule_id int8, recipient_plugin_id int8 not null, primary key (id));
alter table t_recipient add constraint fk_rule_id foreign key (rule_id) references t_rule(id);
alter table t_recipient add constraint fk_recipient_plugin_id foreign key (recipient_plugin_id) references t_plugin_configuration(id);

create sequence seq_rule start 1 increment 50;
create sequence seq_recipient start 1 increment 50;

create table t_notification_action (id int8 not null, element jsonb not null, 
action varchar(30) not null, action_date timestamp not null, state varchar(30) not null, primary key (id));

create sequence seq_notification_action start 1 increment 50;

create table t_recipient_error (id int8 not null, recipient_id int8 not null, job_id uuid not null, 
notification_action_id int8 not null, primary key(id));
alter table t_recipient_error add constraint fk_job_id foreign key (job_id) references t_job_info(id);
alter table t_recipient_error add constraint fk_recipient_id foreign key (recipient_id) references t_recipient(id);
alter table t_recipient_error add constraint fk_notification_action_id foreign key (notification_action_id) references t_notification_action(id);

create sequence seq_recipient_error start 1 increment 50;

