package com.chumbok.imageservice.exception.handler;


import com.chumbok.imageservice.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * BaseExceptionHandler provides -
 * 1. Handles any unknown error/exception.
 * 2. ErrorResponse, DTO containing error information.
 */

@RequiredArgsConstructor
public abstract class BaseExceptionHandler {

	private final Logger log;

	/**
	 * Catch any unknown error/exception and provide readable response to end-user.
	 *
	 * @param ex Throwable object for error/exception.
	 * @return the error response DTO {@link ErrorResponse}
	 */
	@ResponseBody
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Throwable.class)
	public ErrorResponse handleThrowable(final Throwable ex) {
		log.error("Unexpected error", ex);
		return new ErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected internal server error occurred");
	}

	@ResponseBody
	@ResponseStatus(NOT_FOUND)
	@ExceptionHandler(NoHandlerFoundException.class)
	public void handleNoHandlerFoundException(final Throwable ex) {
		log.error("Unexpected error", ex);
	}

	@ResponseBody
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public void handleMethodArgumentNotValidException(final Throwable ex) {
		log.error("Unexpected error", ex);
	}
}
