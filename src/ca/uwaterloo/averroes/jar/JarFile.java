package ca.uwaterloo.averroes.jar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.util.ClassPath;
import org.apache.bcel.util.SyntheticRepository;
import org.apache.bcel.verifier.VerificationResult;
import org.apache.bcel.verifier.Verifier;
import org.apache.bcel.verifier.VerifierFactory;

import soot.SootMethod;
import ca.uwaterloo.averroes.exceptions.Assertions;
import ca.uwaterloo.averroes.soot.Hierarchy;
import ca.uwaterloo.averroes.util.io.FileUtils;

/**
 * A JAR file is a collection of class files. We use BCEL to verify that the generated JAR files conforms to the JVM
 * standards.
 * 
 * @author karim
 * 
 */
public class JarFile {

	private JarOutputStream jarOutputStream;
	private String fileName;
	private Set<JavaClass> bcelClasses;

	/**
	 * Construct a new JAR file.
	 * 
	 * @param fileName
	 */
	public JarFile(String fileName) {
		jarOutputStream = null;
		this.fileName = fileName;
		bcelClasses = new HashSet<JavaClass>();
	}

	/**
	 * Get the output stream of this JAR archive.
	 * 
	 * @return
	 * @throws IOException
	 */
	public JarOutputStream getJarOutputStream() throws IOException {
		if (jarOutputStream == null) {
			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
			jarOutputStream = new JarOutputStream(new FileOutputStream(fileName), manifest);
		}
		return jarOutputStream;
	}

	/**
	 * Add all the generated class files to the Jar file.
	 * 
	 * @throws IOException
	 */
	public void addGeneratedLibraryClassFiles() throws IOException {
		Set<String> classFiles = new HashSet<String>();
		String dir = FileUtils.libraryClassesOutputDirectory();
		String jar = FileUtils.placeholderLibraryJarFile();

		// Add the class files to the crafted JAR file.
		for (String fileName : FileUtils.findFiles(dir, "class", "not found")) {
			add(dir, new File(fileName));
			classFiles.add(relativize(dir, fileName));
		}
		close();

		// Set BCEL's repository class path.
		SyntheticRepository rep = SyntheticRepository.getInstance(new ClassPath(jar.concat(System.getProperty(
				"path.separator").concat(FileUtils.organizedApplicationJarFile()))));
		Repository.setRepository(rep);

		// Now add all those class files in the crafted JAR file to the BCEL repository.
		for (String classFile : classFiles) {
			ClassParser parser = new ClassParser(jar, classFile);
			JavaClass cls = parser.parse();
			bcelClasses.add(cls);
			Repository.getRepository().storeClass(cls);
		}
	}

	/**
	 * Add a class file from source to the Jar file.
	 * 
	 * @param dir
	 * @param source
	 * @throws IOException
	 */
	public void add(String dir, File source) throws IOException {
		BufferedInputStream in = null;
		try {
			if (source.isDirectory()) {
				String name = relativize(dir, source).replace("\\", "/");
				if (!name.isEmpty()) {
					if (!name.endsWith("/"))
						name += "/";
					JarEntry entry = new JarEntry(name);
					entry.setTime(source.lastModified());
					getJarOutputStream().putNextEntry(entry);
					getJarOutputStream().closeEntry();
				}
				for (File nestedFile : source.listFiles())
					add(dir, nestedFile);
				return;
			}

			JarEntry entry = new JarEntry(relativize(dir, source).replace("\\", "/"));
			entry.setTime(source.lastModified());
			getJarOutputStream().putNextEntry(entry);
			in = new BufferedInputStream(new FileInputStream(source));

			byte[] buffer = new byte[1024];
			while (true) {
				int count = in.read(buffer);
				if (count == -1)
					break;
				getJarOutputStream().write(buffer, 0, count);
			}
			getJarOutputStream().closeEntry();
		} finally {
			if (in != null)
				in.close();
		}
	}

	/**
	 * Add the given file with the given entry name to this JAR file.
	 * 
	 * @param source
	 * @param entryName
	 * @throws IOException
	 */
	public void add(File source, String entryName) throws IOException {
		JarEntry entry = new JarEntry(entryName);
		entry.setTime(source.lastModified());
		getJarOutputStream().putNextEntry(entry);
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(source));

		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) != -1) {
			getJarOutputStream().write(buffer, 0, len);
		}
		getJarOutputStream().closeEntry();
		in.close();
	}

	/**
	 * Add the file read from the source input stream with the given entry name to this JAR file.
	 * 
	 * @param source
	 * @param entryName
	 * @throws IOException
	 */
	public void add(InputStream source, String entryName) throws IOException {
		JarEntry entry = new JarEntry(entryName);
		entry.setTime(System.currentTimeMillis());
		getJarOutputStream().putNextEntry(entry);

		byte[] buffer = new byte[1024];
		int len;
		while ((len = source.read(buffer)) != -1) {
			getJarOutputStream().write(buffer, 0, len);
		}
		getJarOutputStream().closeEntry();
		source.close();
	}

	/**
	 * Close the JAR output stream.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		getJarOutputStream().close();
	}

	/**
	 * Verify the integrity of the jar file.
	 * 
	 * @throws IOException
	 * @throws ClassFormatException
	 */
	public void verify() throws ClassFormatException, IOException {
		for (JavaClass cls : bcelClasses) {
			Verifier verifier = VerifierFactory.getVerifier(cls.getClassName());
			Method[] methods = cls.getMethods();
			for (int i = 0; i < methods.length; i++) {
				VerificationResult vr;
				// Do a pass 3a for the constructor of java.lang.Object because we are using an uninitialized "this".
				if (cls.getClassName().equals(Hierarchy.JAVA_LANG_OBJECT)
						&& methods[i].getName().equals(SootMethod.constructorName)) {
					vr = verifier.doPass3a(i);
				} else {
					vr = verifier.doPass3b(i);
				}

				Assertions.verificationResultOKAssertion(vr, cls.getClassName(), methods[i].getName());
			}
		}
	}

	/**
	 * Get the relative path for an absolute file path.
	 * 
	 * @param absolute
	 * @return
	 */
	public static String relativize(String dir, String absolute) {
		return new File(dir).toURI().relativize(new File(absolute).toURI()).getPath();
	}

	/**
	 * Get the relative path for an absolute file path.
	 * 
	 * @param dir
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String relativize(String dir, File file) throws IOException {
		return relativize(dir, file.getCanonicalPath());
	}

	/**
	 * Get the class name from an absolute path to a class file.
	 * 
	 * @param dir
	 * @param classFile
	 * @return
	 */
	public static String pathToClassName(String dir, String classFile) {
		return relativize(dir, classFile).replace(".class", "").replaceAll("/", ".");
	}
}
