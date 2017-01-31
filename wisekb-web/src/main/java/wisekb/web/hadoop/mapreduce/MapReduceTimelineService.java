package wisekb.web.hadoop.mapreduce;

import java.util.List;
import java.util.Map;

/**
 * Created by park on 2016. 10. 19..
 */
public interface MapReduceTimelineService {

    public List<Map> getMRTimeline(String jobId);
}
