package com.example.addressbook.exception;

/** Thrown when the client sends data we cannot accept (mapped to HTTP 400). */
public class BadRequestException extends RuntimeException {

	public BadRequestException(String message) {
		super(message);
	}
}
