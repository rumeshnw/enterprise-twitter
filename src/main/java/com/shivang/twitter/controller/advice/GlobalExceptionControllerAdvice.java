package com.shivang.twitter.controller.advice;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.shivang.twitter.model.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionControllerAdvice.class);

    @ExceptionHandler(CustomException.class)
    public ResponseEntity handleCustomException(CustomException ex, WebRequest request) {
        return generateErrorResponse(request, ex.getHttpStatus(), ex);
    }

    @ExceptionHandler(HystrixRuntimeException.class)
    public ResponseEntity handleHystrixException(HystrixRuntimeException ex, WebRequest request) {

        Throwable t = ex.getCause();
        if (t instanceof CustomException) {
            CustomException appException = (CustomException) t;
            return handleCustomException(appException, request);
        }
        return generateErrorResponse(request, HttpStatus.SERVICE_UNAVAILABLE, ex);
    }

    private ResponseEntity generateErrorResponse(WebRequest request, HttpStatus status, Exception ex) {
        request.setAttribute("javax.servlet.error.status_code", status.value(), RequestAttributes.SCOPE_REQUEST);
        request.setAttribute("javax.servlet.error.exception", ex, RequestAttributes.SCOPE_REQUEST);
        ErrorAttributes errorAttributes = new DefaultErrorAttributes();
        Map<String, Object> body = errorAttributes.getErrorAttributes(request, false);
        return new ResponseEntity(body, status);
    }

}