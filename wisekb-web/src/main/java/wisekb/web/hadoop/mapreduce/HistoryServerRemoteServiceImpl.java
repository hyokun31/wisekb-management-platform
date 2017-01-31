package wisekb.web.hadoop.mapreduce;

import org.apache.hadoop.mapreduce.v2.util.MRApps;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import wisekb.web.util.AgentUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ersione on 2016. 9. 28..
 */
@Service
public class HistoryServerRemoteServiceImpl implements HistoryServerRemoteService {

    @Value("#{hadoop['history.server.url']}")
    private String HISTORY_SERVER_URL;

    @Autowired
    AgentUtil agentUtil;

    @Override
    public Map<String, Object> getHistoryServerInfo() {
        String url = MessageFormatter.format("{}/ws/v1/history", HISTORY_SERVER_URL).getMessage();
        Map<String, Object> returnMap = invoke(url);
        return returnMap;
    }

    @Override
    public Map<String, Object> getJobInfo(String jobId) {
        Map<String, Object> returnMap = new HashMap<>();

        returnMap.putAll(this.getJob(jobId));
        returnMap.putAll(this.getAttempts(jobId));
        returnMap.putAll(this.getJobConf(jobId));

        Map<String, Object> counterMap = this.getCounters(jobId);
        Map<String, Object> jobCounters = (Map<String, Object>) counterMap.get("jobCounters");
        List<Map<String, Object>> counterGroup = (List<Map<String, Object>>) jobCounters.get("counterGroup");
        List<Map<String, Object>> groupList = new ArrayList<>();

        for (Map<String, Object> counter : counterGroup) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) counter.get("counter");
            List<Map<String, Object>> counterList = new ArrayList<>();

            for (Map<String, Object> map : list) {
                map.put("leaf", true);

                counterList.add(map);
            }

            counter.put("name", counter.get("counterGroupName"));
            counter.remove("counter");
            counter.remove("counterGroupName");
            counter.put("children", counterList);

            groupList.add(counter);
        }

        returnMap.put("jobCounters", groupList);

        Map<String, Object> task = (Map<String, Object>) this.getTasks(jobId).get("tasks");
        List<Map<String, Object>> taskList = (List<Map<String, Object>>) task.get("task");
        List<Map<String, Object>> tasks = new ArrayList<>();

        for (Map<String, Object> taskMap : taskList) {
            String taskId = taskMap.get("id").toString();
            taskMap.putAll(this.getTaskCounters(jobId, taskId));
            Map<String, Object> taskAttempt = (Map<String, Object>) this.getTaskAttempts(jobId, taskId).get("taskAttempts");
            List<Map<String, Object>> taskAttemptList = (List<Map<String, Object>>) taskAttempt.get("taskAttempt");
            List<Map<String, Object>> taskAttempts = new ArrayList<>();

            for (Map<String, Object> taskAttemptMap : taskAttemptList) {
                String taskAttemptId = taskAttemptMap.get("id").toString();

                Map<String, Object> taskAttemptsCounters = this.getTaskAttemptsCounters(jobId, taskId, taskAttemptId);
                Map<String, Object> jobTaskAttemptCounters = (Map<String, Object>) taskAttemptsCounters.get("jobTaskAttemptCounters");
                List<Map<String, Object>> taskAttemptCounterGroup = (List<Map<String, Object>>) jobTaskAttemptCounters.get("taskAttemptCounterGroup");
                List<Map<String, Object>> taskAttemptCounterGroupList = new ArrayList<>();

                for (Map<String, Object> counter : taskAttemptCounterGroup) {
                    List<Map<String, Object>> list = (List<Map<String, Object>>) counter.get("counter");
                    List<Map<String, Object>> counterList = new ArrayList<>();

                    for (Map<String, Object> map : list) {
                        map.put("leaf", true);
                        map.put("id", map.get("name"));

                        counterList.add(map);
                    }

                    counter.put("id", counter.get("counterGroupName"));
                    counter.remove("counterGroupName");
                    counter.remove("counter");
                    counter.put("children", counterList);

                    taskAttemptCounterGroupList.add(counter);
                }

                if (taskAttemptCounterGroupList.size() > 0) {
                    taskAttemptMap.put("children", taskAttemptCounterGroupList);
                } else {
                    taskAttemptMap.put("leaf", true);
                }

                taskAttempts.add(taskAttemptMap);
            }
            taskMap.put("taskAttempts", taskAttempts);

            tasks.add(taskMap);
        }

        returnMap.put("tasks", tasks);

        return returnMap;
    }

    @Override
    public Map<String, Object> getJobs() {
        String url = MessageFormatter.format("{}/ws/v1/history/mapreduce/jobs", HISTORY_SERVER_URL).getMessage();
        return invoke(url);
    }

    @Override
    public Map<String, Object> getJob(String jobId) {
        String url = MessageFormatter.format("{}/ws/v1/history/mapreduce/jobs/{}", HISTORY_SERVER_URL, jobId).getMessage();
        Map result = invoke(url);
        return result;
    }

    @Override
    public Map<String, Object> getJobToList(String jobId) {
        String url = MessageFormatter.format("{}/ws/v1/history/mapreduce/jobs/{}", HISTORY_SERVER_URL, jobId).getMessage();

        Map<String, Object> jobRestMap = invoke(url);
        Map<String, Object> jobMap = (Map<String, Object>) jobRestMap.get("job");
        Map<String, Object> returnMap = new HashMap<>();
        List<Map<String, Object>> jobList = new ArrayList<>();

        for (Object keyset : jobMap.keySet().toArray()) {
            Map<String, Object> map = new HashMap<>();
            String key = keyset.toString();
            map.put("key", key);
            map.put("value", jobMap.get(key));

            jobList.add(map);
        }

        returnMap.put("job", jobList);
        return returnMap;
    }

    @Override
    public Map<String, Object> getAttempts(String jobId) {
        String url = MessageFormatter.format("{}/ws/v1/history/mapreduce/jobs/{}/jobattempts", HISTORY_SERVER_URL, jobId).getMessage();
        return invoke(url);
    }

    @Override
    public Map<String, Object> getCounters(String jobId) {
        String url = MessageFormatter.format("{}/ws/v1/history/mapreduce/jobs/{}/counters", HISTORY_SERVER_URL, jobId).getMessage();
        return invoke(url);
    }

    @Override
    public Map<String, Object> getJobConf(String jobId) {
        String url = MessageFormatter.format("{}/ws/v1/history/mapreduce/jobs/{}/conf", HISTORY_SERVER_URL, jobId).getMessage();
        return invoke(url);
    }

    @Override
    public Map<String, Object> getTasks(String jobId) {
        String url = MessageFormatter.format("{}/ws/v1/history/mapreduce/jobs/{}/tasks", HISTORY_SERVER_URL, jobId).getMessage();
        return invoke(url);
    }

    @Override
    public Map<String, Object> getTaskCounters(String jobId, String taskId) {
        String url = MessageFormatter.arrayFormat("{}/ws/v1/history/mapreduce/jobs/{}/tasks/{}/counters", new String[]{HISTORY_SERVER_URL, jobId, taskId}).getMessage();
        return invoke(url);
    }

    @Override
    public Map<String, Object> getTaskAttempts(String jobId, String taskId) {
        String url = MessageFormatter.arrayFormat("{}/ws/v1/history/mapreduce/jobs/{}/tasks/{}/attempts", new String[]{HISTORY_SERVER_URL, jobId, taskId}).getMessage();
        return invoke(url);
    }

    @Override
    public Map<String, Object> getTaskAttemptsCounters(String jobId, String taskId, String attemptId) {
        String url = MessageFormatter.arrayFormat("{}/ws/v1/history/mapreduce/jobs/{}/tasks/{}/attempts/{}/counters", new String[]{HISTORY_SERVER_URL, jobId, taskId, attemptId}).getMessage();
        return invoke(url);
    }

    @Override
    public Map<String, Object> getJobConfByProxy(String proxyServerUrl, String jobId) {
        String url = MessageFormatter.arrayFormat("http://{}/proxy/{}/ws/v1/mapreduce/jobs/{}/conf", new String[]{proxyServerUrl, this.getApplicationId(jobId), jobId}).getMessage();
        return invoke(url);
    }

    @Override
    public Map<String, Object> getJobByProxy(String proxyServerUrl, String jobId) {
        String url = MessageFormatter.arrayFormat("http://{}/proxy/{}/ws/v1/mapreduce/jobs/{}", new String[]{proxyServerUrl, this.getApplicationId(jobId), jobId}).getMessage();
        return invoke(url);
    }

    @Override
    public Map<String, Object> getAttemptsByProxy(String proxyServerUrl, String jobId) {
        String url = MessageFormatter.arrayFormat("http://{}/proxy/{}/ws/v1/mapreduce/jobs/{}/jobattempts", new String[]{proxyServerUrl, this.getApplicationId(jobId), jobId}).getMessage();
        return invoke(url);
    }

    @Override
    public Map<String, Object> getCountersByProxy(String proxyServerUrl, String jobId) {
        String url = MessageFormatter.arrayFormat("http://{}/proxy/{}/ws/v1/mapreduce/jobs/{}/counters", new String[]{proxyServerUrl, this.getApplicationId(jobId), jobId}).getMessage();
        return invoke(url);
    }

    @Override
    public Map<String, Object> getTasksByProxy(String proxyServerUrl, String jobId) {
        String url = MessageFormatter.arrayFormat("http://{}/proxy/{}/ws/v1/mapreduce/jobs/{}/tasks", new String[]{proxyServerUrl, this.getApplicationId(jobId), jobId}).getMessage();
        return invoke(url);
    }

    @Override
    public Map<String, Object> getTaskCountersByProxy(String proxyServerUrl, String jobId, String taskId) {
        String url = MessageFormatter.arrayFormat("http://{}/proxy/{}/ws/v1/mapreduce/jobs/{}/tasks/{}/counters", new String[]{proxyServerUrl, this.getApplicationId(jobId), jobId, taskId}).getMessage();
        return invoke(url);
    }

    @Override
    public Map<String, Object> getTaskAttemptsByProxy(String proxyServerUrl, String jobId, String taskId) {
        String url = MessageFormatter.arrayFormat("http://{}/proxy/{}/ws/v1/mapreduce/jobs/{}/tasks/{}/attempts", new String[]{proxyServerUrl, this.getApplicationId(jobId), jobId, taskId}).getMessage();
        return invoke(url);
    }

    @Override
    public Map<String, Object> getTaskAttemptsCountersByProxy(String proxyServerUrl, String jobId, String taskId, String attemptId) {
        String url = MessageFormatter.arrayFormat("http://{}/proxy/{}/ws/v1/mapreduce/jobs/{}/tasks/{}/attempt/{}/counters", new String[]{proxyServerUrl, this.getApplicationId(jobId), jobId, taskId, attemptId}).getMessage();
        return invoke(url);
    }

    @Override
    public List getJobsByApplication(String applicationId, String proxyServerUrl) {
        String url = MessageFormatter.format("http://{}/proxy/{}/ws/v1/mapreduce/jobs", proxyServerUrl, applicationId).getMessage();
        Map result = invoke(url);

        List<Map<String, Object>> returnList = new ArrayList<>();
        Map<String, Object> jobMap = (Map<String, Object>) result.get("jobs");
        List<Map<String, Object>> jobList = (List<Map<String, Object>>) jobMap.get("job");
        for (Map<String, Object> job : jobList) {
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("state", job.get("state").toString());
            returnMap.put("reducesTotal", job.get("reducesTotal").toString());
            returnMap.put("mapsCompleted", job.get("mapsCompleted").toString());
            returnMap.put("startTime", job.get("startTime").toString());
            returnMap.put("id", job.get("id").toString());
            returnMap.put("name", job.get("name").toString());
            returnMap.put("reducesCompleted", job.get("reducesCompleted").toString());
            returnMap.put("mapsTotal", job.get("mapsTotal").toString());
            returnMap.put("finishTime", job.get("finishTime").toString());
            returnMap.put("reduceProgress", job.get("reduceProgress").toString());
            returnMap.put("mapProgress", job.get("mapProgress").toString());

            returnList.add(returnMap);
        }

        return returnList;
    }

    @Override
    public String getApplicationTaskLog(String jobIdStr, String taskAttemptIdStr) throws IOException {
        return agentUtil.getResourceManagerAgentService().getApplicationTaskLog(jobIdStr, taskAttemptIdStr);
    }

    private String getApplicationId(String jobId) {
        return MRApps.toJobID(jobId).getAppId().toString();
    }

    private static Map invoke(String url) {
        try {
            RestTemplate template = new RestTemplate();
            ResponseEntity<Map> response = template.getForEntity(url, Map.class);
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }
}
