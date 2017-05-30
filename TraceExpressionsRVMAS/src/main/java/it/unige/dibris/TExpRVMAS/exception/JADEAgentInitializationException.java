package it.unige.dibris.TExpRVMAS.exception;

@SuppressWarnings("serial")
public class JADEAgentInitializationException extends RuntimeException {

	public JADEAgentInitializationException() {
	}

	public JADEAgentInitializationException(String message) {
		super(message);
	}

	public JADEAgentInitializationException(Throwable cause) {
		super(cause);
	}

	public JADEAgentInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public JADEAgentInitializationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
