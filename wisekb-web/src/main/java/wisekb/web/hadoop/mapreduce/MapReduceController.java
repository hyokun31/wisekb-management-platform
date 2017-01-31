
package wisekb.web.hadoop.mapreduce;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import wisekb.shared.core.exception.ServiceException;
import wisekb.shared.core.rest.Response;
import wisekb.shared.util.DateUtils;
import wisekb.web.hadoop.resourcemanager.ResourceManagerService;

import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by ersione on 2016. 9. 28..
 */

@RestController
@RequestMapping(value = "/hadoop/mapreduce")
public class MapReduceController {
    private Logger logger = LoggerFactory.getLogger(MapReduceController.class);

    private final String DATEFORMAT_DAY = "MM/dd";

    @Autowired
    private ObjectMapper objectMapper;

    @Value("#{hadoop['resourcemanager.host']}")
    private String RESOURCEMANAGER_HOST;

    @Value("#{hadoop['resourcemanager.port']}")
    private String RESOURCEMANAGER_PORT;
    
    @Autowired
    private HistoryServerRemoteService historyServerRemoteService;
    
    @Autowired
    private ResourceManagerService resourceManagerService;

    @Autowired
    private MapReduceRemoteService mapReduceRemoteService;

    @Autowired
    private MapReduceTimelineService mapReduceTimelineService;

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> info() {
        return historyServerRemoteService.getHistoryServerInfo();
    }

    @RequestMapping(value = "/jobs", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Response jobs() {
        Response response = new Response();
        try {
            Map jobsMap = historyServerRemoteService.getJobs();
            Map jobMap = (Map) jobsMap.get("jobs");
            List<Map> jobList = (List) jobMap.get("job");

            List<Map> runningList = resourceManagerService.getRunningMRJobs();

            logger.info(String.valueOf(runningList.size()));

            jobList.addAll(0, runningList);

            response.getList().addAll(jobList);
            response.setTotal(response.getList().size());
            response.setSuccess(true);

            return response;
        } catch (Exception ex) {
            throw new ServiceException("MapReduce Job 정보를 불러오는 중 오류가 발생하였습니다.", ex);
        }
    }

    @RequestMapping(value = "/jobs/timeseries", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Response timeseries() {
        Response response = new Response();

        try {
            Map jobsMap = historyServerRemoteService.getJobs();
            Map jobMap = (Map) jobsMap.get("jobs");
            List<Map> jobList = (List) jobMap.get("job");

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATEFORMAT_DAY);
            Map countMap = new HashMap();

            Date startTime;
            String dateString;
            for (Map job : jobList) {
                startTime = new Date(Long.parseLong(job.get("startTime").toString()));
                dateString = simpleDateFormat.format(startTime);
                if (countMap.get(dateString) == null) {
                    countMap.put(dateString, 1);
                }
                else {
                    countMap.put(dateString, (int) countMap.get(dateString) + 1);
                }
            }

            Object keyArr[] = countMap.keySet().toArray();

            Map series;
            List<Map> seriesList = new ArrayList<>();
            for (Object key : keyArr) {
                series = new HashMap();

                series.put("name", key);
                series.put("y", countMap.get(key));

                seriesList.add(series);
            }

            Collections.sort(seriesList, new KeyValueComparator());

            response.getMap().put("series", seriesList);
            response.setTotal(response.getList().size());

            return response;
        } catch (Exception ex) {
            throw new ServiceException("Timeseries 정보를 불러오는 중 오류가 발생하였습니다.", ex);
        }

    }

    @RequestMapping(value = "/jobs/job", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Response job(@RequestParam String jobId, @RequestParam String state) {
        Response response = new Response();

        try {
            Map results = new HashMap<>();
            if (state.equals("RUNNING") && resourceManagerService.getJobStatus(jobId).equals("RUNNING")) {
                results = resourceManagerService.getRunningMRJobReport(jobId);
            }
            else {
                Map jobMap = historyServerRemoteService.getJob(jobId);

                results = (Map) jobMap.get("job");
            }

            Object keyArr[] = results.keySet().toArray();
            List<Map> responseList = new ArrayList<>();
            Map responseMap;

            for (Object key : keyArr) {
                responseMap = new HashMap();

                responseMap.put("key", key);
                responseMap.put("value", results.get(key));

                responseList.add(responseMap);
            }

            response.getList().addAll(responseList);
            response.setTotal(response.getList().size());

            return response;
        } catch (Exception ex) {
            throw new ServiceException("MapReduce 상세정보를 불러오는 중 오류가 발생하였습니다.", ex);
        }

    }

    @RequestMapping(value = "/jobs/job/info", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> jobInfo(@RequestParam String jobId) {
        return historyServerRemoteService.getJobInfo(jobId);
    }

    @RequestMapping(value = "/jobs/job/attempts", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> jobAttempts(@RequestParam String jobId) {
        return historyServerRemoteService.getAttempts(jobId);
    }


    /**
     * MapReduce Job의 Job Counter를 반환한다.
     * 최초 UI에서 expand를 하므로 강제로 호출이된다. 따라서 기본값을 빈값으로 파라미터를 모두 설정하고 빈값이 들어오면 빈값을 리턴한다.
     */
    @RequestMapping(value = "/jobs/job/counters", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<Map> jobCounters(@RequestParam(defaultValue = "") String jobId,
                                 @RequestParam(defaultValue = "") String state) {

        try {

            Map counters;

            if (state.equals("RUNNING") && resourceManagerService.getJobStatus(jobId).equals("RUNNING")) {
                counters = mapReduceRemoteService.getCounters(RESOURCEMANAGER_HOST + ":" + RESOURCEMANAGER_PORT, jobId);
            }
            else {
                counters = historyServerRemoteService.getCounters(jobId);
            }

            Map jobCounters = (Map) counters.get("jobCounters");
            List<Map> counterGroupList = (List<Map>) jobCounters.get("counterGroup");

            for (Map counterGroup : counterGroupList) {
                List<Map> counterList = (List<Map>) counterGroup.get("counter");
                int cgi = counterGroupList.indexOf(counterGroup);

                for (Map counter : counterList) {
                    int ci = counterList.indexOf(counter);
                    counter.put("leaf", true);
                    counterList.set(ci, counter);
                }

                counterGroup.put("name", counterGroup.get("counterGroupName"));
                counterGroup.put("leaf", false);
                counterGroup.put("children", counterList);
                counterGroup.remove("counter");
                counterGroupList.set(cgi, counterGroup);
            }

            jobCounters.put("counterGroup", counterGroupList);
            counters.put("jobCounters", jobCounters);

            return counterGroupList;
        } catch (Exception ex) {
            throw new ServiceException("Job Counter 정보를 불러오는 중 오류가 발생하였습니다.", ex);
        }
    }

    @RequestMapping(value = "/jobs/job/configuration", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> jobConfiguration(@RequestParam String jobId,
                                                @RequestParam String state) {

        try {
            Map<String, Object> jobConf = new HashMap<>();
            if (state.equals("RUNNING") && resourceManagerService.getJobStatus(jobId).equals("RUNNING")) {
                jobConf = mapReduceRemoteService.getJobConf(RESOURCEMANAGER_HOST + ":" + RESOURCEMANAGER_PORT, jobId);
            }
            else {
                jobConf = historyServerRemoteService.getJobConf(jobId);
            }

            SortedMap configuration = new TreeMap();
            configuration.putAll(jobConf);

            return configuration;
        } catch (Exception ex) {
            throw new ServiceException("Configuration 정보를 불러오는 중 오류가 발생하였습니다.", ex);
        }

    }

    @RequestMapping(value = "/jobs/job/tasks", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response tasks(@RequestParam String jobId,
                          @RequestParam String state,
                          @RequestParam(defaultValue = "") String node) {
        Response response = new Response();
        try {

            if (node.equals("root")) {
                if (state.equals("RUNNING") && resourceManagerService.getJobStatus(jobId).equals("RUNNING") ) {
                    response.getList().addAll(resourceManagerService.getRunningMRTasks(jobId));
                }
                else {
                    Map tasksMap = historyServerRemoteService.getTasks(jobId);
                    Map taskMap = (Map) tasksMap.get("tasks");

                    response.getList().addAll((List) taskMap.get("task"));
                }
            }
            else {
                List<Map> responseList = new ArrayList<>();
                Map attemptMap = historyServerRemoteService.getTaskAttempts(jobId, node);

                if (attemptMap.get("taskAttempts") != null) {
                    Map attempt = (Map) attemptMap.get("taskAttempts");
                    List<Map> attemptList = (List<Map>) attempt.get("taskAttempt");

                    for (Map map : attemptList) {
                        map.put("leaf", true);

                        responseList.add(map);
                    }

                    response.getList().addAll(responseList);
                }
            }

            response.setTotal(response.getList().size());
            return response;
        } catch (Exception ex) {
            throw new ServiceException("Job Task 정보를 불어오는 중 오류가 발생하였습니다.", ex);
        }
    }

    @RequestMapping(value = "/jobs/job/tasks/task/counters", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> taskCounters(@RequestParam String jobId,
                                            @RequestParam String taskId) {

        try {
            return historyServerRemoteService.getTaskCounters(jobId, taskId);
        } catch (Exception ex) {
            throw new ServiceException("Task Counter 정보를 불어오는 중 오류가 발생하였습니다.", ex);
        }
    }

    @RequestMapping(value = "/jobs/job/tasks/task/attempts", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> taskAttempts(@RequestParam String jobId,
                                            @RequestParam String taskId) {

        try {
            return historyServerRemoteService.getTaskAttempts(jobId, taskId);
        } catch (Exception ex) {
            throw new ServiceException("Task attempts 정보를 불어오는 중 오류가 발생하였습니다.", ex);
        }
    }

    @RequestMapping(value = "/jobs/job/tasks/task/attempts/attempt/counters", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> taskAttemptsCounters(@RequestParam String jobId,
                                                    @RequestParam String taskId,
                                                    @RequestParam String attemptId) {

        try {
            return historyServerRemoteService.getTaskAttemptsCounters(jobId, taskId, attemptId);
        } catch (Exception ex) {
            throw new ServiceException("Attempt Counter 정보를 불어오는 중 오류가 발생하였습니다.", ex);
        }

    }

    @RequestMapping(value = "/jobs/log", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public String taskAttemptsCounters(@RequestParam String jobId,
                                       @RequestParam String attemptId) {
        try {
            return historyServerRemoteService.getApplicationTaskLog(jobId, attemptId);
        } catch (Exception ex) {
            throw new ServiceException("로그 정보를 불어오는 중 오류가 발생하였습니다.", ex);
        }
    }

    @RequestMapping(value = "/timeline", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Response mrTimeline(@RequestParam String jobId) {
        Response response = new Response();
        try {

            List<Map> groupList = new ArrayList<>();

            Map groupMap;

            List<Map> timelineList = mapReduceTimelineService.getMRTimeline(jobId);

            int map = 0;
            int reduce = 0;

            for (Map timeline : timelineList) {
                 if (timeline.get("group").equals("MAP")) {
                    map ++;
                }
                else {
                    reduce ++;
                }
            }

            if (map > 0) {
                groupMap = new HashMap();
                groupMap.put("id", "MAP");
                groupMap.put("content", "MAP");

                groupList.add(groupMap);
            }

            if (reduce > 0) {
                groupMap = new HashMap();
                groupMap.put("id", "REDUCE");
                groupMap.put("content", "REDUCE");

                groupList.add(groupMap);
            }

            response.getMap().put("group", groupList);

            response.getList().addAll(timelineList);
            response.setTotal(response.getList().size());
            return response;
        } catch (Exception ex) {
            throw new ServiceException("Timeline 정보를 불어오는 중 오류가 발생하였습니다.", ex);
        }
    }

    @RequestMapping(value = "/attempts/counter/tree", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<Map> attemptsCounterTree(@RequestParam String jobId,
                                        @RequestParam String taskId,
                                        @RequestParam(defaultValue = "") String node) {
        try {

            if (node.equals("root")) {
                Map taskAttempts = historyServerRemoteService.getTaskAttempts(jobId, taskId);
                Map taskAttempt = (Map) taskAttempts.get("taskAttempts");
                List<Map> taskAttemptList = (List<Map>) taskAttempt.get("taskAttempt");

                return taskAttemptList;
            }
            else {
                Map counters = historyServerRemoteService.getTaskAttemptsCounters(jobId, taskId, node);

                Map taskAttemptCounters = (Map) counters.get("jobTaskAttemptCounters");
                List<Map> taskAttemptGroupList = (List<Map>) taskAttemptCounters.get("taskAttemptCounterGroup");
                Map responseCounter;
                List<Map> responseList = new ArrayList<>();
                List<Map> leafList;

                for (Map counterGroup : taskAttemptGroupList) {
                    responseCounter = new HashMap();
                    List<Map> counterList = (List<Map>) counterGroup.get("counter");
                    int cgi = taskAttemptGroupList.indexOf(counterGroup);

                    leafList = new ArrayList<>();
                    for (Map counter : counterList) {
                        int ci = counterList.indexOf(counter);
                        counter.put("id", counter.get("name"));
                        counter.put("leaf", true);
                        leafList.add(ci, counter);
                    }

                    responseCounter.put("id", counterGroup.get("counterGroupName"));
                    responseCounter.put("leaf", false);
                    responseCounter.put("children", leafList);

                    responseList.add(cgi, responseCounter);
                }

                return responseList;
            }

        } catch (Exception ex) {
            throw new ServiceException("Timeline 정보를 불어오는 중 오류가 발생하였습니다.", ex);
        }
    }

    private long getDuration(Map job) {
        Long start = (Long) job.get("startTime");
        Long finish = (Long) job.get("finishTime");
        return DateUtils.getDiffSeconds(new Date(finish), new Date(start));
    }

    private class KeyValueComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Map map1 = (Map) o1;
            Map map2 = (Map) o2;

            return map1.get("name").toString().compareTo(map2.get("name").toString());
        }
    }
}