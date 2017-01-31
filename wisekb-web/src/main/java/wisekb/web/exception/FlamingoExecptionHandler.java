package wisekb.web.exception;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import wisekb.shared.core.exception.ServiceException;
import wisekb.shared.core.rest.Response;

/**
 * Created by park on 2016. 12. 2..
 */
@ControllerAdvice
public class FlamingoExecptionHandler {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(FlamingoExecptionHandler.class);

    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    @ResponseBody
    public Response handleServiceException(ServiceException ex) {
        Response response = new Response();

        response.setSuccess(false);
        Throwable cause = ex.getCause();
        if (cause != null) {
            response.getError().setMessage(ex.getCause().getMessage());
            response.getError().setCause(ex.getCause().toString());
        } else {
            response.getError().setMessage(ex.toString());
        }
        response.getError().setException(ex.toString());

        logger.debug("Exception Catch ", ex);

        return response;
    }
}