#!/bin/sh

#export LC_CTYPE=cs_Cz
export LC_CTYPE=czech
BUILD=/home/literakl/ROOT/WEB-INF
LIBS=${BUILD}/lib
TOMCAT=/home/literakl/tomcat/common/lib

JARS=$LIBS/dom4j.jar:$LIBS/log4j.jar:$LIBS/mysql-connector-java-2.0.14-bin.jar
JARS=$JARS:$LIBS/lucene.jar:$LIBS/regexp-1.2.jar:$LIBS/velocity.jar
JARS=$JARS:$TOMCAT/xercesImpl.jar:$TOMCAT/xmlParserAPIs.jar:$TOMCAT/servlet.jar


export CLASSPATH=$CLASSPATH:$BUILD/classes:$JARS

java -Xmx94m -cp $CLASSPATH $1 $2 $3 $4 $5
