package wisekb.web.hadoop.mapreduce;

import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by park on 2016. 10. 19..
 */
@Service
public class MapReduceTimelineServiceImpl implements MapReduceTimelineService {

    private final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";

    private final String COLOR_CSS_SKY = "scheduler-delay-proportion";

    private final String COLOR_CSS_ORANGE = "shuffle-read-time-proportion";

    private final String CONTENT_STRING = "<div class=\"task-assignment-timeline-content\" " +
            "data-toggle=\"tooltip\" " +
            "data-placement=\"top\" " +
            "data-html=\"true\" " +
            "data-container=\"body\"data-title=\"State: {}<br>Start Time: {}<br>Finish Time: {}<br>ID: {}\">" +
            "<svg class=\"task-assignment-timeline-duration-bar\">" +
            "<rect class=\"{}\" x=\"0%\" y=\"0px\" height=\"26px\" width=\"100%\"></rect>" +
            "</svg>";

    @Autowired
    HistoryServerRemoteService historyServerRemoteService;

    @Override
    public List<Map> getMRTimeline(String jobId) {

        Map taskMap = historyServerRemoteService.getTasks(jobId);

        Map tasksMap = (Map) taskMap.get("tasks");
        List<Map> taskList = (List<Map>) tasksMap.get("task");
        Map timeline;
        List<Map> timelineList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATEFORMAT);
        String content, css;

        Collections.sort(taskList, new KeyValueComparator());

        for (Map task : taskList) {
            timeline = new HashMap();

            if (Long.parseLong(task.get("startTime").toString()) < 0L) {
                timeline.put("start", task.get("finishTime"));
            }
            else {
                timeline.put("start", task.get("startTime"));
            }

            timeline.put("end", task.get("finishTime"));
            timeline.put("className", "task task-assignment-timeline-object");
            timeline.put("group", task.get("type"));
            timeline.put("taskId", task.get("id"));

            if (task.get("type").equals("MAP")) {
                css = COLOR_CSS_SKY;
            }
            else {
                css = COLOR_CSS_ORANGE;
            }

            content = MessageFormatter.arrayFormat(CONTENT_STRING, new Object[]{
                    task.get("state"),
                    simpleDateFormat.format(task.get("startTime")),
                    simpleDateFormat.format(task.get("finishTime")),
                    task.get("id"),
                    css
            }).getMessage();

            timeline.put("content", content);

            timelineList.add(timeline);
        }

        return timelineList;
    }

    private class KeyValueComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Map map1 = (Map) o1;
            Map map2 = (Map) o2;

            return map1.get("id").toString().compareTo(map2.get("id").toString());
        }
    }
}

