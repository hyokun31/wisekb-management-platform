package wisekb.agent.rm;

import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.exceptions.YarnException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ResourceManagerAgentService {

    Map<String, Object> getConfiguration();

    Map<String, Object> getResourceManagerInfo();

    Map<String, Object> getJVMHeap();

    Map<String, Object> getClusterMetrics();

    Map<String, Object> getAppStatInfo(String applicationId);

    void killApplication(String applicationId) throws IOException, YarnException;

    void moveApplicationAcrossQueues(String applicationId, String queue) throws YarnException, IOException;

    Map<String, Object> getApplicationAttemptReport(String applicationAttemptId) throws YarnException, IOException;

    Map<String, Object> getContainerReport(String containerId) throws YarnException, IOException;

    List listApplications(Set<String> appTypes, List<String> appStates, boolean allAppStates) throws YarnException, IOException;

    Map<String, Object> getApplicationReport(String applicationId) throws YarnException, IOException;

    List listApplicationAttempts(String applicationId) throws YarnException, IOException;

    List listContainers(String appAttemptId) throws YarnException, IOException;

    Map<String, String> getBlockers();

    List getAllQueues() throws IOException, YarnException;

    Map<String, Object> getQueueInfo(String queueName) throws IOException, YarnException;

    List getApplicationsOfQueue(String queueName) throws IOException, YarnException;

    Map getApplication(ApplicationReport application);

    List listClusterNodes(Set<NodeState> nodeStates) throws YarnException, IOException;

    Map<String, Object> getNodeStatus(String nodeIdStr) throws YarnException, IOException;

    List getNodes() throws YarnException, IOException;

    String getApplicationLog(String applicationId) throws IOException;

    String getApplicationLog(String applicationId, String appOwner) throws IOException;

    String getApplicationTaskLog(String jobIdStr, String taskAttemptIdStr) throws IOException;

    String getContainersLogs(String appId, String containerId, String nodeId, String jobOwner) throws IOException;

    List<Map> getRunningMRJobs() throws Exception;

    String getJobStatus(String jobId);

    Map getRunningMRJobReport(String jobId) throws Exception;

    List<Map> getRunningMRTasks(String jobId) throws Exception;
}
