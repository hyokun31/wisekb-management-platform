package wisekb.web.hadoop.resourcemanager;

import java.util.List;
import java.util.Map;

/**
 * Created by park on 2016. 11. 1..
 */
public interface ResourceManagerRepository {

    public static final String NAMESPACE = ResourceManagerRepository.class.getName();

    public List<Map> selectClusterMetrics();
}
