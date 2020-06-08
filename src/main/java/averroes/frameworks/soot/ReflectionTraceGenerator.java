package averroes.frameworks.soot;

import averroes.frameworks.options.FrameworksOptions;
import soot.Scene;
import soot.SootClass;

import java.io.*;

/**
 * This class generates a reflection log file containing only reflection information relevant to the summary currently
 * being generated.  I.e., only reflective calls that occur in classes which exist in the current soot scene are included.
 */
public class ReflectionTraceGenerator {

    File tmpLogFile;

    public ReflectionTraceGenerator(String tmpLogFilePath) {
        this.tmpLogFile = new File(tmpLogFilePath);
        tmpLogFile.getParentFile().mkdirs();
        if (this.tmpLogFile.exists()) {
            this.tmpLogFile.delete();
        }
    }

    /**
     * Filter the massive reflection log and write only the relevant reflection information to a new
     * log file.
     */
    public void generate() {
        try (
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(tmpLogFile)));
            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    new FileInputStream(
                                            FrameworksOptions.getReflectionLog()
                                    )))
        ) {
            String line;
            line = reader.readLine();
            while (line != null) {
                String[] portions = line.split(";");
                //String kind = portions[0];
                String target = portions[1];
                String source = portions[2];
                String className = source.substring(0,source.lastIndexOf("."));
                if (Scene.v().containsClass(className) && isRelevantClass(target)) {
                    writer.write(line);
                    writer.newLine();
                }

                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Problem handling reflective calls!", e);
        }

    }

    /**
     * A class is relevant if it is declared as an application class in the soot scene (i.e. a non-JDK library class)
     * or if it is a Java library class.
     *
     * @param targetClass
     * @return
     */
    private boolean isRelevantClass(String targetClass) {
        SootClass sc = Scene.v().getSootClass(targetClass);
        return sc.isApplicationClass() || sc.isJavaLibraryClass();
    }

}
