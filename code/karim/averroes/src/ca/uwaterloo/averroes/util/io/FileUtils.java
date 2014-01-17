/**
 * 
 */
package ca.uwaterloo.averroes.util.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.uwaterloo.averroes.callgraph.CallGraphDiffOptions;
import ca.uwaterloo.averroes.callgraph.CallGraphSource;
import ca.uwaterloo.averroes.properties.AverroesProperties;

/**
 * Utility class for file-related operations.
 * 
 * @author karim
 * 
 */
public class FileUtils {

	/**
	 * Check if a directory is valid.
	 * 
	 * @param directory
	 * @return
	 */
	public static boolean isValidDirectory(String directory) {
		return new File(directory).isDirectory();
	}

	/**
	 * Check if a file is valid.
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean isValidFile(String fileName) {
		return new File(fileName).isFile();
	}

	/**
	 * Create a new directory. If the directory exists, it will clean it up.
	 * 
	 * @param directory
	 * @throws IOException
	 */
	public static void createDirectory(File directory) throws IOException {
		forceMkdir(directory);

		// Clean up the directory from any files in there
		cleanDirectory(directory);
	}

	/**
	 * Create a new directory with the given path.
	 * 
	 * @param path
	 * @throws IOException
	 */
	public static void createDirectory(String path) throws IOException {
		createDirectory(new File(path));
	}

	/**
	 * Clean a directory without deleting it.
	 * 
	 * @param directory
	 * @throws IOException
	 * 
	 *             Got it from apache commons
	 */
	public static void cleanDirectory(File directory) throws IOException {
		if (!directory.exists()) {
			throw new IllegalArgumentException(directory + " does not exist");
		}

		if (!directory.isDirectory()) {
			throw new IllegalArgumentException(directory + " is not a directory");
		}

		File[] files = directory.listFiles();
		if (files == null) { // null if security restricted
			throw new IOException("Failed to list contents of " + directory);
		}

		for (File file : files) {
			forceDelete(file);
		}
	}

	/**
	 * Delete a file. If file is a directory, delete it and all sub-directories.
	 * <p>
	 * The difference between File.delete() and this method are:
	 * <ul>
	 * <li>A directory to be deleted does not have to be empty.</li>
	 * <li>You get exceptions when a file or directory cannot be deleted. (java.io.File methods returns a boolean)</li>
	 * </ul>
	 * 
	 * @param file
	 * @throws NullPointerException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * 
	 *             Got it from apache commons
	 */
	public static void forceDelete(File file) throws IOException {
		if (file.isDirectory()) {
			deleteDirectory(file);
		} else {
			if (!file.exists()) {
				throw new FileNotFoundException("File does not exist: " + file);
			}

			if (!file.delete()) {
				throw new IOException("Unable to delete file: " + file);
			}
		}
	}

	/**
	 * Delete a directory recursively.
	 * 
	 * @param directory
	 * @throws IOException
	 */
	public static void deleteDirectory(File directory) throws IOException {
		cleanDirectory(directory);

		if (!directory.delete()) {
			throw new IOException("Unable to delete directory " + directory + ".");
		}
	}

	/**
	 * Make a directory, including any necessary but nonexistent parent directories. If a file already exists with
	 * specified name but it is not a directory then an IOException is thrown. If the directory cannot be created (or
	 * does not already exist) then an IOException is thrown.
	 * 
	 * @param directory
	 * @throws NullPointerException
	 * @throws IOException
	 * 
	 *             Got it from apache commons
	 */
	public static void forceMkdir(File directory) throws IOException {
		if (directory.exists()) {
			if (!directory.isDirectory()) {
				throw new IOException("File " + directory + " exists and is "
						+ "not a directory. Unable to create directory.");
			}
		} else {
			if (!directory.mkdirs()) {
				// Double-check that some other thread or process hasn't made
				// the directory in the background
				if (!directory.isDirectory()) {
					throw new IOException("Unable to create directory " + directory);
				}
			}
		}
	}

	/**
	 * Find the files with the given extension in the given directory, ignoring sub-directories.
	 * 
	 * @param dir
	 * @param ext
	 * @param msg
	 * @return
	 * @throws IOException
	 */
	public static List<String> findFiles(String dir, final String ext, String msg) throws IOException {
		return findFiles(dir, ext, msg, false);
	}

	/**
	 * Find the files with the given extension in the given directory.
	 * 
	 * @param dir
	 * @param ext
	 * @param msg
	 * @param includeSubDirectories
	 * @return
	 * @throws IOException
	 */
	public static List<String> findFiles(String dir, final String ext, String msg, boolean includeSubDirectories)
			throws IOException {
		List<String> result = new ArrayList<String>();

		IOFileFilter filter = FileFilterFactory.or(FileFilterFactory.createClassFileFileFilter(),
				FileFilterFactory.createDirectoryFileFilter());

		if (isValidDirectory(dir)) {
			for (File file : new File(dir).listFiles((FileFilter) filter)) {
				if (file.isDirectory()) {
					result.addAll(findFiles(file.toString(), ext, msg, includeSubDirectories));
				} else {
					result.add(file.getCanonicalPath());
				}
			}
		} else {
			throw new IOException(msg);
		}

		return result;
	}

	/**
	 * The path to the output class files.
	 * 
	 * @return
	 */
	public static String classesOutputDirectory() {
		return AverroesProperties.getOutputDir().concat(File.separator).concat("classes");
	}

	/**
	 * The path to the placeholder library class files.
	 * 
	 * @return
	 */
	public static String libraryClassesOutputDirectory() {
		return classesOutputDirectory().concat(File.separator).concat("lib");
	}

	/**
	 * The path to the placeholder library JAR file.
	 * 
	 * @return
	 */
	public static String placeholderLibraryJarFile() {
		return AverroesProperties.getOutputDir().concat(File.separator).concat("placeholderLibrary.jar");
	}
	
	/**
	 * The path to the placeholder library JAR file of a benchmark.
	 * 
	 * @return
	 */
	public static String placeholderLibraryJarFile(String benchmark) {
		return composePath("benchmarks-averroes", benchmark + "-placeholder-lib.jar");
	}

	/**
	 * The path to the organized application JAR file.
	 * 
	 * @return
	 */
	public static String organizedApplicationJarFile() {
		return AverroesProperties.getOutputDir().concat(File.separator).concat("organizedApplication.jar");
	}
	
	/**
	 * The path to the organized application JAR file of a benchmark.
	 * 
	 * @return
	 */
	public static String organizedApplicationJarFile(String benchmark) {
		return composePath("benchmarks-averroes", benchmark + "-organized-app.jar");
	}

	/**
	 * The path to the organized library JAR file.
	 * 
	 * @return
	 */
	public static String organizedLibraryJarFile() {
		return AverroesProperties.getOutputDir().concat(File.separator).concat("organizedLibrary.jar");
	}
	
	/**
	 * The path to the organized library JAR file of a benchmark.
	 * 
	 * @return
	 */
	public static String organizedLibraryJarFile(String benchmark) {
		return composePath("benchmarks-averroes", benchmark + "-organized-lib.jar");
	}

	/**
	 * The path to the SparkAverroes call graph.
	 * 
	 * @return
	 */
	public static String sparkAverroesCallGraphFile() {
		return AverroesProperties.getOutputDir().concat(File.separator).concat("sparkAverroes.gxl");
	}
	
	/**
	 * The path to the Spark call graph.
	 * 
	 * @return
	 */
	public static String sparkCallGraphFile() {
		return AverroesProperties.getOutputDir().concat(File.separator).concat("spark.gxl");
	}
	
	/**
	 * The path to the Android call graph.
	 * 
	 * @return
	 */
	public static String androidCallGraphFile() {
		return AverroesProperties.getOutputDir().concat(File.separator).concat("android.txt.gzip");
	}
	
	/**
	 * The path to the android-averroes call graph.
	 * 
	 * @return
	 */
	public static String androidAverroesCallGraphFile() {
		return AverroesProperties.getOutputDir().concat(File.separator).concat("androidAverroes.txt.gzip");
	}
	
	/**
	 * The path to the DoopAverroes call graph.
	 * 
	 * @return
	 */
	public static String doopAverroesCallGraphFile() {
		return AverroesProperties.getOutputDir().concat(File.separator).concat("doopAverroes.gxl");
	}
	
	/**
	 * The path to the DoopAverroes executable
	 * @return
	 */
	public static String doopAverroesRunExe(String doopHome) {
		return doopHome.concat(File.separator).concat("alt-run-averroes");
	}
	
	/* Some utility methods to get the names of comparison call graphs */ 
	public static String missingCallGraphFile(CallGraphSource sup, CallGraphSource sub, CallGraphDiffOptions options) {
		String suffix = options.isSetSoundnessOptions() ? "_app" : "_lib";
		return missingCallGraphFile(sup, sub).replace(".gxl", suffix + ".gxl");
	}

	public static String extraCallGraphFile(CallGraphSource sup, CallGraphSource sub, CallGraphDiffOptions options) {
		String suffix = options.isSetSoundnessOptions() ? "_app" : "_lib";
		return extraCallGraphFile(sup, sub).replace(".gxl", suffix + ".gxl");
	}

	public static String missingCallGraphFile(CallGraphSource sup, CallGraphSource sub) {
		return AverroesProperties.getOutputDir().concat(File.separator).concat(sup.source()).concat("-").concat(sub.source())
				.concat(".gxl");
	}

	public static String extraCallGraphFile(CallGraphSource sup, CallGraphSource sub) {
		return AverroesProperties.getOutputDir().concat(File.separator).concat(sub.source()).concat("-").concat(sup.source())
				.concat(".gxl");
	}
	
	/**
	 * Compose a path from the given arguments.
	 * 
	 * @param args
	 * @return
	 */
	public static String composePath(String... args) {
		String path = "";

		for (int i = 0; i < args.length - 1; i++) {
			path = path.concat(args[i]).concat(File.separator);
		}
		path = path.concat(args[args.length - 1]);

		return path;
	}
}
