package wisekb.web.uima;

import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UimaServiceImpl implements UimaService {

    @Autowired
    UimaRepository uimaRepository;

    private final String CONTENT_STRING = "<div class=\"task-assignment-timeline-content\" " +
            "data-toggle=\"tooltip\" " +
            "data-placement=\"top\" " +
            "data-html=\"true\" " +
            "data-container=\"body\"data-title=\"log_process_id: {}<br>log_annotator_type: {}<br>log_data: {}\">" +
            "<svg class=\"task-assignment-timeline-duration-bar\" height=\"10px\" width=\"100px\">" +
            "<rect class=\"{}\" x=\"0%\" y=\"0px\" height=\"10px\" width=\"100px\"></rect>" +
            "<text x=\"5\" y=\"10\">{} {}</text>" +
            "</svg>";

    @Override
    public List<Map> getUima(Map params) {
        return uimaRepository.getUima(params);
    }

    @Override
    public List<Map> getTimeLine(Map params) {
        List<Map> dataList = dataList = uimaRepository.getUima(params);
        String content = null;
        for (Map data : dataList) {
            content = MessageFormatter.arrayFormat(CONTENT_STRING, new Object[]{
                    data.get("log_process_id"),
                    data.get("log_annotator_type"),
                    data.get("log_data"),
                    "vis-item SUCCEEDED",
                    data.get("log_annotator_type"),
                    data.get("log_process_type"),
            }).getMessage();

            data.put("content", content);
        }

        return dataList;
    }

    @Override
    public List<Map> getUimaDate(Map params) {
        return uimaRepository.getUimaDate(params);
    }

    @Override
    public List<Map> getUimaAnnotatorType(Map params) {
        return uimaRepository.getUimaAnnotatorType(params);
    }
}
