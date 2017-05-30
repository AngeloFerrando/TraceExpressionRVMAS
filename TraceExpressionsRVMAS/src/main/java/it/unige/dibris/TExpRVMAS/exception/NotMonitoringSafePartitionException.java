package it.unige.dibris.TExpRVMAS.exception;

@SuppressWarnings("serial")
public class NotMonitoringSafePartitionException extends RuntimeException {

	public NotMonitoringSafePartitionException() {
	}

	public NotMonitoringSafePartitionException(String message) {
		super(message);
	}

	public NotMonitoringSafePartitionException(Throwable cause) {
		super(cause);
	}

	public NotMonitoringSafePartitionException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotMonitoringSafePartitionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
