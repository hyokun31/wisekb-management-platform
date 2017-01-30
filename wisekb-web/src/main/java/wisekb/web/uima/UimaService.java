package wisekb.web.uima;

import java.util.List;
import java.util.Map;

public interface UimaService {

    public List<Map> getUima(Map params);

    public List<Map> getTimeLine(Map params);

    public List<Map> getUimaDate(Map params);

    public List<Map> getUimaAnnotatorType(Map params);
}
