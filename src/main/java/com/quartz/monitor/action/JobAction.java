package com.quartz.monitor.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.opensymphony.xwork2.ActionSupport;
import com.quartz.monitor.core.JobContainer;
import com.quartz.monitor.notification.EventService;
import com.quartz.monitor.notification.JobEvent;
import com.quartz.monitor.object.Job;
import com.quartz.monitor.object.QuartzInstance;
import com.quartz.monitor.object.Result;
import com.quartz.monitor.object.Scheduler;
import com.quartz.monitor.util.JsonUtil;
import com.quartz.monitor.util.Tools;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JobAction extends ActionSupport {

    private static final long serialVersionUID = 1L;

    private static Logger log = Logger.getLogger(JobAction.class);
    private String uuid;
    private List<Job> jobList = new ArrayList<Job>();
    private Map<String, Job> jobMap;
    private Job job;
    private Set<String> jobSet = new HashSet<String>();

    private Integer pageNum = 1;// 当前页数
    private Integer numPerPage = 20;// 每页的数量
    private Integer pageCount;// 总页数
    private Integer size;

    private List<JobEvent> jobExecutedList = new ArrayList<JobEvent>();
    private String jsonResult;

    public String list() throws Exception {

        QuartzInstance instance = Tools.getQuartzInstance();
        if (instance == null) {
            new InitAction().execute();
            instance = Tools.getQuartzInstance();
        }
        if (instance == null) {

            final Result result = new Result();
            result.setMessage("请先配置Quartz");
            result.setCallbackType("");
            JsonUtil.toJson(new Gson().toJson(result));
            return null;
        }
        final List<Scheduler> schedulers = instance.getSchedulerList();
        log.info(" schedulers list size:" + schedulers.size());
        if (schedulers != null && schedulers.size() > 0) {
            for (int i = 0; i < schedulers.size(); i++) {
                final Scheduler scheduler = schedulers.get(i);
                final List<Job> temp = instance.getJmxAdapter()
                        .getJobDetails(instance, scheduler);
                for (final Job job : temp) {
                    final String id = Tools.generateUUID();
                    job.setUuid(id);
                    JobContainer.addJob(id, job);
                    jobList.add(job);
                }
            }
        }

        pageCount = Tools.getPageSize(jobList.size(), numPerPage);
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageNum > pageCount) {
            pageNum = pageCount;
        }
        log.info("job size:" + jobList.size());
        size = jobList.size();
        return "list";
    }

    public String start() throws Exception {

        final QuartzInstance instance = Tools.getQuartzInstance();
        final Job job = JobContainer.getJobById(uuid);
        instance.getJmxAdapter().startJobNow(instance,
                instance.getSchedulerByName(job.getSchedulerName()), job);

        final Result result = new Result();
        result.setStatusCode("200");
        result.setMessage("执行成功");
        result.setCallbackType("");
        JsonUtil.toJson(new Gson().toJson(result));
        return null;
    }

    public String delete() throws Exception {

        final QuartzInstance instance = Tools.getQuartzInstance();

        final Job job = JobContainer.getJobById(uuid);
        JobContainer.removeJobById(uuid);
        log.info("delete a quartz job!");
        instance.getJmxAdapter().deleteJob(instance,
                instance.getSchedulerByName(job.getSchedulerName()), job);
        final Result result = new Result();
        result.setMessage("删除成功");
        JsonUtil.toJson(new Gson().toJson(result));
        return null;
    }

    public String pause() throws Exception {

        final QuartzInstance instance = Tools.getQuartzInstance();

        final Job job = JobContainer.getJobById(uuid);
        log.info("pause a quartz job!");
        instance.getJmxAdapter().pauseJob(instance,
                instance.getSchedulerByName(job.getSchedulerName()), job);
        final Result result = new Result();
        result.setMessage("Job已暂停");
        result.setCallbackType("");
        JsonUtil.toJson(new Gson().toJson(result));
        return null;
    }

    public String resume() throws Exception {

        final QuartzInstance instance = Tools.getQuartzInstance();

        final Job job = JobContainer.getJobById(uuid);
        log.info("resume a quartz job!");
        instance.getJmxAdapter().resumeJob(instance,
                instance.getSchedulerByName(job.getSchedulerName()), job);

        final Result result = new Result();
        result.setMessage("Job已恢复");
        result.setCallbackType("");
        JsonUtil.toJson(new Gson().toJson(result));
        return null;
    }

    public String show() throws Exception {

        jobMap = JobContainer.getJobMap();

        for (final Map.Entry<String, Job> entry : jobMap.entrySet()) {
            jobSet.add(entry.getValue().getSchedulerName());
        }
        log.info("get job map for add jsp,size:" + jobMap.size());
        log.info("get schedule name set for add jsp,size:" + jobSet.size());
        return "add";
    }

    public String add() throws Exception {

        final QuartzInstance instance = Tools.getQuartzInstance();

        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", job.getJobName());
        map.put("group", job.getGroup());
        map.put("description", job.getDescription());
        map.put("jobClass",
                JobContainer.getJobById(job.getJobClass()).getJobClass());
        // map.put("jobDetailClass", "org.quartz.Job");
        map.put("durability", true);
        map.put("jobDetailClass", "org.quartz.impl.JobDetailImpl");
        instance.getJmxAdapter().addJob(instance,
                instance.getSchedulerByName(job.getSchedulerName()), map);
        log.info("add job successfully!");

        final Result result = new Result();
        result.setMessage("添加成功");
        result.setCallbackType("");
        JsonUtil.toJson(new Gson().toJson(result));
        return null;
    }

    /**
    * @Title: listExecuted
    * @Description: 返回已执行的任务列表
    * @return
    * @throws
    */
    public String listExecuted() {
        final LinkedList<JobEvent> events = EventService.getEventList();
        int totalCount = 0;
        if (events != null && events.size() > 0) {
            totalCount = events.size() > EventService.getMaxShowEventListSize()
                    ? EventService.getMaxShowEventListSize() : events.size();

            for (int i = 0; i < totalCount; i++) {
                final JobEvent jEvent = events.get(i);
                jobExecutedList.add(jEvent);
            }
        }
        return "listExecuted";
    }

    public String jsonList() {
        final JSONObject jsonObject = new JSONObject();
        final JSONArray jsonArray = new JSONArray();
        int totalCount = 0;
        try {
            final LinkedList<JobEvent> events = EventService.getEventList();
            if (events != null && events.size() > 0) {
                totalCount = events.size() > EventService
                        .getMaxShowEventListSize()
                                ? EventService.getMaxShowEventListSize()
                                : events.size();

                for (int i = 0; i < events.size(); i++) {
                    final JobEvent jEvent = events.get(i);
                    final JSONObject object = JSONObject.fromObject(jEvent);
                    object.put("fireTime",
                            Tools.toStringFromDate(jEvent.getFireTime(),
                                    Tools.DATE_FORMAT_YYYYMMDDHHMMSS));
                    object.put("nextFireTime", Tools
                            .toStringFromDate(jEvent.getNextFireTime(),
                                    Tools.DATE_FORMAT_YYYYMMDDHHMMSS));
                    object.put("previousFireTime", Tools.toStringFromDate(
                            jEvent.getPreviousFireTime(),
                            Tools.DATE_FORMAT_YYYYMMDDHHMMSS));
                    object.put("scheduledFireTime", Tools.toStringFromDate(
                            jEvent.getScheduledFireTime(),
                            Tools.DATE_FORMAT_YYYYMMDDHHMMSS));
                    jsonArray.add(object);
                    if (i == EventService.getMaxShowEventListSize() - 1) {
                        break;
                    }
                }
            }
            jsonObject.put("data", jsonArray);
            jsonObject.put("success", true);
            jsonObject.put("totalCount", totalCount);
            jsonResult = jsonObject.toString();
        } catch (final Throwable t) {
            log.error(t);
        }

        return SUCCESS;
    }

    public List<Job> getJobList() {
        return jobList;
    }

    public void setJobList(final List<Job> jobList) {
        this.jobList = jobList;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(final Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getNumPerPage() {
        return numPerPage;
    }

    public void setNumPerPage(final Integer numPerPage) {
        this.numPerPage = numPerPage;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(final Integer pageCount) {
        this.pageCount = pageCount;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(final Integer size) {
        this.size = size;
    }

    public Map<String, Job> getJobMap() {
        return jobMap;
    }

    public void setJobMap(final Map<String, Job> jobMap) {
        this.jobMap = jobMap;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(final Job job) {
        this.job = job;
    }

    public Set<String> getJobSet() {
        return jobSet;
    }

    public void setJobSet(final Set<String> jobSet) {
        this.jobSet = jobSet;
    }

    public List<JobEvent> getJobExecutedList() {
        return jobExecutedList;
    }

    public void setJobExecutedList(final List<JobEvent> jobExecutedList) {
        this.jobExecutedList = jobExecutedList;
    }

    public String getJsonResult() {
        return jsonResult;
    }

    public void setJsonResult(final String jsonResult) {
        this.jsonResult = jsonResult;
    }

}
