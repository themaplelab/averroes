package averroes.frameworks.soot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootClass;
import soot.SourceLocator;
import soot.options.Options;
import soot.util.JasminOutputStream;
import averroes.util.io.Paths;

/**
 * A utility class to write class files to disk.
 * 
 * @author Karim Ali
 *
 */
public class ClassWriter {

	private static final Logger logger = LoggerFactory
			.getLogger(ClassWriter.class);

	/**
	 * Write the class file for the given library class.
	 * 
	 * @param cls
	 * @throws IOException
	 */
	public static void writeLibraryClassFile(SootClass cls) {
		Options.v().set_output_dir(Paths.framewokrsLibraryClassesOutputDirectory().getPath());

		File file = new File(SourceLocator.v().getFileNameFor(cls, Options.output_format_class));
		file.getParentFile().mkdirs();

		try {
			OutputStream streamOut = new JasminOutputStream(new FileOutputStream(file));
			PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));

			if (cls.containsBafBody()) {
				new soot.baf.JasminClass(cls).print(writerOut);
			} else {
				new soot.jimple.JasminClass(cls).print(writerOut);
			}

			writerOut.flush();
			streamOut.close();
		} catch (IOException e) {
			logger.error("Cannot write class " + cls + " to " + file.getPath());
			e.printStackTrace();
		}
	}
}
