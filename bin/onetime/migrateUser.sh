#!/bin/sh

export LC_CTYPE=czech

LIBS=/home/literakl/abc/source/libs
DEPLOY=/home/www-data/deploy/abclinuxu
ARGS="-Dlog4j.configuration=file:$DEPLOY/WEB-INF/conf/log4j.xml -Dabc.config=$DEPLOY/WEB-INF/conf/systemPrefs.xml"
BUILD=$DEPLOY/WEB-INF/classes

JARS=$LIBS/dom4j.jar:$LIBS/log4j.jar
JARS=$JARS:$LIBS/mysql-connector.jar:$LIBS/proxool.jar
export CLASSPATH=$CLASSPATH:$BUILD:$JARS

java $ARGS -cp $CLASSPATH cz.abclinuxu.migrate.UpgradeUser
