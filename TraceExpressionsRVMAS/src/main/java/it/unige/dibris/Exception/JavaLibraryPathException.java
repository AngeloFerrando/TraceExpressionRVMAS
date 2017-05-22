package it.unige.dibris.Exception;

@SuppressWarnings("serial")
public class JavaLibraryPathException extends RuntimeException {

	public JavaLibraryPathException() {
	}

	public JavaLibraryPathException(String message) {
		super(message);
	}

	public JavaLibraryPathException(Throwable cause) {
		super(cause);
	}

	public JavaLibraryPathException(String message, Throwable cause) {
		super(message, cause);
	}

	public JavaLibraryPathException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
