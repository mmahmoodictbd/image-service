package com.chumbok.imageservice.exception;

public class IORuntimeException extends RuntimeException {

	public IORuntimeException(Throwable cause) {
		super(cause);
	}

	public IORuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
