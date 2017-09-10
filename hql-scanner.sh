#!/bin/bash

UBERJAR=${HQLFOOJAR:-target/msync.hql-foo-0.1.0-SNAPSHOT-standalone.jar}

if [ ! -f $UBERJAR ]; then
    lein uberjar
fi

java -jar $UBERJAR $@ 2>/dev/null
