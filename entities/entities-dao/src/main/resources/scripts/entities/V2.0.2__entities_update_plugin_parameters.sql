-- This script only update plugin parameters named "attribue file size" to "attribute file size"
-- (there is no datasource-dao module this why this script is here)
update t_plugin_parameter set name = 'attribute file size' where name = 'attribue file size';