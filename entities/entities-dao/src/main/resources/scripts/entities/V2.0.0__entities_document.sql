alter table t_entity ADD COLUMN files jsonb  DEFAULT null;
create table t_document_file_locally_stored (id int8 not null, file_checksum varchar(255) not null, document_id int8 not null, primary key (id));
alter table t_document_file_locally_stored add constraint fk_documentLS_doc_id foreign key (document_id) references t_entity;
create sequence documentLS_Sequence start 1 increment 50;
