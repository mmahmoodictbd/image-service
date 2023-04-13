package com.chumbok.imageservice.exception.handler;

import com.chumbok.imageservice.dto.ErrorResponse;
import com.chumbok.imageservice.exception.IORuntimeException;
import com.chumbok.imageservice.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.core.exception.SdkException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestControllerAdvice
public class ApplicationExceptionHandler extends BaseExceptionHandler {

	public ApplicationExceptionHandler() {
		super(log);
	}

	@ResponseStatus(NOT_FOUND)
	@ExceptionHandler(NotFoundException.class)
	public ErrorResponse handleNotFoundException(final NotFoundException ex) {
		return new ErrorResponse("NOT_FOUND", "Could not found requested resource.");
	}

	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ExceptionHandler({SdkException.class, IORuntimeException.class})
	public ErrorResponse handleSdkException(final RuntimeException ex) {
		return new ErrorResponse("INTERNAL_SERVER_ERROR", "Could not access/connect with the URL.");
	}
}
