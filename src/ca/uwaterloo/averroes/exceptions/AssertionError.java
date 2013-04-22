package ca.uwaterloo.averroes.exceptions;

/**
 * An assertion error is an error that is thrown when an assertion fails.
 * 
 * @author karim
 * 
 */
public class AssertionError extends Error {

	private static final long serialVersionUID = 7356269331503470943L;

	/**
	 * Construct a new assertion error
	 */
	public AssertionError() {
		super();
	}

	/**
	 * Construct a new assertion error with the given error message.
	 * 
	 * @param message
	 */
	public AssertionError(String message) {
		super(message);
	}
}
