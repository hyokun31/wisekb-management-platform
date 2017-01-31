package wisekb.web.hadoop.mapreduce;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface HistoryServerRemoteService {
    Map<String, Object> getHistoryServerInfo();

    Map<String, Object> getJobInfo(String jobId);

    Map<String, Object> getJobs();

    Map<String, Object> getJob(String jobId);

    Map<String, Object> getJobToList(String jobId);

    Map<String, Object> getAttempts(String jobId);

    Map<String, Object> getCounters(String jobId);

    Map<String, Object> getJobConf(String jobId);

    Map<String, Object> getTasks(String jobId);

    Map<String, Object> getTaskCounters(String jobId, String taskId);

    Map<String, Object> getTaskAttempts(String jobId, String taskId);

    Map<String, Object> getTaskAttemptsCounters(String jobId, String taskId, String attemptId);

    Map<String, Object> getJobConfByProxy(String proxyServerUrl, String jobId);

    Map<String, Object> getJobByProxy(String proxyServerUrl, String jobId);

    Map<String, Object> getAttemptsByProxy(String proxyServerUrl, String jobId);

    Map<String, Object> getCountersByProxy(String proxyServerUrl, String jobId);

    Map<String, Object> getTasksByProxy(String proxyServerUrl, String jobId);

    Map<String, Object> getTaskCountersByProxy(String proxyServerUrl, String jobId, String taskId);

    Map<String, Object> getTaskAttemptsByProxy(String proxyServerUrl, String jobId, String taskId);

    Map<String, Object> getTaskAttemptsCountersByProxy(String proxyServerUrl, String jobId, String taskId, String attemptId);

    List getJobsByApplication(String applicationId, String proxyServerUrl);

    String getApplicationTaskLog(String jobIdStr, String taskAttemptIdStr) throws IOException;
}
