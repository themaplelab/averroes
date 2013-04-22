package ca.uwaterloo.averroes.exceptions;

/**
 * An Averroes exception is thrown whenever Averroes fails to complete any of its tasks.
 * 
 * @author karim
 * 
 */
public class AverroesException extends Exception {

	private static final long serialVersionUID = 7398074375006931772L;

	/**
	 * Construct a new Averroes exception and initialize it with an error message and the cause of the exception.
	 * 
	 * @param message
	 * @param cause
	 */
	public AverroesException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Construct a new Averroes exception with the given error message.
	 * 
	 * @param message
	 */
	public AverroesException(String message) {
		super(message);
	}
}
