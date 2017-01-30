package org.exem.flamingo.web.uima;

import org.exem.flamingo.shared.core.exception.ServiceException;
import org.exem.flamingo.shared.core.rest.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by user on 2016. 12. 28..
 */
@RestController
@RequestMapping("/uima")
public class UimaController {

    @Autowired
    UimaService uimaService;

    @RequestMapping("/getUimaLog")
    public Response getUimaLog(@RequestParam Map params) {
        Response response = new Response();
        try {
            response.getList().addAll(uimaService.getUima(params));
            response.setTotal(response.getList().size());

            return response;
        } catch (Exception ex) {
            throw new ServiceException("로그 정보를 불러오는 중 오류가 발생하였습니다.", ex);
        }
    }

    @RequestMapping("/getTimeline")
    public Response getTimeline(@RequestParam Map params) {
        Response response = new Response();
        try {
            response.getList().addAll(uimaService.getTimeLine(params));
            response.setTotal(response.getList().size());

            return response;
        } catch (Exception ex) {
            throw new ServiceException("로그 정보를 불러오는 중 오류가 발생하였습니다.", ex);
        }
    }

    @RequestMapping("/getCountByDate")
    public Response getUimaLogDate(@RequestParam Map params) {
        Response response = new Response();
        try {
            response.getList().addAll(uimaService.getUimaDate(params));
            response.setTotal(response.getList().size());

            return response;
        } catch (Exception ex) {
            throw new ServiceException("로그 정보를 불러오는 중 오류가 발생하였습니다.", ex);
        }
    }

    @RequestMapping("/getAnnotatorType")
    public Response getUimaLogAnnotatorType(@RequestParam Map params) {
        Response response = new Response();
        try {
            response.getList().addAll(uimaService.getUimaAnnotatorType(params));
            response.setTotal(response.getList().size());

            return response;
        } catch (Exception ex) {
            throw new ServiceException("로그 정보를 불러오는 중 오류가 발생하였습니다.", ex);
        }
    }
}
