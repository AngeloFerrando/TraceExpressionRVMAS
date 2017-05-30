package it.unige.dibris.TExpRVMAS.exception;

@SuppressWarnings("serial")
public class TraceExpressionFileFormatException extends RuntimeException {

	public TraceExpressionFileFormatException() {
	}

	public TraceExpressionFileFormatException(String message) {
		super(message);
	}

	public TraceExpressionFileFormatException(Throwable cause) {
		super(cause);
	}

	public TraceExpressionFileFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public TraceExpressionFileFormatException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
