package wisekb.web.hadoop.mapreduce;

import java.util.Map;

/**
 * Created by ersione on 2016. 9. 28..
 */
public interface MapReduceRemoteService {
    public Map<String, Object> getAllJobs();

    public Map<String, Object> getJobInfo(String jobId);

    public Map<String, Object> getJobConf(String proxyServerUrl, String jobId);

    public Map<String, Object> getJob(String jobId);

    public Map<String, Object> getAttempts(String jobId);

    public Map<String, Object> getCounters(String proxyServerUrl, String jobId);

    public Map<String, Object> getTasks(String jobId);

    public Map<String, Object> getTaskCounters(String jobId, String taskId);

    public Map<String, Object> getTaskAttempts(String jobId, String taskId);

    public Map<String, Object> getTaskAttemptsCounters(String jobId, String taskId, String attemptId);
}
