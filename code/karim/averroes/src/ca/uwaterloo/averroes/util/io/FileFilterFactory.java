package ca.uwaterloo.averroes.util.io;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A factory for file filters.
 * 
 * @author karim
 * 
 */
public class FileFilterFactory {

	/**
	 * Create a file filter that recognizes files with a given extension.
	 * 
	 * @param suffix
	 * @return
	 */
	public static FilenameFilter createExtensionFileFilter(final String suffix) {
		return new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(suffix);
			}
		};
	}

	/**
	 * Create a file filter that recognizes directory.
	 * 
	 * @return
	 */
	public static IOFileFilter createDirectoryFileFilter() {
		return new IOFileFilter() {
			@Override
			public boolean accept(File dir) {
				return dir.isDirectory();
			}
		};
	}

	/**
	 * Create a file filter that recognizes Java class files.
	 * 
	 * @return
	 */
	public static IOFileFilter createClassFileFileFilter() {
		return new IOFileFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".class");
			}
		};
	}

	/**
	 * Create a file filter that is the "union" of the all the given file filters.
	 * 
	 * @param fileFilters
	 * @return
	 */
	public static IOFileFilter or(final IOFileFilter... fileFilters) {
		return new IOFileFilter() {
			@Override
			public boolean accept(File file) {
				for (IOFileFilter filter : fileFilters) {
					if (filter.accept(file)) {
						return true;
					}
				}
				return false;
			}
		};
	}
}
