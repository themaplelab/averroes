package ca.uwaterloo.averroes.util.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * A resource is something to which we can open an InputStream 1 or more times.
 * 
 * Similar to FoundFile in soot.SourceLocator, which is not accessible.
 */
public interface Resource {
	public InputStream open() throws IOException;
}
