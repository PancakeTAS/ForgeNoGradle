package de.pfannekuchen.forgenogradle.exceptions;

public class ReflectionException extends RuntimeException {

	private static final long serialVersionUID = -3650878044498924807L;

	public ReflectionException(String msg, Throwable t) {
		super(msg, t);
	}
	
}
