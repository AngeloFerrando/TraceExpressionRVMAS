package it.unige.dibris.TExpRVMAS.Exception;

@SuppressWarnings("serial")
public class NoMinimalMonitoringSafePartitionFoundException extends DecentralizedPartitionNotFoundException {
	
	public NoMinimalMonitoringSafePartitionFoundException() {
	}

	public NoMinimalMonitoringSafePartitionFoundException(String message) {
		super(message);
	}

	public NoMinimalMonitoringSafePartitionFoundException(Throwable cause) {
		super(cause);
	}

	public NoMinimalMonitoringSafePartitionFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoMinimalMonitoringSafePartitionFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
