#!/bin/sh

export LC_CTYPE=cs_Cz

LIBS=/home/literakl/abc/source/libs
BUILD=/home/literakl/abc/deploy/WEB-INF/classes

JARS=$LIBS/dom4j.jar:$LIBS/log4j.jar:$LIBS/mysql-connector-java-2.0.14-bin.jar
JARS=$JARS:$LIBS/lucene.jar:$LIBS/regexp-1.2.jar:$LIBS/servlet.jar
JARS=$JARS:$LIBS/activation.jar:$LIBS/mail.jar:$LIBS/velocity.jar:$LIBS/commons-collections.jar
export CLASSPATH=$CLASSPATH:$BUILD:$JARS

java -cp $CLASSPATH cz.abclinuxu.utils.search.CreateIndex $1 $2 $3 $4 $5

