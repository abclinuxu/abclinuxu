#!/bin/sh

#export LC_CTYPE=cs_Cz

LIBS=@SOURCE_DIR@/libs
BUILD=@DEPLOY_ABCLINUXU@/WEB-INF/classes
ARGS="-Xmx400m -Dabc.config=@DEPLOY_ABCLINUXU@/WEB-INF/conf/systemPrefs.xml"

JARS=$LIBS/dom4j.jar:$LIBS/jaxen.jar:$LIBS/log4j.jar:$LIBS/mysql-connector.jar:$LIBS/proxool.jar
JARS=$JARS:$LIBS/lucene.jar:$LIBS/regexp.jar:$LIBS/servlet.jar:$LIBS/whirlycache.jar:$LIBS/concurrent.jar
JARS=$JARS:$LIBS/activation.jar:$LIBS/mail.jar:$LIBS/commons-collections.jar:$LIBS/commons-logging.jar
JARS=$JARS:$LIBS/commons-httpclient.jar:$LIBS/commons-codec.jar:$LIBS/commons-logging.jar
JARS=$JARS:$LIBS/freemarker.jar:$LIBS/htmlcleaner.jar:$LIBS/htmlparser.jar:$LIBS/commons-email.jar
JARS=$JARS:$LIBS/commons-io.jar
export CLASSPATH=$CLASSPATH:$BUILD:$JARS

java $ARGS -cp $CLASSPATH "$1"

