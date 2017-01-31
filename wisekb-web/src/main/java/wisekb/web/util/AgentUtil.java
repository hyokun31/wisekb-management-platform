package wisekb.web.util;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import wisekb.agent.nn.Namenode2AgentService;
import wisekb.agent.rm.ResourceManagerAgentService;
import wisekb.shared.util.zookeeper.ZkHaUtils;
import wisekb.web.remote.RemoteInvocation;

/**
 * Created by Park on 2016. 9. 20..
 */
@Component
public class AgentUtil extends RemoteInvocation {

    public static final String NAMENODE_SERVICE = "namenode2";

    public static final String RESOURCEMANAGER_SERVICE = "resourcemanager";

    @Value("#{hadoop['namenode.host']}")
    private String NAMENODE_HOST;

    @Value("#{hadoop['namenode.agent.port']}")
    private String NAMENODE_AGENT_PORT;

    @Value("#{hadoop['namenode.ha']}")
    private Boolean NAMENODE_HA;

    @Value("#{hadoop['zookeeper.quorum']}")
    private String zookeeperQuorum;

    @Value("#{hadoop['namenode.znode']}")
    private String NAMENODE_ZNODE;

    @Value("#{hadoop['resourcemanager.host']}")
    private String RESOURCEMANAGER_HOST;

    @Value("#{hadoop['resourcemanager.agent.port']}")
    private String RESOURCEMANAGER_AGENT_PORT;


    /**
     * Namenode Agent의 JVM에 배포되어 있는 Namenode Agent의 서비스를 획득한다.
     *
     * @return {@link com.exem.flamingo.management.agent.nn.Namenode2AgentService}
     */
    public Namenode2AgentService getNamenode2AgentService() {
        String remoteServiceUrl;

        if (NAMENODE_HA.booleanValue()) {
            remoteServiceUrl = this.getRemoteServiceUrl(ZkHaUtils.getActiveNnHost(zookeeperQuorum, NAMENODE_ZNODE), Integer.parseInt(NAMENODE_AGENT_PORT), NAMENODE_SERVICE);
        }
        else {
            remoteServiceUrl = this.getRemoteServiceUrl(NAMENODE_HOST, Integer.parseInt(NAMENODE_AGENT_PORT), NAMENODE_SERVICE);
        }

        return this.getRemoteService(remoteServiceUrl, Namenode2AgentService.class);
    }

    public ResourceManagerAgentService getResourceManagerAgentService() {
        String remoteServiceUrl;

        remoteServiceUrl = this.getRemoteServiceUrl(RESOURCEMANAGER_HOST, Integer.parseInt(RESOURCEMANAGER_AGENT_PORT), RESOURCEMANAGER_SERVICE);

        return this.getRemoteService(remoteServiceUrl, ResourceManagerAgentService.class);
    }
}
