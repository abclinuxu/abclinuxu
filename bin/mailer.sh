#!/bin/sh

#export LC_CTYPE=cs_Cz
export LC_CTYPE=czech

LIBS=@SOURCE_DIR@/libs
BUILD=@DEPLOY_ABCLINUXU@/WEB-INF/classes
ARGS="-Dlog4j.configuration=file:@DEPLOY_ABCLINUXU@/WEB-INF/conf/log4j.xml -Dabc.config=@DEPLOY_ABCLINUXU@/WEB-INF/conf/systemPrefs.xml"

JARS=$LIBS/dom4j.jar:$LIBS/log4j.jar:$LIBS/mysql-connector.jar:$LIBS/proxool.jar
JARS=$JARS:$LIBS/servlet.jar:$LIBS/freemarker.jar
JARS=$JARS:$LIBS/activation.jar:$LIBS/mail.jar:$LIBS/commons-collections.jar
CLASSPATH=$CLASSPATH:$BUILD:$JARS

java $ARGS -cp $CLASSPATH cz.abclinuxu.utils.email.Mailer $1 $2 "$3" "$4"

# mailer.sh ~/users.txt /mail/upgrade-2003-07.ftl admin@abclinuxu.cz "Upozorneni na upgrade"

