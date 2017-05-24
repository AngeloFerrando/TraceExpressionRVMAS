package it.unige.dibris.TExpRVMAS.Exception;

@SuppressWarnings("serial")
public class EnvironmentVariableNotDefined extends RuntimeException {

	public EnvironmentVariableNotDefined() {
		
	}

	public EnvironmentVariableNotDefined(String message) {
		super(message);
	}

	public EnvironmentVariableNotDefined(Throwable cause) {
		super(cause);
	}

	public EnvironmentVariableNotDefined(String message, Throwable cause) {
		super(message, cause);
	}

	public EnvironmentVariableNotDefined(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
