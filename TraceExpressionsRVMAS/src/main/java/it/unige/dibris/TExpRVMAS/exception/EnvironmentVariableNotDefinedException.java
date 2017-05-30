package it.unige.dibris.TExpRVMAS.exception;

@SuppressWarnings("serial")
public class EnvironmentVariableNotDefinedException extends RuntimeException {

	public EnvironmentVariableNotDefinedException() {
		
	}

	public EnvironmentVariableNotDefinedException(String message) {
		super(message);
	}

	public EnvironmentVariableNotDefinedException(Throwable cause) {
		super(cause);
	}

	public EnvironmentVariableNotDefinedException(String message, Throwable cause) {
		super(message, cause);
	}

	public EnvironmentVariableNotDefinedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
