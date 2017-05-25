package it.unige.dibris.TExpRVMAS.Exception;

@SuppressWarnings("serial")
public class DecentralizedPartitionNotFoundException extends Exception {
	public DecentralizedPartitionNotFoundException() {
		
	}

	public DecentralizedPartitionNotFoundException(String message) {
		super(message);
	}

	public DecentralizedPartitionNotFoundException(Throwable cause) {
		super(cause);
	}

	public DecentralizedPartitionNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public DecentralizedPartitionNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
