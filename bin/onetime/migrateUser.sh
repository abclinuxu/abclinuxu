#!/bin/sh

export LC_CTYPE=czech

LIBS=/home/literakl/abc/source/libs
BUILD=/home/literakl/abc/deploy/WEB-INF/classes

JARS=$LIBS/dom4j.jar:$LIBS/log4j.jar
JARS=$JARS:$LIBS/mysql-connector.jar:$LIBS/proxool.jar
export CLASSPATH=$CLASSPATH:$BUILD:$JARS

java -cp $CLASSPATH cz.abclinuxu.migrate.UpgradeUser
