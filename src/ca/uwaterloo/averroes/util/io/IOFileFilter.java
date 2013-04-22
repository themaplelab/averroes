package ca.uwaterloo.averroes.util.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * An IO filter for files and directories.
 * 
 * @author karim
 * 
 */
public class IOFileFilter implements FileFilter, FilenameFilter {

	@Override
	public boolean accept(File dir, String name) {
		return accept(new File(dir, name));
	}

	@Override
	public boolean accept(File file) {
		return accept(file.getParentFile(), file.getName());
	}
}