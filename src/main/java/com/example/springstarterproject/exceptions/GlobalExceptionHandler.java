package com.example.springstarterproject.exceptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse errorResponse = new ErrorResponse(
                "NOT FOUND",
                e.getMessage(),
                status.value(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        );
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse errorResponse = new ErrorResponse(
                "BAD REQUEST",
                e.getMessage(),
                status.value(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        );
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException e) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ErrorResponse errorResponse = new ErrorResponse(
                "FORBIDDEN",
                e.getMessage(),
                status.value(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        );
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorResponse errorResponse = new ErrorResponse(
                "UNAUTHORIZED",
                e.getMessage(),
                status.value(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        );
        return ResponseEntity.status(status).body(errorResponse);
    }

}
