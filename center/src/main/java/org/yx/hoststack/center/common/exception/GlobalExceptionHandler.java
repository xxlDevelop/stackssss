package org.yx.hoststack.center.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.yx.lib.utils.util.R;

import static org.yx.hoststack.center.common.enums.SysCode.x00000400;


@ControllerAdvice
@Order(990)
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BindException.class)
    @ResponseBody
    public R<Object> BindException(BindException e, HttpServletResponse response, HttpServletRequest request) {

        BindingResult bindingResult = e.getBindingResult();
        StringBuilder errorMessages = new StringBuilder();

        // Collect all field errors into a single string
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            if (!errorMessages.isEmpty()) {
                errorMessages.append("; ");
            }
            errorMessages.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage());
        }
        return R.failed(x00000400.getValue(), String.valueOf(errorMessages));
    }


    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    @ResponseBody
    public R<Object> MissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String error = "Parameter '" + e.getParameterName() + "' is missing.";
        return R.failed(x00000400.getValue(), error);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public R<Object> handleException(Exception e) {
        return R.failed(x00000400.getValue(), "An unexpected error occurred: " + e.getMessage());
    }
}
