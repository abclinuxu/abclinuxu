#!/bin/sh

export LC_CTYPE=cs_Cz
BUILD=/home/literakl/ROOT/WEB-INF
LIBS=${BUILD}/lib
TOMCAT=/home/literakl/tomcat/common/lib

#$LIBS/crimson.jar:$LIBS/junit.jar
JARS=$LIBS/dom4j.jar:$LIBS/log4j.jar:$LIBS/mm.mysql-2.0.7-bin.jar:$LIBS/regexp-1.2.jar
JARS=$JARS:$TOMCAT/xercesImpl.jar:$TOMCAT/xmlParserAPIs.jar

export CLASSPATH=$CLASSPATH:$BUILD/classes:$JARS

java -cp $CLASSPATH $1 $2 $3 $4 $5
