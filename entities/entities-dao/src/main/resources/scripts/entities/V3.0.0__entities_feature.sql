-- This script refactor entities to add a public feature field dedicated to business search API and public consultation
alter table t_entity add column feature jsonb;
CREATE INDEX idxgin ON t_entity USING GIN (feature);
-- Drop useless columns due to feature refactoring
alter table t_entity drop column geometry;
alter table t_entity drop column label;
alter table t_entity drop column properties;
alter table t_entity drop column sipId;
alter table t_entity drop column licence;
alter table t_entity drop column score;
alter table t_entity drop column description_file_id;

