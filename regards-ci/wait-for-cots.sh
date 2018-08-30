#!/bin/bash -xe
/wait-for-it.sh -t 0 rs-elasticsearch:9300
/wait-for-it.sh -t 0 rs-rabbitmq:5672
/wait-for-it.sh -t 0 rs-postgres:5432
