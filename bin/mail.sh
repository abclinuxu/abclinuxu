#!/bin/sh

export LC_CTYPE=cs_Cz
BUILD=../build/WEB-INF
TOMCAT=/home/literakl/tomcat/common/lib
LIBS=../libs
JARS=$LIBS/crimson.jar:$LIBS/dom4j.jar:$LIBS/jdbc2_0-stdext.jar:$LIBS/log4j.jar:$LIBS/servlet.jar:$LIBS/jaxp.jar:$LIBS/mm.mysql-2.0.7-bin.jar:$LIBS/velocity.jar
export CLASSPATH=$CLASSPATH:$BUILD/classes:$TOMCAT/activation.jar:$TOMCAT/mail.jar:$JARS

java -cp $CLASSPATH cz.abclinuxu.utils.Mailer $1 $2
