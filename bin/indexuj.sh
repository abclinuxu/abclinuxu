#!/bin/sh

export LC_CTYPE=cs_Cz

LIBS=@SOURCE_DIR@/libs
BUILD=@DEPLOY_ABCLINUXU@/WEB-INF/classes
ARGS="-Xmx200m -Dlog4j.configuration=file:@DEPLOY_ABCLINUXU@/WEB-INF/conf/log4j.xml -Dabc.config=@DEPLOY_ABCLINUXU@/WEB-INF/conf/systemPrefs.xml"

JARS=$LIBS/dom4j.jar:$LIBS/log4j.jar:$LIBS/mysql-connector.jar:$LIBS/proxool.jar
JARS=$JARS:$LIBS/lucene.jar:$LIBS/regexp.jar:$LIBS/servlet.jar
JARS=$JARS:$LIBS/activation.jar:$LIBS/mail.jar:$LIBS/velocity.jar:$LIBS/commons-collections.jar
export CLASSPATH=$CLASSPATH:$BUILD:$JARS

java $ARGS -cp $CLASSPATH cz.abclinuxu.utils.search.CreateIndex $1 $2 $3 $4 $5

