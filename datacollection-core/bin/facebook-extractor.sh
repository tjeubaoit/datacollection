#!/usr/bin/env bash

export JVM_OPTS="-Xms4G -Xmx4G"

$(dirname $0)/run-extractors.sh \
    com.vcc.bigdata.extract.mongo.FbPageCommentExtractor \
    com.vcc.bigdata.extract.mongo.FbPagePostExtractor \
    com.vcc.bigdata.extract.mongo.FbGroupCommentExtractor \
    com.vcc.bigdata.extract.mongo.FbGroupPostExtractor