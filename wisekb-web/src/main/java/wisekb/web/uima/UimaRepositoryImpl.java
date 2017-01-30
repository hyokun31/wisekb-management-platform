package wisekb.web.uima;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import wisekb.shared.core.repository.PersistentRepositoryImpl;

import java.util.List;
import java.util.Map;

@Repository
public class UimaRepositoryImpl extends PersistentRepositoryImpl implements UimaRepository {

    @Override
    public String getNamespace() {
        return this.NAMESPACE;
    }

    @Autowired
    public UimaRepositoryImpl(SqlSessionTemplate sqlSessionTemplate) {
        super.setSqlSessionTemplate(sqlSessionTemplate);
    }


    @Override
    public List<Map> getUima(Map params) {
        return this.getSqlSessionTemplate().selectList(this.getNamespace() + ".selectUimaLog", params);
    }

    @Override
    public List<Map> getUimaDate(Map params) {
        return this.getSqlSessionTemplate().selectList(this.getNamespace() + ".selectCountByDate", params);
    }

    @Override
    public List<Map> getUimaAnnotatorType(Map params) {
        return this.getSqlSessionTemplate().selectList(this.getNamespace() + ".selectAnnotatorTypeList", params);
    }
}
