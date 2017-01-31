package wisekb.web.hadoop.resourcemanager;

import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wisekb.web.util.AgentUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Park on 2016. 9. 26..
 */
@Service
public class ResourceManagerServiceImpl implements ResourceManagerService {

    @Autowired
    AgentUtil agentUtil;

    @Autowired
    ResourceManagerRepository resourceManagerRepository;

    @Override
    public void killApplication(String applicationId) throws IOException, YarnException {
        agentUtil.getResourceManagerAgentService().killApplication(applicationId);
    }

    @Override
    public Map<String, Object> getAppStatInfo(String applicationId) {
        return agentUtil.getResourceManagerAgentService().getAppStatInfo(applicationId);
    }

    @Override
    public Map<String, Object> getJVMHeap() {
        return agentUtil.getResourceManagerAgentService().getJVMHeap();
    }

    @Override
    public Map<String, Object> getClusterMetrics() {
        return agentUtil.getResourceManagerAgentService().getClusterMetrics();
    }

    @Override
    public List<Map> getLastClusterMetrics() {
        return resourceManagerRepository.selectClusterMetrics();
    }

    @Override
    public List getAllQueues() throws IOException, YarnException {
        return agentUtil.getResourceManagerAgentService().getAllQueues();
    }

    @Override
    public Map<String, Object> getApplicationAttemptReport(String applicationAttemptId) throws IOException, YarnException {
        return agentUtil.getResourceManagerAgentService().getApplicationAttemptReport(applicationAttemptId);
    }

    @Override
    public String getApplicationLog(String applicationId) throws IOException {
        return agentUtil.getResourceManagerAgentService().getApplicationLog(applicationId);
    }

    @Override
    public String getApplicationLog(String applicationId, String appOwner) throws IOException {
        return agentUtil.getResourceManagerAgentService().getApplicationLog(applicationId, appOwner);
    }

    @Override
    public Map<String, Object> getApplicationReport(String applicationId) throws IOException, YarnException {
        return agentUtil.getResourceManagerAgentService().getApplicationReport(applicationId);
    }

    @Override
    public List getApplicationsOfQueue(String queueName) throws IOException, YarnException {
        return agentUtil.getResourceManagerAgentService().getApplicationsOfQueue(queueName);
    }

    @Override
    public Map<String, String> getBlockers() {
        return agentUtil.getResourceManagerAgentService().getBlockers();
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return agentUtil.getResourceManagerAgentService().getConfiguration();
    }

    @Override
    public Map<String, Object> getContainerReport(String containerId) throws IOException, YarnException {
        return agentUtil.getResourceManagerAgentService().getContainerReport(containerId);
    }

    @Override
    public String getContainersLogs(String appId, String containerId, String nodeId, String jobOwner) throws IOException {
        return agentUtil.getResourceManagerAgentService().getContainersLogs(appId, containerId, nodeId, jobOwner);
    }

    @Override
    public Map<String, Object> getNodeStatus(String nodeIdStr) throws IOException, YarnException {
        return agentUtil.getResourceManagerAgentService().getNodeStatus(nodeIdStr);
    }

    @Override
    public List getNodes() throws IOException, YarnException {
        return agentUtil.getResourceManagerAgentService().getNodes();
    }

    @Override
    public Map<String, Object> getQueueInfo(String queueName) throws IOException, YarnException {
        return agentUtil.getResourceManagerAgentService().getQueueInfo(queueName);
    }

    @Override
    public List listApplicationAttempts(String applicationId) throws IOException, YarnException {
        return agentUtil.getResourceManagerAgentService().listApplicationAttempts(applicationId);
    }

    @Override
    public List listApplications(Set<String> appTypes, List<String> appStates, boolean allAppStates) throws IOException, YarnException {
        return agentUtil.getResourceManagerAgentService().listApplications(appTypes, appStates, allAppStates);
    }

    @Override
    public List listClusterNodes(Set<NodeState> nodeStates) throws IOException, YarnException {
        return agentUtil.getResourceManagerAgentService().listClusterNodes(nodeStates);
    }

    @Override
    public List listContainers(String appAttemptId) throws IOException, YarnException {
        return agentUtil.getResourceManagerAgentService().listContainers(appAttemptId);
    }

    @Override
    public void moveApplicationAcrossQueues(String applicationId, String queue) throws IOException, YarnException {
        agentUtil.getResourceManagerAgentService().moveApplicationAcrossQueues(applicationId, queue);
    }

    @Override
    public List getRunningApplications() throws IOException, YarnException {
        List<Map> runningList = agentUtil.getResourceManagerAgentService().listApplications(null, new ArrayList(), false);
        List<Map> returnList = new ArrayList<>();
        long endTime = System.currentTimeMillis();

        for (Map app : runningList) {
            long startTime = (long) app.get("startTime");

            app.put("elapsedTime", endTime - startTime);

            returnList.add(app);
        }
        return returnList;
    }

    @Override
    public List<Map> getRunningMRJobs() throws Exception {
        return agentUtil.getResourceManagerAgentService().getRunningMRJobs();
    }

    @Override
    public String getJobStatus(String jobId) throws Exception {
        return agentUtil.getResourceManagerAgentService().getJobStatus(jobId);
    }

    @Override
    public Map getRunningMRJobReport(String jobId) throws Exception {
        return agentUtil.getResourceManagerAgentService().getRunningMRJobReport(jobId);
    }

    @Override
    public List<Map> getRunningMRTasks(String jobId) throws Exception {
        return agentUtil.getResourceManagerAgentService().getRunningMRTasks(jobId);
    }
}
