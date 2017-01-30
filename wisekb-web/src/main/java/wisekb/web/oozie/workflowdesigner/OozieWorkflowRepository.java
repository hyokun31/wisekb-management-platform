package wisekb.web.oozie.workflowdesigner;

import java.util.List;
import java.util.Map;

public interface OozieWorkflowRepository {
    public static final String NAMESPACE = OozieWorkflowRepository.class.getName();

    Map selectTreeId(String jobId);

    List<Map> listWorkflows();

    Map getRecentWorkflow();

    void insertWorkflow(Map param);

    void updateWorkflow(Map param);

    void deleteWorkflow(long id);
}
