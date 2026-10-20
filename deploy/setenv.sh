#!/bin/sh
# snapshot 应用外置配置目录。
# 数据源账号/口令放在这里，不入库、不进 war。
# 文件权限：/u1/tomcat/conf/snapshot 700，内部 application.properties 600。
CATALINA_OPTS="$CATALINA_OPTS -Dspring.config.additional-location=file:/u1/tomcat/conf/snapshot/"

# 如需覆盖日志或其它 JVM 参数，在此追加，例如：
# CATALINA_OPTS="$CATALINA_OPTS -Dlogging.file.name=/u1/tomcat/logs/snapshot.log"
