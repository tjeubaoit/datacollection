#!/usr/bin/env bash

export JVM_OPTS="-Xms1G -Xmx1G"

base_dir=$(dirname $(readlink -e $0))

$(dirname $0)/run-class.sh \
    --logging.path ${base_dir}/../logs/tools "$@"