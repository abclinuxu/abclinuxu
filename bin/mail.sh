#!/bin/sh

export LC_CTYPE=cs_Cz
BUILD=../build/WEB-INF
TOMCAT=/home/literakl/tomcat/common/lib
export CLASSPATH=$CLASSPATH:$BUILD/classes:$TOMCAT/activation.jar:$TOMCAT/mail.jar

java -cp $CLASSPATH cz.abclinuxu.utils.MailTest
