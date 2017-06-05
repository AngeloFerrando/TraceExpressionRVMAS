package it.unige.dibris.TExpRVMAS.exception;

@SuppressWarnings("serial")
public class TraceExpressionNeitherAtomicNorAsyncEventTypesException extends RuntimeException {
	
	public TraceExpressionNeitherAtomicNorAsyncEventTypesException() {
	}

	public TraceExpressionNeitherAtomicNorAsyncEventTypesException(String message) {
		super(message);
	}

	public TraceExpressionNeitherAtomicNorAsyncEventTypesException(Throwable cause) {
		super(cause);
	}

	public TraceExpressionNeitherAtomicNorAsyncEventTypesException(String message, Throwable cause) {
		super(message, cause);
	}

	public TraceExpressionNeitherAtomicNorAsyncEventTypesException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
