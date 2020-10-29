#!/usr/bin/env bash

export JVM_OPTS="-Xms2G -Xmx2G"

$(dirname $0)/run-extractors.sh \
    com.vcc.bigdata.extract.mongo.FbProfileCommentExtractor \
    com.vcc.bigdata.extract.mongo.FbProfilePostExtractor \
    com.vcc.bigdata.extract.mongo.FbProfileExtractor