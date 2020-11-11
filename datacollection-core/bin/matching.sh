#!/usr/bin/env bash

export JVM_OPTS="-Xms2G -Xmx2G"

base_dir=$(dirname $(readlink -e $0))

while :
do
  $(dirname $0)/run-class.sh \
      --logging.path ${base_dir}/../logs/matching \
      --class com.vcc.bigdata.matching.Matcher "$@"

  if [ $? -eq 0 ]; then
    echo "Finish job at $(date). Sleep 1 minute(s) and restart"
    sleep 1m
  else
    echo "Job failed at $(date). Sleep 10 seconds and try again"
    sleep 10s
  fi
done
