package averroes.frameworks.analysis;

import averroes.frameworks.soot.ClassWriter;
import averroes.frameworks.soot.CodeGenerator;
import averroes.soot.Names;
import averroes.util.io.Printers;
import averroes.util.io.Printers.PrinterType;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;

import java.util.HashMap;

/**
 * XTA Jimple body creator that over-approximates objects in the library by using one set per method
 * (set_m) and one set per field (set_f). The analysis also generates a class xta.XTA that holds
 * some commons values (e.g., guard used in conditions).
 *
 * @author Karim Ali
 */
public class XtaJimpleBody extends AbstractJimpleBody {
    public static final String name = "xta";

    private Local xtaGuard = null;

    private SootFieldRef setM = null;
    private Local setMLocal = null;

    private HashMap<SootFieldRef, SootFieldRef> setF = new HashMap<>();
    private HashMap<SootFieldRef, Local> setFLocal = new HashMap<>();

    /**
     * Create a new XTA Jimple body creator for method M.
     *
     * @param method
     */
    public XtaJimpleBody(SootMethod method) {
        super(method);
    }

    @Override
    protected Local setToCast() {
        return getSetMLocal();
    }

    @Override
    protected AssignStmt buildStoreToSetExpr(Value from) {
        return buildStoreFieldExpr(getSetM(), from);
    }

    @Override
    protected void storeToSet(Value from) {
        storeField(getSetM(), from);
    }

    @Override
    protected Local getGuard() {
        if (xtaGuard == null) {
            xtaGuard = loadField(Scene.v().getField(Names.XTA_GUARD_FIELD_SIGNATURE).makeRef(), true);
        }
        return xtaGuard;
    }

    @Override
    protected void ensureCommonClassExists() {
        if (Scene.v().containsClass(Names.XTA_CLASS)) {
            return;
        }

        // Create a public class and set its super class to java.lang.Object
        SootClass averroesXta =
                CodeGenerator.createEmptyClass(
                        Names.XTA_CLASS, Modifier.PUBLIC, Scene.v().getObjectType().getSootClass());

        // Add a default constructor to it
        CodeGenerator.createEmptyDefaultConstructor(averroesXta);

        // Add static field "guard" to the class
        CodeGenerator.createField(
                averroesXta, Names.GUARD_FIELD_NAME, BooleanType.v(), Modifier.PUBLIC | Modifier.STATIC);

        // Add a static initializer to it (it also initializes the static fields with default values
        // TODO: Do we really need this?
//        CodeGenerator.createStaticInitializer(averroesXta);

        // Print out the Jimple code
        averroesXta.getMethods().forEach(m -> Printers.printJimple(PrinterType.GENERATED, m));

        // Write it to disk
        ClassWriter.writeLibraryClassFile(averroesXta);
    }

    @Override
    protected void handleFields() {
        fieldReads.forEach(
                f -> {
                    storeToSet(getSetFLocal(f));
                });

        fieldWrites.forEach(
                f -> {
                    storeToSetF(f);
                });
    }

    /**
     * Get the local representing the set_m field of the underlying Soot method.
     *
     * @return
     */
    private Local getSetMLocal() {
        if (setMLocal == null) {
            setMLocal = loadField(getSetM(), true);
        }

        return setMLocal;
    }

    /**
     * This basically special cases the loadField grammar chunk for the set_m and set_f field of the
     * underlying method. For those loads, use the "this" variable instead of the regular cast as long
     * as the method is not static. Otherwise, cyclic calls happen and the flying spaghetti monster
     * will show up on your doorstep.
     *
     * @param fieldRef
     * @return
     */
    @Override
    protected Value getFieldRef(SootFieldRef fieldRef) {
        if (fieldRef.isStatic()) {
            return Jimple.v().newStaticFieldRef(fieldRef);
        } else if (fieldRef.equals(getSetM()) && !method.isStatic()) {
            return Jimple.v().newInstanceFieldRef(body.getThisLocal(), fieldRef);
        } else {
            return Jimple.v()
                    .newInstanceFieldRef(
                            getCompatibleValue(fieldRef.declaringClass().getType()), fieldRef);
        }
    }

    /**
     * Get the local representing the set_f field of the given Soot field reference.
     *
     * @param fieldRef
     * @return
     */
    private Local getSetFLocal(SootFieldRef fieldRef) {
        if (!setFLocal.containsKey(fieldRef)) {
            setFLocal.put(fieldRef, loadField(getSetF(fieldRef), true));
        }

        return setFLocal.get(fieldRef);
    }

    /**
     * Get the Soot field representing the set_m field of the underlying Soot method.
     *
     * @return
     */
    private SootFieldRef getSetM() {
        if (setM == null) {
            ensureSetMExists();
            setM = method.getDeclaringClass().getFieldByName(setMName()).makeRef();
        }

        return setM;
    }

    /**
     * Get the Soot field reference representing the set_f field of the given Soot field reference.
     *
     * @param fieldRef
     * @return
     */
    private SootFieldRef getSetF(SootFieldRef fieldRef) {
        if (!setF.containsKey(fieldRef)) {
            // ensureSetFExists(field);
            // setF.put(field,
            // field.getDeclaringClass().getFieldByName(setFName(field)));
            setF.put(fieldRef, fieldRef);
        }

        return setF.get(fieldRef);
    }

    /**
     * Model the statement set_f = (Type(f) set_m)
     *
     * @param fieldRef
     */
    private void storeToSetF(SootFieldRef fieldRef) {
        Value from = getCompatibleValue(getSetF(fieldRef).type());
        storeField(getSetF(fieldRef), from);
    }

    // /**
    // * Ensures that the declaring class of the given Soot field declares the
    // * set_f field.
    // *
    // * @param field
    // */
    // private void ensureSetFExists(SootField field) {
    // SootClass cls = field.getDeclaringClass();
    // String name = setFName(field);
    //
    // if (!cls.declaresFieldByName(name)) {
    // int modifiers = Modifier.PRIVATE | (field.isStatic() ? Modifier.STATIC :
    // 0);
    // cls.addField(new SootField(name, field.getType(), modifiers));
    // }
    // }

    /**
     * Ensures that the declaring class of the given Soot method declares the set_m field.
     */
    private void ensureSetMExists() {
        SootClass cls = method.getDeclaringClass();
        String name = setMName();

        if (!cls.declaresFieldByName(name)) {
            int modifiers = Modifier.PRIVATE | (method.isStatic() ? Modifier.STATIC : 0);
            cls.addField(new SootField(name, Scene.v().getObjectType(), modifiers));
        }
    }

    // /**
    // * The name of the set_f field for the given Soot field.
    // *
    // * @param field
    // * @return
    // */
    // private String setFName(SootField field) {
    // return Names.SET_FIELD_PREFIX + field.getName();
    // }

    /**
     * The name of the set_m field for the given Soot method. We're using the index of the method here
     * instead of its name because it could be an overloaded method. In such case, using the name will
     * raise an exception when we try to add a field for the 2nd overload of the method because a
     * field with the name set_m_methodname has already been added to the class.
     *
     * @return
     */
    private String setMName() {
        return Names.SET_METHOD_PREFIX + method.getDeclaringClass().getMethods().indexOf(method);
    }
}
