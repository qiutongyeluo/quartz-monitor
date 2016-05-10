package com.quartz.monitor.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;

import com.quartz.monitor.conf.QuartzConfig;
import com.quartz.monitor.notification.Listener;
import com.quartz.monitor.object.QuartzInstance;
import com.quartz.monitor.object.Scheduler;
import com.quartz.monitor.util.JMXUtil;

/**
 * 处理应用与Quartz的连接（使用JMX）
 * @author guolei
 *
 */
public class QuartzConnectServiceImpl implements QuartzConnectService {
    static Logger log = Logger.getLogger(QuartzConnectServiceImpl.class);

    @Override
    public QuartzInstance initInstance(final QuartzConfig config)
            throws Exception {
        final Map<String, String[]> env = new HashMap<String, String[]>();
        env.put(JMXConnector.CREDENTIALS,
                new String[] { config.getUserName(), config.getPassword() });
        final JMXServiceURL jmxServiceURL = JMXUtil
                .createQuartzInstanceConnection(config);
        final JMXConnector connector = JMXConnectorFactory
                .connect(jmxServiceURL, env);
        final MBeanServerConnection connection = connector
                .getMBeanServerConnection();

        final ObjectName mBName = new ObjectName(
                "quartz:type=QuartzScheduler,*");
        final Set<ObjectName> names = connection.queryNames(mBName, null);
        final QuartzInstance quartzInstance = new QuartzInstance();
        quartzInstance.setMBeanServerConnection(connection);
        quartzInstance.setJmxConnector(connector);

        final List<Scheduler> schList = new ArrayList<Scheduler>();
        for (final ObjectName objectName : names) // for each scheduler.
        {
            final QuartzJMXAdapter jmxAdapter = QuartzJMXAdapterFactory
                    .initQuartzJMXAdapter(objectName, connection);
            quartzInstance.setJmxAdapter(jmxAdapter);

            final Scheduler scheduler = jmxAdapter
                    .getSchedulerByJmx(quartzInstance, objectName);
            schList.add(scheduler);

            // attach listener
            // log.info("added listener " + objectName.getCanonicalName());
            final Listener listener = new Listener();
            listener.setUUID(scheduler.getUuidInstance());
            connection.addNotificationListener(objectName, listener, null,
                    null);
            log.info("added listener " + objectName.getCanonicalName());
            // QuartzInstance.putListener(listener);
        }
        quartzInstance.setSchedulerList(schList);
        return quartzInstance;
    }
}
