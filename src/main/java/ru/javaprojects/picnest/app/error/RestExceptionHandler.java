package ru.javaprojects.picnest.app.error;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.javaprojects.picnest.common.error.IllegalRequestDataException;
import ru.javaprojects.picnest.common.error.LocalizedException;
import ru.javaprojects.picnest.common.error.NotFoundException;
import ru.javaprojects.picnest.common.util.AppUtil;
import ru.javaprojects.picnest.users.error.UserDisabledException;

import java.util.*;

@RestControllerAdvice(annotations = RestController.class)
@AllArgsConstructor
@Slf4j
public class RestExceptionHandler {
    private final MessageSource messageSource;

    private static final Map<Class<? extends Throwable>, HttpStatus> HTTP_STATUS_MAP = Map.of(
            EntityNotFoundException.class, HttpStatus.CONFLICT,
            DataIntegrityViolationException.class, HttpStatus.CONFLICT,
            IllegalRequestDataException.class, HttpStatus.UNPROCESSABLE_ENTITY,
            ConstraintViolationException.class, HttpStatus.UNPROCESSABLE_ENTITY,
            IllegalArgumentException.class, HttpStatus.UNPROCESSABLE_ENTITY,
            NotFoundException.class, HttpStatus.NOT_FOUND,
            UserDisabledException.class, HttpStatus.FORBIDDEN
    );

    @ExceptionHandler(BindException.class)
    public ProblemDetail bindException(BindException e, HttpServletRequest req) {
        Map<String, String> invalidParams = new LinkedHashMap<>();
        for (ObjectError error : e.getBindingResult().getGlobalErrors()) {
            invalidParams.put(error.getObjectName(), error.getDefaultMessage());
        }
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            invalidParams.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("BindingException: {} at request {}", invalidParams, req.getRequestURI());
        var problemDetail = createProblemDetail(e, HttpStatus.UNPROCESSABLE_ENTITY, e.getLocalizedMessage());
        problemDetail.setProperty("invalid_params", invalidParams);
        return problemDetail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail constraintViolationException(ConstraintViolationException e, HttpServletRequest req) {
        List<String> invalidParams = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .toList();
        log.warn("ConstraintViolationException: {} at request {}", invalidParams, req.getRequestURI());
        var problemDetail = createProblemDetail(e, HttpStatus.UNPROCESSABLE_ENTITY, e.getLocalizedMessage());
        problemDetail.setProperty("invalid_params", invalidParams);
        return problemDetail;
    }

    @ExceptionHandler(LocalizedException.class)
    public ProblemDetail localizedExceptionHandler(LocalizedException e, HttpServletRequest req, Locale locale) {
        log.error("Exception at request {}: {}", req.getRequestURL(), e.toString());
        String message = messageSource.getMessage(e.getMessageCode(), e.getMessageArgs(), locale);
        return createProblemDetail(e, HTTP_STATUS_MAP.get(e.getClass()), message);
    }

    //   https://howtodoinjava.com/spring-mvc/spring-problemdetail-errorresponse/#5-adding-problemdetail-to-custom-exceptions
    @ExceptionHandler(Exception.class)
    public ProblemDetail exception(Exception e, HttpServletRequest req, Locale locale) {
        HttpStatus status = HTTP_STATUS_MAP.get(e.getClass());
        if (status != null) {
            log.error("Exception at request {}: {}", req.getRequestURI(), e.toString());
            String message = e.getLocalizedMessage();
            if (e.getClass().isAssignableFrom(DataIntegrityViolationException.class)) {
                Optional<String> messageCode = DbConstraintMessageCodes.getMessageCode(message);
                if (messageCode.isPresent()) {
                    message = messageSource.getMessage(messageCode.get(), null, locale);
                }
            }
            return createProblemDetail(e, status, message);
        } else {
            Throwable root = AppUtil.getRootCause(e);
            log.error("Exception at request {}: {}", req.getRequestURI(), root);
            return createProblemDetail(e, HttpStatus.INTERNAL_SERVER_ERROR, root.getLocalizedMessage());
        }
    }

    private ProblemDetail createProblemDetail(Exception e, HttpStatusCode statusCode, String detail) {
        return ErrorResponse.builder(e, statusCode, detail).build().getBody();
    }
}
