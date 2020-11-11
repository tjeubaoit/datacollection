#!/usr/bin/env bash

export JVM_OPTS="-Xms2G -Xmx2G"

base_dir=$(dirname $(readlink -e $0))

$(dirname $0)/run-class.sh \
    --logging.level info \
    --logging.path ${base_dir}/../logs/fetchfbavatar \
    --class com.vcc.bigdata.jobs.fbavatar.FetchFbAvatarUrl "$@"