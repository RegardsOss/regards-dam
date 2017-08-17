#!/bin/bash -xe
/wait-for-it.sh -t 0 rs_elasticsearch:9300
/wait-for-it.sh -t 0 rs_rabbitmq:5672
/wait-for-it.sh -t 0 rs_postgres:5432
