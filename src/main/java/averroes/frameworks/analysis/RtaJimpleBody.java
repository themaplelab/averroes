package averroes.frameworks.analysis;

import averroes.frameworks.soot.ClassWriter;
import averroes.frameworks.soot.CodeGenerator;
import averroes.soot.Names;
import averroes.util.io.Printers;
import averroes.util.io.Printers.PrinterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.jimple.AssignStmt;

/**
 * RTA Jimple body creator that over-approximates all objects in the library by just one set
 * represented by the field RTA.set in the newly generated class RTA.
 *
 * @author Karim Ali
 */
public class RtaJimpleBody extends AbstractJimpleBody {
    public static final String name = "rta";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Local rtaSet = null;
    private Local rtaGuard = null;

    /**
     * Create a new RTA Jimple body creator for method M.
     *
     * @param method
     */
    public RtaJimpleBody(SootMethod method) {
        super(method);
    }

    @Override
    protected Local setToCast() {
        return getRtaSet();
    }

    @Override
    protected AssignStmt buildStoreToSetExpr(Value from) {
        return buildStoreFieldExpr(Scene.v().getField(Names.RTA_SET_FIELD_SIGNATURE).makeRef(), from);
    }

    @Override
    protected void storeToSet(Value from) {
        storeField(Scene.v().getField(Names.RTA_SET_FIELD_SIGNATURE).makeRef(), from);
    }

    @Override
    protected Local getGuard() {
        if (rtaGuard == null) {
            rtaGuard = loadField(Scene.v().getField(Names.RTA_GUARD_FIELD_SIGNATURE).makeRef(), true);
        }
        return rtaGuard;
    }

    @Override
    protected void ensureCommonClassExists() {
        if (Scene.v().containsClass(Names.RTA_CLASS)) {
            return;
        }

        // Create a public class and set its super class to java.lang.Object
        SootClass averroesRta =
                CodeGenerator.createEmptyClass(
                        Names.RTA_CLASS, Modifier.PUBLIC, Scene.v().getObjectType().getSootClass());

        // Add a default constructor to it
        CodeGenerator.createEmptyDefaultConstructor(averroesRta);

        // Add static field "set" to the class
        CodeGenerator.createField(
                averroesRta,
                Names.SET_FIELD_NAME,
                Scene.v().getObjectType(),
                Modifier.PUBLIC | Modifier.STATIC);
        CodeGenerator.createField(
                averroesRta, Names.GUARD_FIELD_NAME, BooleanType.v(), Modifier.PUBLIC | Modifier.STATIC);

        // Add a static initializer to it (it also initializes the static fields with default values
        // TODO: Do we really need this?
//        CodeGenerator.createStaticInitializer(averroesRta);

        // Print out the Jimple code
        averroesRta.getMethods().forEach(m -> Printers.printJimple(PrinterType.GENERATED, m));

        // Write it to disk
        ClassWriter.writeLibraryClassFile(averroesRta);
    }

    @Override
    protected void handleFields() {
        fieldReads.stream()
                .forEach(
                        f -> {
                            storeToSet(loadField(f, true));
                        });

        fieldWrites.stream()
                .forEach(
                        f -> {
                            storeField(f, getCompatibleValue(f.type()));
                        });
    }

    /**
     * Load the RTA.set field into a local variable, if not loaded before.
     *
     * @return
     */
    private Local getRtaSet() {
        if (rtaSet == null) {
            rtaSet = loadField(Scene.v().getField(Names.RTA_SET_FIELD_SIGNATURE).makeRef(), true);
        }
        return rtaSet;
    }
}
