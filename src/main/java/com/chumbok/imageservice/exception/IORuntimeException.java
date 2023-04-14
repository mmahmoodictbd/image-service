package com.chumbok.imageservice.exception;

public class IORuntimeException extends RuntimeException {

	public IORuntimeException(final String message) {
		super(message);
	}

	public IORuntimeException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
