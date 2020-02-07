alter table t_entity drop column aip_state;

-- table for request on entities
create table t_entity_request (id int8, urn varchar(255) not null, 
group_id varchar(128) not null, primary key(id));
alter table t_entity_request add constraint uk_group_id unique (group_id);
create sequence seq_entity_request start 1 increment 50;
CREATE INDEX idx_group_id ON t_entity_request (group_id);

