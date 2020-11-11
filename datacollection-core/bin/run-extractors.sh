#!/usr/bin/env bash

export JVM_OPTS="-Xms2G -Xmx2G"

while :
do
  $(dirname $0)/run-class.sh --class com.vcc.bigdata.extract.ExtractorLauncher "$@"
  echo "Job failed at $(date). Sleep 30 seconds and try again"
  sleep 30s
done