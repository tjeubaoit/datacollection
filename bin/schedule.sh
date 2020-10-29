#!/bin/bash

h=${1:-01}
m=${2:-15}
today=${3:-0}
last_run=$(date)

[ ${#h} -lt 2 ] && h="0${h}"
[ ${#m} -lt 2 ] && m="0${m}"

echo "Time start job ${h}:${m}"

function run_job() {
 while :
 do
  echo "Job start............."
  python elasticsearch_dump.py
  [ $? -eq 0 ] && break || echo "Job failed. Retry"
 done

 last_run=$(date)
 echo "Job finish at ${last_run}"
}

#echo "First run.............."
#run_job

while :
do
 IFS=':' read -ra arr <<< "$(date +'%d:%H:%M')"

 if [ ${arr[0]} -ne $today ] && [ "${arr[1]}${arr[2]}" -ge "$h$m" ]; then
  run_job
  today="${arr[0]}"
 else
  echo "Last run at ${last_run}"
  echo "Time start job ${h}:${m}. Sleep 5 minutes"
  sleep 5m
 fi
done
