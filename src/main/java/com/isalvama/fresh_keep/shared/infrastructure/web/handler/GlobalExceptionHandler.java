package com.isalvama.fresh_keep.shared.infrastructure.web.handler;

import com.isalvama.fresh_keep.modules.account.domain.exception.ForbiddenException;
import com.isalvama.fresh_keep.modules.account.domain.exception.UnauthorizedException;
import com.isalvama.fresh_keep.shared.application.exception.ApplicationException;
import com.isalvama.fresh_keep.shared.domain.exception.ConflictException;
import com.isalvama.fresh_keep.shared.domain.exception.DomainException;
import com.isalvama.fresh_keep.shared.infrastructure.exception.*;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(InfrastructureException.class)
    public ProblemDetail handleInfrastructureException(InfrastructureException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Server Error");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ResponseBody
    @ExceptionHandler(ApplicationException.class)
    public ProblemDetail handleApplicationException(ApplicationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Application Server Error");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ResponseBody
    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbiddenException(ForbiddenException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problemDetail.setTitle("Forbidden Request");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ResponseBody
    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorizedException(UnauthorizedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problemDetail.setTitle("Unauthorized Error");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ResponseBody
    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleEntityConflictException(ConflictException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("Conflict Error");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ResponseBody
    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Business Rule Error");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ResponseBody
    @ExceptionHandler(AiUnprocessableInputException.class)
    public ProblemDetail handleAiUnprocessableInputException(AiUnprocessableInputException ex) {
        log.warn("AiUnprocessableInputException: message='{}', cause='{}'", ex.getMessage(), ex.getCause() != null ? ex.getCause().getMessage() : null);
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problemDetail.setTitle("Unprocessable Ticket Data Error");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ResponseBody
    @ExceptionHandler(AiRateLimitedException.class)
    public ProblemDetail handleAiRateLimitedException(AiRateLimitedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS);
        problemDetail.setTitle("AI Rate Limit Exceedance");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ResponseBody
    @ExceptionHandler(TicketProcessingException.class)
    public ProblemDetail handleTicketProcessingException(TicketProcessingException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Internal AI Server Error");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ResponseBody
    @ExceptionHandler(AiRetryableException.class)
    public ProblemDetail handleAiRetryableException(AiRetryableException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("AI Server Error");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation Error In Body Data");
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    @ResponseBody
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation Error in Parameter");
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> errors.put(
                violation.getPropertyPath().toString(), violation.getMessage()
        ));
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleEmptyBody(HttpMessageNotReadableException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Required request body is missing or malformed");
        pd.setTitle("Message Not Readable");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Type Mismatch Error");
        problemDetail.setDetail(String.format("The parameter '%s' with value '%s' could not be converted to type '%s'",
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName()));
        return problemDetail;
    }
}


