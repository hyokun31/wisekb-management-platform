package org.apache.hadoop.yarn.server.resourcemanager;

import wisekb.agent.rm.ResourceManagerAgent;
import wisekb.agent.rm.ResourceManagerAgentService;
import com.google.protobuf.ServiceException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileContext;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TypeConverter;
import org.apache.hadoop.mapreduce.v2.LogParams;
import org.apache.hadoop.mapreduce.v2.api.MRClientProtocol;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.GetJobReportRequest;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.GetTaskReportsRequest;
import org.apache.hadoop.mapreduce.v2.api.records.*;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.service.ServiceStateException;
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationsRequest;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.ApplicationNotFoundException;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.factories.RecordFactory;
import org.apache.hadoop.yarn.factory.providers.RecordFactoryProvider;
import org.apache.hadoop.yarn.ipc.YarnRPC;
import org.apache.hadoop.yarn.logaggregation.AggregatedLogFormat;
import org.apache.hadoop.yarn.logaggregation.LogAggregationUtils;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.ClusterMetricsInfo;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.YarnVersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.*;
import java.net.InetSocketAddress;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static wisekb.agent.rm.ResourceManagerAgent.MEGA_BYTES;

/**
 * @See {@link org.apache.hadoop.yarn.client.cli.ApplicationCLI}
 * @See {@link org.apache.hadoop.yarn.client.cli.NodeCLI}
 * @See {@link org.apache.hadoop.yarn.client.cli.LogsCLI}
 */
public class ResourceManagerAgentServiceImpl implements ResourceManagerAgentService, InitializingBean, DisposableBean {

    private static final Log LOG = LogFactory.getLog(ResourceManagerAgent.class);

    /**
     * Apache Hadoop Resource Manager
     */
    private ResourceManager resourceManager;

    /**
     * YARN Client for Apache Hadoop Resource Manager
     */
    private YarnClient yarnClient;

    /**
     * SLF4J Application Logging
     */
    private Logger logger = LoggerFactory.getLogger(ResourceManagerAgentServiceImpl.class);

    /**
     * SLF4J Exception Logging
     */
    private Logger exceptionLogger = LoggerFactory.getLogger("flamingo.exception");

    public static String formatDate(final Date date, final String pattern) {
        final SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.resourceManager = ResourceManagerAgent.resourceManager;

        try {
            yarnClient = YarnClient.createYarnClient();
            yarnClient.init(resourceManager.getConfig());
            yarnClient.start();
            LOG.info("Flamingo - YARN Client를 초기화했습니다.");
        } catch (ServiceStateException e) {
            LOG.error("YARN Client 초기화를 실패했습니다");
        }
    }

    @Override
    public Map<String, Object> getConfiguration() {
        Map<String, Object> map = new HashMap<>();
        Configuration config = this.resourceManager.getConfig();
        Iterator<Map.Entry<String, String>> iterator = config.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    @Override
    public Map getResourceManagerInfo() {
        try {
            Map<String, Object> result = new HashMap<String, Object>();
            Configuration conf = resourceManager.getConfig();
            result.put("buildVersion", YarnVersionInfo.getBuildVersion());
            result.put("version", YarnVersionInfo.getVersion());
            result.put("startTime", resourceManager.getStartTime());
            result.put("name", resourceManager.getName());
            result.put("nmHeartbeatInterval", conf.getLong(YarnConfiguration.RM_NM_HEARTBEAT_INTERVAL_MS, YarnConfiguration.DEFAULT_RM_NM_HEARTBEAT_INTERVAL_MS));

            result.put("queue", getAllQueues().size());
            result.put("jvm", getJVMHeap());
            result.put("cluster", getClusterMetrics());
            return result;
        } catch (Exception ex) {
            throw new RuntimeException("[Resource Manager Agent] Cannot retrieves a information of Resource Manager", ex);
        }
    }

    @Override
    public Map<String, Object> getJVMHeap() {
        final Runtime rt = Runtime.getRuntime();
        final long maxMemory = rt.maxMemory() / MEGA_BYTES;
        final long totalMemory = rt.totalMemory() / MEGA_BYTES;
        final long freeMemory = rt.freeMemory() / MEGA_BYTES;
        final long usedMemory = totalMemory - freeMemory;

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("maxMemory", maxMemory);
        result.put("totalMemory", totalMemory);
        result.put("freeMemory", freeMemory);
        result.put("usedMemory", usedMemory);
        return result;
    }

    @Override
    public Map<String, Object> getClusterMetrics() {
        try {
            Map<String, Object> result = new HashMap<String, Object>();

            //2.6.0 이전 부터 아래 코드 사용
            //ClusterMetricsInfo clusterMetrics = new ClusterMetricsInfo(resourceManager, resourceManager.getRMContext());
            //2.7.0 이후 부터 아래 코드 사용
            ClusterMetricsInfo clusterMetrics = new ClusterMetricsInfo(resourceManager);

            // running state
            result.put("appsSubmitted", clusterMetrics.getAppsSubmitted());
            result.put("appsPending", clusterMetrics.getAppsPending());
            result.put("appsRunning", clusterMetrics.getAppsRunning());
            result.put("appsCompleted", clusterMetrics.getAppsCompleted());
            result.put("appsFailed", clusterMetrics.getAppsFailed());
            result.put("appsKilled", clusterMetrics.getAppsKilled());

            // containers
            result.put("containersAllocated", clusterMetrics.getContainersAllocated());
            result.put("containersPending", clusterMetrics.getPendingContainers());
            result.put("containersReserved", clusterMetrics.getReservedContainers());

            // cluster memory
            result.put("allocatedMB", clusterMetrics.getAllocatedMB());
            result.put("totalMB", clusterMetrics.getTotalMB());
            result.put("reservedMB", clusterMetrics.getReservedMB());

            // datanode state
            result.put("activeNodes", clusterMetrics.getActiveNodes());
            result.put("rebootedNodes", clusterMetrics.getRebootedNodes());
            result.put("decommissionedNodes", clusterMetrics.getDecommissionedNodes());
            result.put("lostNodes", clusterMetrics.getLostNodes());
            result.put("unhealthyNodes", clusterMetrics.getUnhealthyNodes());

            result.put("allocatedVirtualCores", clusterMetrics.getAllocatedVirtualCores());
            result.put("availableVirtualCores", clusterMetrics.getAvailableVirtualCores());
            result.put("reservedVirtualCores", clusterMetrics.getReservedVirtualCores());
            result.put("totalVirtualCores", clusterMetrics.getTotalVirtualCores());

            return result;
        } catch (Exception ex) {
            throw new RuntimeException("[Resource Manager Agent] Cannot retrieves a information of ClusterMetrics", ex);
        }
    }

    @Override
    public Map<String, Object> getAppStatInfo(String applicationId) {
        return null;
    }

    @Override
    public void killApplication(String applicationId) throws IOException, YarnException {
        ApplicationId appId = ConverterUtils.toApplicationId(applicationId);
        ApplicationReport appReport = null;
        try {
            appReport = yarnClient.getApplicationReport(appId);
        } catch (ApplicationNotFoundException e) {
            System.out.println("Application with id '" + applicationId + "' doesn't exist in RM.");
            throw e;
        }

        if (appReport.getYarnApplicationState() == YarnApplicationState.FINISHED
            || appReport.getYarnApplicationState() == YarnApplicationState.KILLED
            || appReport.getYarnApplicationState() == YarnApplicationState.FAILED) {
            System.out.println("Application " + applicationId + " has already finished ");
        } else {
            System.out.println("Killing application " + applicationId);
            yarnClient.killApplication(appId);
        }
    }

    /**
     * Moves the application with the given ID to the given queue.
     */
    @Override
    public void moveApplicationAcrossQueues(String applicationId, String queue) throws YarnException, IOException {
        ApplicationId appId = ConverterUtils.toApplicationId(applicationId);
        ApplicationReport appReport = yarnClient.getApplicationReport(appId);
        if (appReport.getYarnApplicationState() == YarnApplicationState.FINISHED
            || appReport.getYarnApplicationState() == YarnApplicationState.KILLED
            || appReport.getYarnApplicationState() == YarnApplicationState.FAILED) {
            System.out.println("Application " + applicationId + " has already finished ");
        } else {
            System.out.println("Moving application " + applicationId + " to queue " + queue);
            yarnClient.moveApplicationAcrossQueues(appId, queue);
            System.out.println("Successfully completed move.");
        }
    }

    /**
     * Prints the application attempt report for an application attempt id.
     *
     * @param applicationAttemptId Application Attempt ID
     * @throws YarnException
     */
    @Override
    public Map<String, Object> getApplicationAttemptReport(String applicationAttemptId) throws YarnException, IOException {
        ApplicationAttemptReport appAttemptReport = yarnClient.getApplicationAttemptReport(ConverterUtils.toApplicationAttemptId(applicationAttemptId));
        Map<String, Object> map = new HashMap<>();
        if (appAttemptReport != null) {
            map.put("applicationAttemptId", appAttemptReport.getApplicationAttemptId());
            map.put("state", appAttemptReport.getYarnApplicationAttemptState());
            map.put("amContainer", appAttemptReport.getAMContainerId().toString());
            map.put("trackingUrl", appAttemptReport.getTrackingUrl());
            map.put("rpcPort", appAttemptReport.getRpcPort());
            map.put("amHost", appAttemptReport.getHost());
            map.put("diagnostics", appAttemptReport.getDiagnostics());
        } else {
            throw new RuntimeException("Application Attempt with id '" + applicationAttemptId + "' doesn't exist in History Server.");
        }
        return map;
    }

    /**
     * Prints the container report for an container id.
     *
     * @param containerId Container ID
     * @throws YarnException
     */
    @Override
    public Map<String, Object> getContainerReport(String containerId) throws YarnException, IOException {
        ContainerReport containerReport = yarnClient.getContainerReport((ConverterUtils.toContainerId(containerId)));
        Map<String, Object> map = new HashMap<>();
        if (containerReport != null) {
            map.put("containerId", containerReport.getContainerId());
            map.put("startTime", containerReport.getCreationTime());
            map.put("finishTime", containerReport.getFinishTime());
            map.put("state", containerReport.getContainerState());
            map.put("logUrl", containerReport.getLogUrl());
            map.put("assignedNode", containerReport.getAssignedNode());
            map.put("diagnostics", containerReport.getDiagnosticsInfo());
        } else {
            throw new RuntimeException("Container with id '" + containerId + "' doesn't exist in History Server.");
        }
        return map;
    }

    @Override
    public List listApplications(Set<String> appTypes, List<String> appStates, boolean allAppStates) throws YarnException, IOException {
        List list = new ArrayList();
        EnumSet<YarnApplicationState> filters = EnumSet.noneOf(YarnApplicationState.class);
        if (allAppStates) {
            for (YarnApplicationState appState : YarnApplicationState.values()) {
                filters.add(appState);
            }
        } else {
            if (appStates.isEmpty()) {
                filters.add(YarnApplicationState.RUNNING);
                filters.add(YarnApplicationState.ACCEPTED);
                filters.add(YarnApplicationState.SUBMITTED);
            }
        }

        List<ApplicationReport> applications = null;

        if (appTypes == null) {
            applications = yarnClient.getApplications(appTypes, filters);
        } else {
            applications = yarnClient.getApplications(filters);
        }

        for (ApplicationReport application : applications) {
            Map map = getApplication(application);
            list.add(map);
        }
        return list;
    }

    @Override
    public Map<String, Object> getApplicationReport(String applicationId) throws YarnException, IOException {
        ApplicationReport application = yarnClient.getApplicationReport(ConverterUtils.toApplicationId(applicationId));
        Map<String, Object> map = new HashMap<>();

        if (application != null) {
            map.put("applicationId", application.getApplicationId().toString());
            map.put("applicationType", application.getApplicationType());
            map.put("name", application.getName());
            map.put("user", application.getUser());
            map.put("queue", application.getQueue());
            map.put("startTime", application.getStartTime());
            map.put("finishTime", application.getFinishTime());
            DecimalFormat formatter = new DecimalFormat("###.##");
            String progress = formatter.format(application.getProgress());
            map.put("progress", progress);
            map.put("yarnApplicationState", application.getYarnApplicationState());
            map.put("finalApplicationStatus", application.getFinalApplicationStatus());
            map.put("trackingUrl", application.getOriginalTrackingUrl());
            map.put("rpcPort", application.getRpcPort());
            map.put("amHost", application.getHost());

            map.put("numUsedContainers", application.getApplicationResourceUsageReport().getNumUsedContainers());
            map.put("numReservedContainers", application.getApplicationResourceUsageReport().getNumReservedContainers());

            map.put("usedResourcesMemory", application.getApplicationResourceUsageReport().getUsedResources().getMemory());
            map.put("reservedResourcesMemory", application.getApplicationResourceUsageReport().getReservedResources().getMemory());
            map.put("neededResourcesMemory", application.getApplicationResourceUsageReport().getNeededResources().getMemory());

            map.put("usedResourcesVcores", application.getApplicationResourceUsageReport().getUsedResources().getVirtualCores());
            map.put("reservedResourcesVcores", application.getApplicationResourceUsageReport().getReservedResources().getVirtualCores());
            map.put("neededResourcesVcores", application.getApplicationResourceUsageReport().getNeededResources().getVirtualCores());

            map.put("memorySeconds", application.getApplicationResourceUsageReport().getMemorySeconds());
            map.put("vcoreSeconds", application.getApplicationResourceUsageReport().getVcoreSeconds());
            map.put("diagnostics", application.getDiagnostics());
        } else {
            throw new RuntimeException("Application with id '" + applicationId + "' doesn't exist in RM.");
        }
        return map;
    }

    /**
     * Lists the application attempts matching the given applicationid
     *
     * @param applicationId Application ID
     * @throws YarnException
     * @throws IOException
     */
    @Override
    public List listApplicationAttempts(String applicationId) throws YarnException, IOException {
        List list = new ArrayList();
        List<ApplicationAttemptReport> appAttemptsReport = yarnClient.getApplicationAttempts(ConverterUtils.toApplicationId(applicationId));
        for (ApplicationAttemptReport appAttemptReport : appAttemptsReport) {
            Map<String, Object> map = new HashMap<>();
            map.put("applicationAttemptId", appAttemptReport.getApplicationAttemptId());
            map.put("yarnApplicationAttemptState", appAttemptReport.getYarnApplicationAttemptState().toString());
            map.put("amContainerId", appAttemptReport.getAMContainerId().toString());
            map.put("trackingUrl", appAttemptReport.getTrackingUrl());
            list.add(map);
        }
        return list;
    }

    /**
     * Lists the containers matching the given application attempts
     *
     * @param appAttemptId Application ID
     * @throws YarnException
     * @throws IOException
     */
    @Override
    public List listContainers(String appAttemptId) throws YarnException, IOException {
        List list = new ArrayList();
        List<ContainerReport> appsReport = yarnClient.getContainers(ConverterUtils.toApplicationAttemptId(appAttemptId));
        for (ContainerReport containerReport : appsReport) {
            Map<String, Object> map = new HashMap<>();

            map.put("containerId", containerReport.getContainerId());
            map.put("startTime", containerReport.getCreationTime());
            map.put("finishTime", containerReport.getFinishTime());
            map.put("containerState", containerReport.getContainerState());
            map.put("assignedNode", containerReport.getAssignedNode());
            map.put("logUrl", containerReport.getLogUrl());
            map.put("diagnostics", containerReport.getDiagnosticsInfo());
            map.put("containerExitStatus", containerReport.getContainerExitStatus());
            map.put("priority", containerReport.getPriority().toString());
            map.put("vcores", containerReport.getAllocatedResource().getVirtualCores());
            map.put("memory", containerReport.getAllocatedResource().getMemory());

            list.add(map);
        }
        return list;
    }

    @Override
    public Map<String, String> getBlockers() {
        return yarnClient.getBlockers();
    }

    @Override
    public List getAllQueues() throws IOException, YarnException {
        List list = new ArrayList();
        List<QueueInfo> queues = yarnClient.getAllQueues();
        for (QueueInfo queue : queues) {
            Map<String, Object> map = new HashMap<>();
            map.put("capacity", queue.getCapacity());
            map.put("currentCapacity", queue.getCurrentCapacity());
            map.put("maximumCapacity", queue.getMaximumCapacity());
            map.put("name", queue.getQueueName());
            map.put("state", queue.getQueueState().toString());
//            for (ApplicationReport applicationReport : queue.getApplications()) {
//                map.put("applicationReport", applicationReport.getApplicationId().toString());
//            }
            list.add(map);
        }
        return list;
    }

    @Override
    public Map<String, Object> getQueueInfo(String queueName) throws IOException, YarnException {
        Map<String, Object> map = new HashMap<>();
        QueueInfo queue = yarnClient.getQueueInfo(queueName);
        map.put("capacity", queue.getCapacity());
        map.put("currentCapacity", queue.getCurrentCapacity());
        map.put("maximumCapacity", queue.getMaximumCapacity());
        map.put("name", queue.getQueueName());
        map.put("state", queue.getQueueState().toString());
        return map;
    }

    @Override
    public List getApplicationsOfQueue(String queueName) throws IOException, YarnException {
        List list = new ArrayList();
        QueueInfo queueInfo = yarnClient.getQueueInfo(queueName);
        List<ApplicationReport> applications = queueInfo.getApplications();
        for (ApplicationReport application : applications) {
            Map map = getApplication(application);
            list.add(map);
        }
        return list;
    }

    @Override
    public Map getApplication(ApplicationReport application) {
        Map<String, Object> map = new HashMap<>();

        DecimalFormat formatter = new DecimalFormat("###.##");
        String progress = formatter.format(application.getProgress());

        map.put("applicationId", application.getApplicationId().toString());
        map.put("applicationType", application.getApplicationType());
        map.put("currentApplicationAtteptId", application.getCurrentApplicationAttemptId().toString());
        map.put("name", application.getName());
        map.put("user", application.getUser());
        map.put("queue", application.getQueue());
        map.put("startTime", application.getStartTime());
        map.put("finishTime", application.getFinishTime());
        map.put("yarnApplicationState", application.getYarnApplicationState().toString());
        map.put("finalApplicationStatus", application.getFinalApplicationStatus().toString());
        map.put("progress", progress);
        map.put("trackingUrl", application.getOriginalTrackingUrl());

        map.put("numUsedContainers", application.getApplicationResourceUsageReport().getNumUsedContainers());
        map.put("numReservedContainers", application.getApplicationResourceUsageReport().getNumReservedContainers());

        map.put("usedResourcesMemory", application.getApplicationResourceUsageReport().getUsedResources().getMemory());
        map.put("reservedResourcesMemory", application.getApplicationResourceUsageReport().getReservedResources().getMemory());
        map.put("neededResourcesMemory", application.getApplicationResourceUsageReport().getNeededResources().getMemory());

        map.put("usedResourcesVcores", application.getApplicationResourceUsageReport().getUsedResources().getVirtualCores());
        map.put("reservedResourcesVcores", application.getApplicationResourceUsageReport().getReservedResources().getVirtualCores());
        map.put("neededResourcesVcores", application.getApplicationResourceUsageReport().getNeededResources().getVirtualCores());

        map.put("memorySeconds", application.getApplicationResourceUsageReport().getMemorySeconds());
        map.put("vcoreSeconds", application.getApplicationResourceUsageReport().getVcoreSeconds());
        return map;
    }

    /**
     * Lists the nodes matching the given node states
     *
     * @param nodeStates Node State
     * @throws YarnException
     * @throws IOException
     */
    @Override
    public List listClusterNodes(Set<NodeState> nodeStates) throws YarnException, IOException {
        List<NodeReport> nodesReport = yarnClient.getNodeReports(nodeStates.toArray(new NodeState[0]));
        List list = new ArrayList();
        for (NodeReport nodeReport : nodesReport) {
            list.add(getNode(nodeReport));
        }
        return list;
    }

    /**
     * Get the node report for node id.
     *
     * @param nodeIdStr Node ID
     * @throws YarnException
     */
    @Override
    public Map<String, Object> getNodeStatus(String nodeIdStr) throws YarnException, IOException {
        NodeId nodeId = ConverterUtils.toNodeId(nodeIdStr);
        List<NodeReport> nodesReport = yarnClient.getNodeReports();
        NodeReport nodeReport = null;
        Map<String, Object> map = new HashMap<>();

        for (NodeReport report : nodesReport) {
            if (!report.getNodeId().equals(nodeId)) {
                continue;
            }
            nodeReport = report;
            map = getNode(nodeReport);
        }
        return map;
    }

    @Override
    public List getNodes() throws YarnException, IOException {
        List<NodeReport> nodesReports = yarnClient.getNodeReports();
        List list = new ArrayList();
        for (NodeReport nodeReport : nodesReports) {
            list.add(getNode(nodeReport));
        }
        return list;
    }

    public Map<String, Object> getNode(NodeReport nodeReport) {
        Map<String, Object> map = new HashMap<>();
        map.put("nodeId", nodeReport.getNodeId().toString());
        map.put("rackName", nodeReport.getRackName());
        map.put("nodeState", nodeReport.getNodeState().toString());
        map.put("httpAddress", nodeReport.getHttpAddress());
        map.put("lastHealthUpdated", formatDate(new Date(nodeReport.getLastHealthReportTime()), "yyyy-MM-dd HH:mm:ss"));
        map.put("healthReport", nodeReport.getHealthReport());
        map.put("numContainers", nodeReport.getNumContainers());

        map.put("usedVcores", (nodeReport.getUsed() == null) ? 0 : (nodeReport.getUsed().getVirtualCores()));
        map.put("capacityVcores", nodeReport.getCapability().getVirtualCores());

        map.put("usedMemory", (nodeReport.getUsed() == null) ? 0 : (nodeReport.getUsed().getMemory()));
        map.put("capacityMemory", nodeReport.getCapability().getMemory());
        return map;
    }

    @Override
    public String getApplicationLog(String applicationId) throws IOException {
        String appOwner = UserGroupInformation.getCurrentUser().getShortUserName();
        return getApplicationLog(applicationId, appOwner);
    }

    @Override
    public String getApplicationLog(String applicationId, String appOwner) throws IOException {
        ApplicationId appId = ConverterUtils.toApplicationId(applicationId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        return dumpContainersLogs(appId, appOwner, null, out) == -1 ? "" : new String(baos.toByteArray(), "UTF-8");
    }

    @Override
    public String getApplicationTaskLog(String jobIdStr, String taskAttemptIdStr) {
        Cluster cluster = null;
        try {
            JobID jobId = JobID.forName(jobIdStr);
            TaskAttemptID taskAttemptId = TaskAttemptID.forName(taskAttemptIdStr);
            cluster = new Cluster(ResourceManagerAgent.configuration);
            LogParams logParams = cluster.getLogParams(jobId, taskAttemptId);
            ApplicationId appId = ConverterUtils.toApplicationId(logParams.getApplicationId());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(baos);
            return dumpContainersLogs(appId, logParams.getOwner(), logParams.getContainerId(), out) == -1 ? "" :
                new String(baos.toByteArray(), "UTF-8");
        } catch (Exception e) {
            System.out.println("could not find log");
        } finally {
            if (cluster != null) {
                try {
                    cluster.close();
                } catch (IOException e) {
                    // nothing to do.
                }
            }
        }

        return "";
    }

    private int dumpContainersLogs(ApplicationId appId, String appOwner, String containerId, PrintStream out) throws IOException {
        Path remoteRootLogDir = new Path(getConf().get(YarnConfiguration.NM_REMOTE_APP_LOG_DIR, YarnConfiguration.DEFAULT_NM_REMOTE_APP_LOG_DIR));
        String user = appOwner;
        String logDirSuffix = LogAggregationUtils.getRemoteNodeLogDirSuffix(getConf());

        // TODO Change this to get a list of files from the LAS.
        Path remoteAppLogDir = LogAggregationUtils.getRemoteAppLogDir(remoteRootLogDir, appId, user, logDirSuffix);
        logger.debug("remoteAppLogDir : {}", remoteAppLogDir);
        RemoteIterator<FileStatus> nodeFiles;
        try {
            Path qualifiedLogDir = FileContext.getFileContext(getConf()).makeQualified(remoteAppLogDir);
            logger.debug("qualifiedLogDir : {}", qualifiedLogDir);
            nodeFiles = FileContext.getFileContext(qualifiedLogDir.toUri(), getConf()).listStatus(remoteAppLogDir);
            logger.debug("nodeFiles : {}", nodeFiles);
        } catch (FileNotFoundException fnf) {
            exceptionLogger.warn("Not found log file : {}", fnf);
            System.out.println("Logs not available at " + remoteAppLogDir.toString());
            System.out.println("Log aggregation has not completed or is not enabled.");
            return -1;
        }
        while (nodeFiles.hasNext()) {
            FileStatus thisNodeFile = nodeFiles.next();
            AggregatedLogFormat.LogReader reader = new AggregatedLogFormat.LogReader(getConf(), new Path(remoteAppLogDir, thisNodeFile.getPath().getName()));

            try {
                DataInputStream valueStream;
                AggregatedLogFormat.LogKey key = new AggregatedLogFormat.LogKey();
                valueStream = reader.next(key);

                while (valueStream != null) {
                    if (containerId == null || key.toString().equals(containerId)) {
                        String containerString = "\n\nContainer: " + key + " on " + thisNodeFile.getPath().getName();
                        out.println(containerString);
                        out.println(StringUtils.repeat("=", containerString.length()));
                        while (true) {
                            try {
                                AggregatedLogFormat.LogReader.readAContainerLogsForALogType(valueStream, out, thisNodeFile.getModificationTime());
                            } catch (EOFException eof) {
                                break;
                            }
                        }

                        // Next container
                        key = new AggregatedLogFormat.LogKey();
                        valueStream = reader.next(key);
                    } else {
                        // Next container
                        key = new AggregatedLogFormat.LogKey();
                        valueStream = reader.next(key);
                    }
                }
            } finally {
                reader.close();
            }
        }
        return 0;
    }

    @Override
    public String getContainersLogs(String appId, String containerId, String nodeId, String jobOwner) throws IOException {
        Path remoteRootLogDir = new Path(getConf().get(YarnConfiguration.NM_REMOTE_APP_LOG_DIR, YarnConfiguration.DEFAULT_NM_REMOTE_APP_LOG_DIR));
        String suffix = LogAggregationUtils.getRemoteNodeLogDirSuffix(getConf());
        Path logPath = LogAggregationUtils.getRemoteNodeLogFileForApp(remoteRootLogDir, ConverterUtils.toApplicationId(appId), jobOwner, ConverterUtils.toNodeId(nodeId), suffix);
        AggregatedLogFormat.LogReader reader;
        try {
            reader = new AggregatedLogFormat.LogReader(getConf(), logPath);
        } catch (FileNotFoundException fnfe) {
            System.out.println("Logs not available at " + logPath.toString());
            System.out.println("Log aggregation has not completed or is not enabled.");
            return "";
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        return dumpAContainerLogs(containerId, reader, out) == -1 ? "" : new String(baos.toByteArray(), "UTF-8");
    }

    @Override
    public List<Map> getRunningMRJobs() throws Exception {
        GetApplicationsRequest request = GetApplicationsRequest.newInstance();
        Configuration conf = this.resourceManager.getConfig();

        RecordFactory recordFactory = RecordFactoryProvider.getRecordFactory(conf);
        Set<String> appStates = new HashSet<>();
        appStates.add("RUNNING");

        request.setApplicationStates(appStates);

        Set<String> applicationTypes = new HashSet<>();
        applicationTypes.add("MAPREDUCE");
        request.setApplicationTypes(applicationTypes);

        List<ApplicationReport> appReports = null;
        List<Map> returnList = new ArrayList<>();

        EnumSet<YarnApplicationState> filters = EnumSet.noneOf(YarnApplicationState.class);
        filters.add(YarnApplicationState.RUNNING);

        try {
            appReports = yarnClient.getApplications(applicationTypes, filters);
            for (ApplicationReport appReport : appReports) {
                Map reportMap = new HashMap();
                try {

                    JobId jobId = TypeConverter.toYarn(TypeConverter.fromYarn(appReport.getApplicationId()));
                    java.net.URL url = new java.net.URL(appReport.getOriginalTrackingUrl());
                    InetSocketAddress inetSocketAddress = new InetSocketAddress(url.getHost(), appReport.getRpcPort());

                    YarnRPC rpc = YarnRPC.create(conf);
                    MRClientProtocol proxy = (MRClientProtocol) rpc.getProxy(MRClientProtocol.class, inetSocketAddress, conf);

                    /*Get Job Report*/
                    GetJobReportRequest getJobReportRequest = recordFactory.newRecordInstance(GetJobReportRequest.class);
                    getJobReportRequest.setJobId(jobId);
                    JobReport jobReport = proxy.getJobReport(getJobReportRequest).getJobReport();

                    /*Get Map Tasks*/
                    GetTaskReportsRequest gtreportsRequest = recordFactory.newRecordInstance(GetTaskReportsRequest.class);
                    gtreportsRequest.setJobId(jobId);
                    gtreportsRequest.setTaskType(TaskType.MAP);
                    List<TaskReport> mapTaskReportList = proxy.getTaskReports(gtreportsRequest).getTaskReportList();

                    /*Get Reduce Tasks*/
                    gtreportsRequest = recordFactory.newRecordInstance(GetTaskReportsRequest.class);
                    gtreportsRequest.setJobId(jobId);
                    gtreportsRequest.setTaskType(TaskType.REDUCE);
                    List<TaskReport> reduceTaskReportList = proxy.getTaskReports(gtreportsRequest).getTaskReportList();

                    reportMap.put("id", jobId.toString());
                    reportMap.put("user", appReport.getUser());
                    reportMap.put("name", appReport.getName());
                    reportMap.put("queue", appReport.getQueue());
                    reportMap.put("mapsTotal", mapTaskReportList.size());
                    reportMap.put("reducesTotal", reduceTaskReportList.size());
                    reportMap.put("state", jobReport.getJobState().toString());
                    reportMap.put("startTime", jobReport.getStartTime());
                    reportMap.put("finishTime", jobReport.getFinishTime());
                    reportMap.put("submitTime", jobReport.getSubmitTime());
                    reportMap.put("trackingUrl", appReport.getTrackingUrl());

                    returnList.add(reportMap);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    continue;
                }
            }
            return returnList;
        } catch (YarnException e) {
            e.printStackTrace();
            throw new ServiceException("Unable to retrieve apps from ResourceManager Agent", e);
        }
    }

    @Override
    public String getJobStatus(String strJobId) {
        String splitJobId[] = strJobId.split("_");

        try {
            ApplicationId applicationId = ApplicationId.newInstance(Long.parseLong(splitJobId[1]), Integer.parseInt(splitJobId[2]));
            ApplicationReport applicationReport = yarnClient.getApplicationReport(applicationId);

            return applicationReport.getYarnApplicationState().toString();
        } catch (Exception ex) {
            return "NOTHING";
        }
    }

    @Override
    public Map getRunningMRJobReport(String strJobId) throws Exception {
        JobID mrJobID = JobID.forName(strJobId);
        JobId jobId = TypeConverter.toYarn(mrJobID);
        String splitJobId[] = strJobId.split("_");
        ApplicationId applicationId = ApplicationId.newInstance(Long.parseLong(splitJobId[1]), Integer.parseInt(splitJobId[2]));
        ApplicationReport appReport = yarnClient.getApplicationReport(applicationId);
        Configuration conf = this.resourceManager.getConfig();
        RecordFactory recordFactory = RecordFactoryProvider.getRecordFactory(conf);

        java.net.URL url = new java.net.URL(appReport.getOriginalTrackingUrl());
        InetSocketAddress inetSocketAddress = new InetSocketAddress(url.getHost(), appReport.getRpcPort());

        YarnRPC rpc = YarnRPC.create(conf);
        MRClientProtocol proxy = (MRClientProtocol) rpc.getProxy(MRClientProtocol.class, inetSocketAddress, conf);

        /*Get Job Report*/
        GetJobReportRequest getJobReportRequest = recordFactory.newRecordInstance(GetJobReportRequest.class);
        getJobReportRequest.setJobId(jobId);
        JobReport jobReport = proxy.getJobReport(getJobReportRequest).getJobReport();

        /*Get Map Tasks*/
        GetTaskReportsRequest gtreportsRequest = recordFactory.newRecordInstance(GetTaskReportsRequest.class);
        gtreportsRequest.setJobId(jobId);
        gtreportsRequest.setTaskType(TaskType.MAP);
        List<TaskReport> mapTaskReportList = proxy.getTaskReports(gtreportsRequest).getTaskReportList();

        int successfulMapAttempts = 0, killedMapAttempts = 0, failedMapAttempts = 0;
        for (TaskReport taskReport : mapTaskReportList) {
            if (taskReport.getTaskState() == TaskState.SUCCEEDED) {
                successfulMapAttempts++;
            } else if (taskReport.getTaskState() == TaskState.KILLED) {
                killedMapAttempts++;
            } else if (taskReport.getTaskState() == TaskState.FAILED) {
                failedMapAttempts++;
            }
        }

        /*Get Reduce Tasks*/
        gtreportsRequest = recordFactory.newRecordInstance(GetTaskReportsRequest.class);
        gtreportsRequest.setJobId(jobId);
        gtreportsRequest.setTaskType(TaskType.REDUCE);
        List<TaskReport> reduceTaskReportList = proxy.getTaskReports(gtreportsRequest).getTaskReportList();

        int successfulReduceAttempts = 0, killedReduceAttempts = 0, failedReduceAttempts = 0;
        for (TaskReport taskReport : reduceTaskReportList) {
            if (taskReport.getTaskState() == TaskState.SUCCEEDED) {
                successfulReduceAttempts++;
            } else if (taskReport.getTaskState() == TaskState.KILLED) {
                killedReduceAttempts++;
            } else if (taskReport.getTaskState() == TaskState.FAILED) {
                failedReduceAttempts++;
            }
        }

        Map reportMap = new HashMap();
        reportMap.put("id", jobId.toString());
        reportMap.put("user", appReport.getUser());
        reportMap.put("name", appReport.getName());
        reportMap.put("queue", appReport.getQueue());
        reportMap.put("mapsTotal", mapTaskReportList.size());
        reportMap.put("reducesTotal", reduceTaskReportList.size());
        reportMap.put("state", jobReport.getJobState().toString());
        reportMap.put("startTime", jobReport.getStartTime());
        reportMap.put("finishTime", jobReport.getFinishTime());
        reportMap.put("submitTime", jobReport.getSubmitTime());
        reportMap.put("trackingUrl", appReport.getTrackingUrl());

        reportMap.put("successfulMapAttempts", successfulMapAttempts);
        reportMap.put("killedMapAttempts", killedMapAttempts);
        reportMap.put("failedMapAttempts", failedMapAttempts);
        reportMap.put("successfulReduceAttempts", successfulReduceAttempts);
        reportMap.put("killedReduceAttempts", killedReduceAttempts);
        reportMap.put("failedReduceAttempts", failedReduceAttempts);

        return reportMap;
    }

    @Override
    public List<Map> getRunningMRTasks(String strJobId) throws Exception {
        List<Map> returnList = new ArrayList<>();
        JobID mrJobID = JobID.forName(strJobId);
        JobId jobId = TypeConverter.toYarn(mrJobID);
        String splitJobId[] = strJobId.split("_");
        ApplicationId applicationId = ApplicationId.newInstance(Long.parseLong(splitJobId[1]), Integer.parseInt(splitJobId[2]));
        ApplicationReport appReport = yarnClient.getApplicationReport(applicationId);
        Configuration conf = this.resourceManager.getConfig();
        RecordFactory recordFactory = RecordFactoryProvider.getRecordFactory(conf);

        java.net.URL url = new java.net.URL(appReport.getOriginalTrackingUrl());
        InetSocketAddress inetSocketAddress = new InetSocketAddress(url.getHost(), appReport.getRpcPort());

        YarnRPC rpc = YarnRPC.create(conf);
        MRClientProtocol proxy = (MRClientProtocol) rpc.getProxy(MRClientProtocol.class, inetSocketAddress, conf);

        /*Get Job Report*/
        GetJobReportRequest getJobReportRequest = recordFactory.newRecordInstance(GetJobReportRequest.class);
        getJobReportRequest.setJobId(jobId);
        JobReport jobReport = proxy.getJobReport(getJobReportRequest).getJobReport();

        /*Get Map Tasks*/
        GetTaskReportsRequest gtreportsRequest = recordFactory.newRecordInstance(GetTaskReportsRequest.class);
        gtreportsRequest.setJobId(jobId);
        gtreportsRequest.setTaskType(TaskType.MAP);
        List<TaskReport> mapTaskReportList = proxy.getTaskReports(gtreportsRequest).getTaskReportList();

        for (TaskReport taskReport : mapTaskReportList) {
            Map taskMap = new HashMap();

            taskMap.put("startTime", taskReport.getStartTime());
            taskMap.put("finishTime", taskReport.getFinishTime());
            taskMap.put("progress", taskReport.getProgress() * 100);
            taskMap.put("id", taskReport.getTaskId().toString());
            taskMap.put("state", taskReport.getTaskState().toString());
            taskMap.put("elapsedTime", taskReport.getFinishTime() - taskReport.getStartTime());
            taskMap.put("type", "MAP");

            returnList.add(taskMap);
        }

        /*Get Reduce Tasks*/
        gtreportsRequest = recordFactory.newRecordInstance(GetTaskReportsRequest.class);
        gtreportsRequest.setJobId(jobId);
        gtreportsRequest.setTaskType(TaskType.REDUCE);
        List<TaskReport> reduceTaskReportList = proxy.getTaskReports(gtreportsRequest).getTaskReportList();

        for (TaskReport taskReport : reduceTaskReportList) {
            Map taskMap = new HashMap();

            taskMap.put("startTime", taskReport.getStartTime());
            taskMap.put("finishTime", taskReport.getFinishTime());
            taskMap.put("progress", taskReport.getProgress() * 100);
            taskMap.put("id", taskReport.getTaskId().toString());
            taskMap.put("state", taskReport.getTaskState().toString());
            taskMap.put("elapsedTime", taskReport.getFinishTime() - taskReport.getStartTime());
            taskMap.put("type", "REDUCE");

            returnList.add(taskMap);
        }

        return returnList;
    }

    private int dumpAContainerLogs(String containerIdStr, AggregatedLogFormat.LogReader reader, PrintStream out) throws IOException {
        DataInputStream valueStream;
        AggregatedLogFormat.LogKey key = new AggregatedLogFormat.LogKey();
        valueStream = reader.next(key);

        while (valueStream != null && !key.toString().equals(containerIdStr)) {
            // Next container
            key = new AggregatedLogFormat.LogKey();
            valueStream = reader.next(key);
        }

        if (valueStream == null) {
            System.out.println("Logs for container " + containerIdStr + " are not present in this log-file.");
            return -1;
        }

        while (true) {
            try {
                AggregatedLogFormat.LogReader.readAContainerLogsForALogType(valueStream, out);
            } catch (EOFException eof) {
                break;
            }
        }
        return 0;
    }

    @Override
    public void destroy() throws Exception {
        yarnClient.close();
    }

    private Configuration getConf() {
        return resourceManager.getConfig();
    }
}
