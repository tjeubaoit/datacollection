while :
do
 rm /storage/datacollection/datacollection/logs/*.log.*
 last_run=$(date)
 echo "Job finish at ${last_run}"
 sleep 168h
done



