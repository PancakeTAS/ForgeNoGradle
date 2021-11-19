package de.pfannekuchen.launcher.exceptions;

public class ConnectionException extends RuntimeException {

	private static final long serialVersionUID = -3650878044498924807L;

	public ConnectionException(String msg, Throwable t) {
		super(msg, t);
	}
	
}
