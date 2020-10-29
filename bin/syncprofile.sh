#!/usr/bin/env bash

export JVM_OPTS="-Xms2G -Xmx2G"

base_dir=$(dirname $(readlink -e $0))

$(dirname $0)/run-class.sh \
    --logging.path ${base_dir}/../logs/syncprofile \
    --class com.vcc.bigdata.jobs.syncprofile.SyncProfileToEs "$@"
