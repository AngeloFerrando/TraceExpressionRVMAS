package it.unige.dibris.TExpRVMAS.exception;

@SuppressWarnings("serial")
public class JPLInitializationException extends RuntimeException {
	
	public JPLInitializationException() {
	}

	public JPLInitializationException(String message) {
		super(message);
	}

	public JPLInitializationException(Throwable cause) {
		super(cause);
	}

	public JPLInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public JPLInitializationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
