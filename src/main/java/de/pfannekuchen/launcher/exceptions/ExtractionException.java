package de.pfannekuchen.launcher.exceptions;

public class ExtractionException extends RuntimeException {

	private static final long serialVersionUID = 8654247928929688843L;

	public ExtractionException(String msg, Throwable t) {
		super(msg, t);
	}
	
}
