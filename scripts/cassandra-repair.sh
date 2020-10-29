#!/usr/bin/env bash

until [ $? -ne 0 ];
do
 apache-cassandra-3.11.0/bin/nodetool repair --trace datacollection1 $1;
 echo "Current other repair runnging. Sleep 60s..."
 sleep 60s
done