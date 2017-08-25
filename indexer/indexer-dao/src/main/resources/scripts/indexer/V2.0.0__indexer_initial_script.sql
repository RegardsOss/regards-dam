/* Indexer */
create table t_data_file (id int8 not null, checksum varchar(255), data_file_type int4, checksum_digest_algorithm varchar(255), data_file_ref bytea, size int8, data_file_mine_type varchar(255), primary key (id));
