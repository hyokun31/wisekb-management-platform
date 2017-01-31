package wisekb.web.hadoop.resourcemanager;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import wisekb.shared.core.repository.PersistentRepositoryImpl;

import java.util.List;
import java.util.Map;

/**
 * Created by park on 2016. 11. 1..
 */
@Repository
public class ResourceManagerRepositoryImpl extends PersistentRepositoryImpl implements ResourceManagerRepository {

    @Autowired
    public ResourceManagerRepositoryImpl (SqlSessionTemplate sqlSessionTemplate) {
        super.setSqlSessionTemplate(sqlSessionTemplate);
    }

    @Override
    public String getNamespace() {
        return this.NAMESPACE;
    }

    @Override
    public List<Map> selectClusterMetrics() {
        return this.getSqlSessionTemplate().selectList(this.getNamespace() + ".selectClusterMetrics");
    }
}
