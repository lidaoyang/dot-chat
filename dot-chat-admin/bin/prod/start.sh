#!/bin/bash

#开始时间 时间戳
startTime=`date +'%Y-%m-%d %H:%M:%S'`
time=`date +'%Y%m%d%H'`
echo "开始时间："$startTime

JAVA_HOME=/usr/local/java/jdk-17.0.11
JAVA=$JAVA_HOME/bin/java

cd /home/app/admin || exit

echo "当前目录："$PWD

#jar包文件名称
APP_NAME=./dot-chat-admin.jar

#创建日志目录
mkdir -p logs
#日志文件名称
LOG_FILE=./logs/console.log

LOG_FILE_BAK=./logs/backup/console.log.bak.$time

#启动环境   # 如果需要配置数据和redis，请在 application-prod.yml中修改, 用jar命令修改即可
APP_OPTION="-server -Xmx512m -Xms512m -Xmn256m -Xss128m -Dspring.profiles.active=prod"


#备份日志
/bin/cp  -rf $LOG_FILE $LOG_FILE_BAK

#删除旧日志
rm -rf $LOG_FILE

# 创建日志文件
touch $LOG_FILE

echo "开始停止 Dot-Chat-Admin 项目进程"
#停止进程
sh ./stop.sh

sleep 2

#判断jar包文件是否存在，如果存在启动jar包，并时时查看启动日志

if test -e $APP_NAME;then
  echo '开始启动此程序...'

# 启动jar包，指向日志文件，2>&1 & 表示打开或指向同一个日志文件  --spring.profiles.active=prod 启动 prod环境

  nohup $JAVA $APP_OPTION -jar $APP_NAME  > $LOG_FILE 2>&1 &
  echo "正在发布中，请稍后......"
  sleep 15s

  #通过检测日志来判断
  while [ -f $LOG_FILE ]
  do
      success=`grep "Started DotChatAdminApplication in " $LOG_FILE`
      if [[ "$success" != "" ]]
      then
          echo "dot-chat Started Success"
          break
      else
          echo "dot-chat Running ......."
          sleep 1s
      fi

#      echo "开始检测启动失败标记"
      fail=`grep "APPLICATION FAILED TO START"|grep "Application run failed" $LOG_FILE`
      if [[ "$fail" != "" ]]
      then
          echo "项目启动失败"
          exit 1 #执行错误命令终止部署
          break
      else
          echo "dot-chat Running ......."
          sleep 1s
      fi

  done

endTime=`date +'%Y-%m-%d %H:%M:%S'`
startSecond=$(date --date="$startTime" +%s);
endSecond=$(date --date="$endTime" +%s);

total=$((endSecond-startSecond))
# shellcheck disable=SC2027
echo "本次运行时间： "$total"s"
# shellcheck disable=SC2086
echo "当前时间："$endTime

fi