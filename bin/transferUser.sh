#!/bin/sh

export LC_CTYPE=cs_Cz
BUILD=../build/WEB-INF
export CLASSPATH=$CLASSPATH:$BUILD/classes:$BUILD/lib/crimson.jar:$BUILD/lib/jaxp.jar
export CLASSPATH=$CLASSPATH:$BUILD/lib/mm.mysql-2.0.7-bin.jar:$BUILD/lib/dom4j.jar
export CLASSPATH=$CLASSPATH:$BUILD/lib/jdbc2_0-stdext.jar:$BUILD/lib/log4j.jar

java -cp $CLASSPATH cz.abclinuxu.transfer.TransferUser 1
