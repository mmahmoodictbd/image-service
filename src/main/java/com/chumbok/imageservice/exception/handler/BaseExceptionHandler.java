package com.chumbok.imageservice.exception.handler;

import com.chumbok.imageservice.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
public abstract class BaseExceptionHandler {

	private final Logger log;

	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Throwable.class)
	public ErrorResponse handleAllUnhandledException(final Throwable ex) {
		log.error("Unexpected error", ex);
		return new ErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected internal server error occurred");
	}

	@ResponseStatus(NOT_FOUND)
	@ExceptionHandler({NoHandlerFoundException.class, MissingServletRequestParameterException.class})
	public ErrorResponse handleNoHandlerFoundException(final Throwable ex) {
		log.error("Unexpected error", ex);
		return new ErrorResponse("NOT_FOUND", ex.getMessage());
	}

	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
	public ErrorResponse handleMethodArgumentNotValidException(final Throwable ex) {
		log.error("Invalid request", ex);
		return new ErrorResponse("BAD_REQUEST", ex.getMessage());
	}
}
