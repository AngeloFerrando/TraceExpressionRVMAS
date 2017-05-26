package it.unige.dibris.TExpRVMAS.Exception;

@SuppressWarnings("serial")
public class PrologPredicateFailedException extends RuntimeException{

	public PrologPredicateFailedException() {
	}

	public PrologPredicateFailedException(String message) {
		super(message);
	}

	public PrologPredicateFailedException(Throwable cause) {
		super(cause);
	}

	public PrologPredicateFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public PrologPredicateFailedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}