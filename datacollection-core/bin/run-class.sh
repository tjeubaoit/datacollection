#!/usr/bin/env bash

base_dir=$(dirname $(readlink -e $0))/../

TMP_DIR=${base_dir}/tmp

LOG4J_OPTS="file:"${base_dir}/config/log4j.xml
HBASE_OPTS=${base_dir}/config/hbase-site.xml
APP_CONFIG_OPTS=${base_dir}/config/server.properties
HYSTRIX_CONFIG_OPTS="file://"${base_dir}/config/hystrix.properties

KAFKA_PRODUCER_OPTS=${base_dir}/config/kafka-producer.properties
KAFKA_CONSUMER_OPTS=${base_dir}/config/kafka-consumer.properties

if [ -z "$JVM_OPTS" ]; then
  JVM_OPTS="-Xms512M -Xmx512M"
fi
JVM_OPTS="${JVM_OPTS} -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:InitiatingHeapOccupancyPercent=70"

CLASSPATH=./*
[ -d "target" ] && CLASSPATH=${CLASSPATH}:"${base_dir}/target/*"
[ -d "target/lib" ] && CLASSPATH=${CLASSPATH}:"${base_dir}target/lib/*"
[ -d "lib" ] && CLASSPATH=${CLASSPATH}:"${base_dir}lib/*"

java -Dlog4j.configuration=${LOG4J_OPTS} \
    -Dkafka.producer.conf=${KAFKA_PRODUCER_OPTS} \
    -Dkafka.consumer.conf=${KAFKA_CONSUMER_OPTS} \
    -Dhbase.conf=${HBASE_OPTS} \
    -Darchaius.configurationSource.additionalUrls=${HYSTRIX_CONFIG_OPTS} \
    -Djava.io.tmpdir=${TMP_DIR} \
    -Dapp.conf=${APP_CONFIG_OPTS} ${JVM_OPTS} -cp ${CLASSPATH} \
     com.vcc.bigdata.Main "$@"