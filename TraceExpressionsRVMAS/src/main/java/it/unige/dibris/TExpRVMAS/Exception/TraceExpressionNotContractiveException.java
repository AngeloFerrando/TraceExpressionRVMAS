package it.unige.dibris.TExpRVMAS.Exception;

public class TraceExpressionNotContractiveException extends RuntimeException {

	public TraceExpressionNotContractiveException() {
	}

	public TraceExpressionNotContractiveException(String message) {
		super(message);
	}

	public TraceExpressionNotContractiveException(Throwable cause) {
		super(cause);
	}

	public TraceExpressionNotContractiveException(String message, Throwable cause) {
		super(message, cause);
	}

	public TraceExpressionNotContractiveException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
