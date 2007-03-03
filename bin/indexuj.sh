#!/bin/sh

export LC_CTYPE=cs_Cz
export JAVA_HOME=/usr/local/java
export PATH=$PATH:$JAVA_HOME/bin

LIBS=@SOURCE_DIR@/libs
BUILD=@DEPLOY_ABCLINUXU@/WEB-INF/classes
ARGS="-Xmx300m -Dlog4j.configuration=log4j.xml -Dabc.config=@DEPLOY_ABCLINUXU@/WEB-INF/conf/systemPrefs.xml"

JARS=$LIBS/dom4j.jar:$LIBS/jaxen.jar:$LIBS/log4j.jar:$LIBS/mysql-connector.jar:$LIBS/proxool.jar
JARS=$JARS:$LIBS/lucene.jar:$LIBS/regexp.jar:$LIBS/servlet.jar:$LIBS/whirlycache.jar:$LIBS/concurrent.jar
JARS=$JARS:$LIBS/activation.jar:$LIBS/mail.jar:$LIBS/commons-collections.jar:$LIBS/commons-logging.jar
export CLASSPATH=$CLASSPATH:$BUILD:$JARS

INDEX_REAL=@DEPLOY_ABCLINUXU@/WEB-INF/index
INDEX_TMP=@DEPLOY_ABCLINUXU@/WEB-INF/index_tmp

#create directories for index
mkdir -p ${INDEX_REAL}
mkdir -p ${INDEX_TMP}

#index data in this directory
java $ARGS -cp $CLASSPATH cz.abclinuxu.utils.search.CreateIndex ${INDEX_TMP}

#remove old index
rm ${INDEX_REAL}/*

#move new index to correct location
mv ${INDEX_TMP}/* ${INDEX_REAL}

#remove temporary directory
rmdir ${INDEX_TMP}
