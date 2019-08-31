package averroes.frameworks.soot;

import averroes.soot.SootSceneUtil;
import averroes.util.io.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.SootClass;
import soot.SourceLocator;
import soot.baf.BafASMBackend;
import soot.options.Options;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A utility class to write class files to disk.
 *
 * @author Karim Ali
 */
public class ClassWriter {

    private static final Logger logger = LoggerFactory.getLogger(ClassWriter.class);

    /**
     * Write the class file for the generated library classes.
     */
    public static void writeLibraryClassFiles() {
        SootSceneUtil.getClasses().forEach(ClassWriter::writeLibraryClassFile);
    }

    /**
     * Write the class file for the given library class.
     *
     * @param cls
     * @throws IOException
     */
    public static void writeLibraryClassFile(SootClass cls) {
        Options.v().set_output_dir(Paths.frameworksLibraryClassesOutputDirectory().getPath());
        Options.v().set_java_version(Options.java_version_8);

        File file = new File(SourceLocator.v().getFileNameFor(cls, Options.output_format_class));
        file.getParentFile().mkdirs();

        try {
            OutputStream streamOut = new FileOutputStream(file);
            BafASMBackend backend = new BafASMBackend(cls, Options.v().java_version());
            backend.generateClassFile(streamOut);
        } catch (IOException e) {
            logger.error("Cannot write class " + cls + " to " + file.getPath());
            e.printStackTrace();
        }
    }
}
