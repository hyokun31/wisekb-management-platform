package wisekb.web.hadoop.resourcemanager;

import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.exceptions.YarnException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Park on 2016. 9. 26..
 */
public interface ResourceManagerService {

    void killApplication(String applicationId) throws IOException, YarnException;

    Map<String, Object> getAppStatInfo(String applicationId);

    Map<String, Object> getJVMHeap();

    Map<String, Object> getClusterMetrics();

    List<Map> getLastClusterMetrics();

    List getAllQueues() throws IOException, YarnException;

    Map<String, Object> getApplicationAttemptReport(String applicationAttemptId) throws IOException, YarnException;

    String getApplicationLog(String applicationId) throws IOException;

    String getApplicationLog(String applicationId, String appOwner) throws IOException;

    Map<String, Object> getApplicationReport(String applicationId) throws IOException, YarnException;

    List getApplicationsOfQueue(String queueName) throws IOException, YarnException;

    Map<String, String> getBlockers();

    Map<String, Object> getConfiguration();

    Map<String, Object> getContainerReport(String containerId) throws IOException, YarnException;

    String getContainersLogs(String appId, String containerId, String nodeId, String jobOwner) throws IOException;

    Map<String, Object> getNodeStatus(String nodeIdStr) throws IOException, YarnException;

    List getNodes() throws IOException, YarnException;

    Map<String, Object> getQueueInfo(String queueName) throws IOException, YarnException;

    List listApplicationAttempts(String applicationId) throws IOException, YarnException;

    List listApplications(Set<String> appTypes, List<String> appStates, boolean allAppStates) throws IOException, YarnException;

    List listClusterNodes(Set<NodeState> nodeStates) throws IOException, YarnException;

    List listContainers(String appAttemptId) throws IOException, YarnException;

    void moveApplicationAcrossQueues(String applicationId, String queue) throws IOException, YarnException;

    List getRunningApplications() throws IOException, YarnException;

    List<Map> getRunningMRJobs() throws Exception;

    String getJobStatus(String jobId) throws Exception;

    Map getRunningMRJobReport(String jobId) throws Exception;

    List<Map> getRunningMRTasks(String jobId) throws Exception;
}
