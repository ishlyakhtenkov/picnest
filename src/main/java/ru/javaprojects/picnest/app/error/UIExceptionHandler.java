package ru.javaprojects.picnest.app.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.javaprojects.picnest.app.AuthUser;
import ru.javaprojects.picnest.common.error.IllegalRequestDataException;
import ru.javaprojects.picnest.common.error.LocalizedException;
import ru.javaprojects.picnest.common.util.AppUtil;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@ControllerAdvice
@AllArgsConstructor
@Slf4j
public class UIExceptionHandler {
    private final MessageSource messageSource;

    //https://stackoverflow.com/questions/45080119/spring-rest-exception-handling-fileuploadbasesizelimitexceededexception
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public @ResponseBody ProblemDetail maxUploadSizeException(HttpServletRequest req, MaxUploadSizeExceededException e) {
        log.warn("MaxUploadSizeExceededException at request {}: {}", req.getRequestURI(), e.toString());
        return ErrorResponse.builder(e, HttpStatus.UNPROCESSABLE_ENTITY, AppUtil.getRootCause(e).getLocalizedMessage())
                .build().getBody();
    }

    @ExceptionHandler(LocalizedException.class)
    public ModelAndView localizedExceptionHandler(HttpServletRequest req, LocalizedException e, Locale locale) {
        log.error("Exception at request {}: {}", req.getRequestURL(), e.toString());
        String message = messageSource.getMessage(e.getMessageCode(), e.getMessageArgs(), locale);
        return createExceptionModelAndView(e, message, locale);
    }

    @ExceptionHandler(ClientAbortException.class)
    public void clientAbortExceptionHandler(HttpServletRequest req, Exception e) {
        log.error("Exception at request {}: {}", req.getRequestURL(), e.toString());
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception e, Locale locale) {
        log.error("Exception at request {}: {}", req.getRequestURL(), e.toString());
        if (e instanceof ClientAbortException) {
            throw new IllegalRequestDataException("client abort", "picture.stream-failed", null);
        }
        String message = messageSource.getMessage("error.unable-to-process", null, locale);
        if (e.getClass().isAssignableFrom(DataIntegrityViolationException.class)) {
            Optional<String> messageCode = DbConstraintMessageCodes.getMessageCode(message);
            if (messageCode.isPresent()) {
                message = messageSource.getMessage(messageCode.get(), null, locale);
            }
        }
        return createExceptionModelAndView(e, message, locale);
    }

    private ModelAndView createExceptionModelAndView(Exception e, String message, Locale locale) {
        ModelAndView mav;
        if (e.getClass() == NoResourceFoundException.class || e.getClass() == HttpRequestMethodNotSupportedException.class) {
            mav = new ModelAndView("error/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
        } else {
            mav = new ModelAndView("error/exception",
                    Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "reasonPhrase", messageSource.getMessage("error.internal-server-error", null, locale),
                            "message", message));
            mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        mav.addObject("authUser", AuthUser.safeGet() != null ? AuthUser.authUser() : null);
        return mav;
    }
}
