package org.exem.flamingo.web.uima;

import java.util.List;
import java.util.Map;

/**
 * Created by user on 2016. 12. 28..
 */
public interface UimaRepository {

    public static final String NAMESPACE = UimaRepository.class.getName();

    public List<Map> getUima(Map params);

    public List<Map> getUimaDate(Map params);

    public List<Map> getUimaAnnotatorType(Map params);
}
