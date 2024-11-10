
#接口项目站点路径（目录按照各自配置）
APP_PATH=/home/app

#jar包文件名称
APP_NAME=$APP_PATH/dot-chat.jar

echo "开始停止 Dot-Chat 项目进程 $APP_NAME"
#查询进程，并杀掉当前jar/java程序

pid=`ps -ef|grep $APP_NAME | grep -v grep | awk '{print $2}'`

echo "pid: $pid "

if [ $pid ];then
  echo "pid: $pid"
  kill -9 $pid
  echo "Dot-Chat 项目进程进程终止成功"
else
  echo "未找到对应服务"
fi
