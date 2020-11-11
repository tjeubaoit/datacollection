#!/usr/bin/env bash

export JVM_OPTS="-Xms2G -Xmx2G"

$(dirname $0)/run-extractors.sh \
    com.vcc.bigdata.extract.mongo.ForumArticleExtractor \
    com.vcc.bigdata.extract.mongo.ForumCommentExtractor \
    com.vcc.bigdata.extract.sql.ZambaExtractor