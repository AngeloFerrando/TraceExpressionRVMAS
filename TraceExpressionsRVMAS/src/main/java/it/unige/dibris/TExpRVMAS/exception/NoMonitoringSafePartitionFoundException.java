package it.unige.dibris.TExpRVMAS.exception;

@SuppressWarnings("serial")
public class NoMonitoringSafePartitionFoundException extends DecentralizedPartitionNotFoundException {

	public NoMonitoringSafePartitionFoundException() {
	}

	public NoMonitoringSafePartitionFoundException(String message) {
		super(message);
	}

	public NoMonitoringSafePartitionFoundException(Throwable cause) {
		super(cause);
	}

	public NoMonitoringSafePartitionFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoMonitoringSafePartitionFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
	
