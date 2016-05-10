package com.quartz.monitor.core;

import java.io.File;
import java.io.FileNotFoundException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.quartz.monitor.conf.QuartzConfig;
import com.quartz.monitor.notification.EventService;
import com.quartz.monitor.object.QuartzInstance;
import com.quartz.monitor.util.XstreamUtil;

public class InitListener implements ServletContextListener {
    private static Logger log = Logger.getLogger(InitListener.class);

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        log.info("load xml to config container");
        final String path = Thread.currentThread().getContextClassLoader()
                .getResource("").getPath() + "quartz-config";
        final File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        } else {
            final File[] fileList = file.listFiles();
            log.info("find " + fileList.length + " configs of quartz config!");
            for (int i = 0; i < fileList.length; i++) {
                if (!fileList[i].isDirectory()
                        && fileList[i].getName().startsWith("quartz-config-")) {
                    try {
                        final QuartzConfig config = XstreamUtil
                                .xml2Object(fileList[i].getAbsolutePath());
                        QuartzInstanceContainer.addQuartzConfig(config);
                        final QuartzConnectService quartzConnectService = new QuartzConnectServiceImpl();
                        final QuartzInstance quartzInstance = quartzConnectService
                                .initInstance(config);
                        QuartzInstanceContainer.addQuartzInstance(
                                config.getUuid(), quartzInstance);
                    } catch (final FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (final Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        final ServletContext sc = event.getServletContext();
        if (sc != null) {
            final String sMaxEvents = sc.getInitParameter("maxevents");
            final int maxEvents = NumberUtils.toInt(sMaxEvents,
                    EventService.DEFAULT_MAX_EVENT_LIST_SIZE);
            sc.setAttribute("maxevents", maxEvents); // expose to other servlets
            EventService.setMaxEventListSize(maxEvents);

            final String sMaxShowEvents = sc.getInitParameter("maxshowevents");
            final int maxShowEvents = NumberUtils.toInt(sMaxShowEvents,
                    EventService.DEFAULT_MAX_SHOW_EVENT_LIST_SIZE);
            sc.setAttribute("maxshowevents", maxShowEvents); // expose to other
                                                             // servlets
            EventService.setMaxShowEventListSize(maxShowEvents);
        }
    }

    @Override
    public void contextDestroyed(final ServletContextEvent arg0) {
        // TODO Auto-generated method stub

    }
}
