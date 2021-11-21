package de.pfannekuchen.forgenogradle.exceptions;

public class FilesystemException extends RuntimeException {

	private static final long serialVersionUID = -3650878044498924807L;

	public FilesystemException(String msg, Throwable t) {
		super(msg, t);
	}
	
}
