package wisekb.web.oozie.dashboard;

import java.util.List;
import java.util.Map;

public interface DashBoardTimelineService {

    List<Map> getTimeline(Map params);
}
