package averroes.frameworks.soot;

import averroes.frameworks.options.FrameworksOptions;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;

import java.io.*;
import java.util.Map;

/**
 * This class generates a reflection log file containing only reflection information relevant to the summary currently
 * being generated.  I.e., only reflective calls that occur in classes which exist in the current soot scene are included.
 */
public class ReflectionTransformer extends SceneTransformer {

    /**
     * Filter the massive reflection log and write only the relevant reflection information to a new
     * log file.
     */
    @Override
    public void internalTransform(String s, Map<String, String> map) {
        try (
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
                String kind = portions[0];
                String target = portions[1];
                String source = portions[2];
                int lineNum = Integer.parseInt(portions[3]);
                String className = source.substring(0,source.lastIndexOf("."));
                if (Scene.v().containsClass(className) && isRelevantClass(target, kind)) {
                   inline(kind, target, source, lineNum);
                }

                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Problem handling reflective calls!", e);
        }

    }

    /**
     * Replaces a reflective call with its non-reflective equivalent.
     *
     * @param kind
     * @param target
     * @param source
     * @param lineNum
     */
    private void inline(String kind, String target, String source, int lineNum) {
        switch (kind) {
            case "Class.forName":
                SootClass sc = Scene.v().getSootClass(source.substring(0,source.lastIndexOf(".")));
                SootMethod m = sc.getMethodByName(source.substring(source.lastIndexOf(".")));
                //m.getActiveBody().getUnits().
                return;
            case "Class.newInstance":
                return;
            case "Constructor.newInstance":
                return;
            case "Field.get*":
                return;
            case "Method.invoke":
                return;
            default:
                throw new RuntimeException("Unexpected reflection kind: " + kind);
        }
    }

    /**
     * A class is relevant if it is declared as an application class in the soot scene (i.e. a non-JDK library class)
     * or if it is a Java library class.
     *
     * @param targetClass
     * @return
     */
    private boolean isRelevantClass(String targetClass, String kind) {
        if (kind.equals("Class.forName") || kind.equals("Class.newInstance")) {
            SootClass sc = Scene.v().getSootClass(targetClass);
            return sc.isApplicationClass() || sc.isJavaLibraryClass();
        } else {
            // other types of reflection (e.g. method invoke), not yet supported
            return false;
        }
    }

}
