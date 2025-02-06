package org.yx.hoststack.center.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;

import static org.yx.hoststack.center.common.enums.SysCode.*;


/**
 * @author all
 */
@ControllerAdvice
@Order(990)
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BindException.class)
    @ResponseBody
    public R<Object> bindException(BindException e, HttpServletResponse response, HttpServletRequest request) {

        BindingResult bindingResult = e.getBindingResult();
        StringBuilder errorMessages = new StringBuilder();

        // Collect all field errors into a single string
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            if (!errorMessages.isEmpty()) {
                errorMessages.append("; ");
            }
            errorMessages.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage());
        }
        KvLogger.instance(e.getClass())
                .p(LogFieldConstants.EVENT, "BindException")
                .p(LogFieldConstants.ACTION, "BindException")
                .p(LogFieldConstants.ReqUrl, request.getRequestURL())
                .p(LogFieldConstants.ERR_CODE, x00000400.getValue())
                .p(LogFieldConstants.Code, x00000400.getValue())
                .p(LogFieldConstants.ERR_MSG, e.getMessage())
                .p(LogFieldConstants.Success, false)
                .p(LogFieldConstants.Alarm, 1)
                .d();
        return R.failed(x00000400.getValue(), String.valueOf(errorMessages));
    }


    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    @ResponseBody
    public R<Object> missingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletResponse response, HttpServletRequest request) {
        String error = "Parameter '" + e.getParameterName() + "' is missing.";
        KvLogger.instance(e.getClass())
                .p(LogFieldConstants.EVENT, "MissingServletRequestParameterException")
                .p(LogFieldConstants.ACTION, "MissingServletRequestParameterException")
                .p(LogFieldConstants.ReqUrl, request.getRequestURL())
                .p(LogFieldConstants.ERR_CODE, x00000400.getValue())
                .p(LogFieldConstants.Code, x00000400.getValue())
                .p(LogFieldConstants.ERR_MSG, e.getMessage())
                .p(LogFieldConstants.Success, false)
                .p(LogFieldConstants.Alarm, 1)
                .d();
        return R.failed(x00000400.getValue(), error);
    }

    @ExceptionHandler(value = BadSqlGrammarException.class)
    @ResponseBody
    public R<Object> badSqlGrammarException(BadSqlGrammarException e, HttpServletResponse response, HttpServletRequest request) {
        KvLogger.instance(e.getClass())
                .p(LogFieldConstants.EVENT, "BadSqlGrammarException")
                .p(LogFieldConstants.ACTION, "BadSqlGrammarException")
                .p(LogFieldConstants.ReqUrl, request.getRequestURL())
                .p(LogFieldConstants.ERR_CODE, x00000501.getValue())
                .p(LogFieldConstants.Code, x00000501.getValue())
                .p(LogFieldConstants.ERR_MSG, e.getMessage())
                .p(LogFieldConstants.Success, false)
                .p(LogFieldConstants.Alarm, 1)
                .e(e);
        return R.failed(x00000501.getValue(), x00000501.getMsg());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public R<Object> handleException(Exception e, HttpServletResponse response, HttpServletRequest request) {
        KvLogger.instance(e.getClass())
                .p(LogFieldConstants.EVENT, "Exception")
                .p(LogFieldConstants.ACTION, "Exception")
                .p(LogFieldConstants.ReqUrl, request.getRequestURL())
                .p(LogFieldConstants.ERR_CODE, x00000500.getValue())
                .p(LogFieldConstants.Code, x00000500.getValue())
                .p(LogFieldConstants.ERR_MSG, e.getMessage())
                .p(LogFieldConstants.Success, false)
                .p(LogFieldConstants.Alarm, 1)
                .e(e);
        return R.failed(x00000500.getValue(), "An unexpected error occurred: " + e.getMessage());
    }
}
