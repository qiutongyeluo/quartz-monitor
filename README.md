quartz-monitor
==============

quartz-monitor 是一个基于DWZ的Quartz管理工具，可以实时动态的管理Job和trigger，具体功能主要包括：

1. 配置管理
针对不同环境的Quartz，需要有一个统一的入口去管理这些配置，来满足对不同环境的任务的管理。
2. Job管理
提供对Job的管理和维护功能。Monitor提供对Job的基本管理，包括对其状态、执行时间、基本信息的管理以及提供基于Job的基本操作。
3. Trigger管理
提供对trigger的管理和维护功能。可以查看某个job的trigger信息，并添加和修改trigger。
4. Cron Expression校验
Cron Expression虽然简单却非常容易写错，所以我们提供了对其的校验功能。

配置方法：

1）配置quartz支持JMX

在需要管理的应用的quartz.properties中加入配置：
```xml
org.quartz.scheduler.jmx.export = true
```

2）配置应用容器支持JMX

比如我使用的是TOMCAT，并且在Linux上，我需要往catalina.sh中加入：
```xml
JAVA_OPTS='-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=2911 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dorg.quartz.scheduler.jmx.export=true'
```
或者windows下，在catalina.bat下面添加：
set CATALINA_OPTS=%CATALINA_OPTS% -Dcom.sun.management.jmxremote
set CATALINA_OPTS=%CATALINA_OPTS% -Dcom.sun.management.jmxremote.port=1099
set CATALINA_OPTS=%CATALINA_OPTS% -Dcom.sun.management.jmxremote.ssl=false
set CATALINA_OPTS=%CATALINA_OPTS% -Dcom.sun.management.jmxremote.authenticate=false
或者linux下，在catalina.sh加入：
export JAVA_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=7044 -Dcom.sun.management.jmxremote.rmi.port=7044 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"

注： 加入catalina.sh的位置有以下要求，否则会在./shutdown.sh时报错，因为shutdown时也会调用catalina.sh。
必须放到以下语句下面：
① elif [ "$1" = "run" ]; then        // ./startup.sh时会调用
② elif [ "$1" = "start" ]; then      // ./catalina.sh run时会调用


3）配置Quartz-Monitor

将quartz-monitor放入tomcat，配置好远程quartz的jmx信息，如jmx端口和ip，即可使用。
