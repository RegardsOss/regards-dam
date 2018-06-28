-- This script refactor entities to add a public feature field dedicated to business search API and public consultation
alter table t_entity add column feature jsonb;
alter table t_entity add column wgs84 jsonb;
CREATE INDEX idxgin ON t_entity USING GIN (feature);
-- Drop useless columns due to feature refactoring
alter table t_entity drop column geometry;
alter table t_entity drop column label;
alter table t_entity drop column properties;
alter table t_entity drop column sipId;
alter table t_entity drop column licence;
alter table t_entity drop column score;
alter table t_entity drop column description_file_id;
alter table t_entity drop column files;

-- TODO
-- drop table t_description_file;

-- Generalize local storage to all entities
alter table t_document_file_locally_stored rename to t_local_storage;
alter table t_local_storage drop constraint fk_documentLS_doc_id;
alter table t_local_storage rename column document_id to entity_id;
alter table t_local_storage add constraint uk_t_local_storage_document_file_checksum unique (entity_id, file_checksum);
alter table t_local_storage add constraint fk_ls_entity_id foreign key (entity_id) references t_entity;



