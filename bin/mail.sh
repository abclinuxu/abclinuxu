#!/bin/sh

export LC_CTYPE=cs_Cz
BUILD=/home/literakl/ROOT/WEB-INF
TOMCAT=/home/literakl/tomcat/common/lib
LIBS=../libs

JARS=$LIBS/log4j.jar:$LIBS/mysql-connector-java-2.0.14-bin.jar
JARS=$JARS:$LIBS/lucene.jar:$LIBS/regexp-1.2.jar:$LIBS/servlet.jar
JARS=$JARS:$LIBS/dom4j.jar:$TOMCAT/xercesImpl.jar:$TOMCAT/xmlParserAPIs.jar
JARS=$JARS:$LIBS/activation.jar:$LIBS/mail.jar:$LIBS/velocity.jar:$LIBS/commons-collections.jar
export CLASSPATH=$CLASSPATH:$BUILD/classes:$JARS

#JARS=$LIBS/crimson.jar:$LIBS/dom4j.jar:$LIBS/jdbc2_0-stdext.jar:
#$LIBS/log4j.jar:$LIBS/servlet.jar:$LIBS/jaxp.jar:$LIBS/mm.mysql-2.0.7-bin.jar:$LIBS/velocity.jar
#export CLASSPATH=$CLASSPATH:$BUILD/classes:$TOMCAT/activation.jar:$TOMCAT/mail.jar:$JARS

java -cp $CLASSPATH cz.abclinuxu.utils.MailNews $1 $2
