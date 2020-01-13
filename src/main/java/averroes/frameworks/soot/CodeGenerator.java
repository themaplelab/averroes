package averroes.frameworks.soot;

import averroes.frameworks.analysis.AbstractJimpleBody;
import averroes.frameworks.analysis.RtaJimpleBody;
import averroes.frameworks.analysis.XtaJimpleBody;
import averroes.frameworks.options.FrameworksOptions;
import averroes.soot.Names;
import averroes.soot.SootSceneUtil;
import soot.*;
import soot.jimple.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The master-mind of Averroes. That's where the magic of generating code for library classes
 * happen.
 *
 * @author Karim Ali
 */
public class CodeGenerator {

    /**
     * Generate Jimple for all classes that Averroes has processed.
     * <p>
     * Note: do not call Scene.v().getClasses() here as it will throw concurrent
     * modification error when new classes are added.
     */
    public static void generateJimple() {
        // We ignore non-concrete methods, because they do not have method bodies (surprise!).
        SootSceneUtil.getClasses().stream().map(SootClass::getMethods).flatMap(List::stream)
                .filter(SootMethod::isConcrete).collect(Collectors.toList()).forEach(m -> getJimpleBodyCreator(m).generateCode());
    }

    /**
     * Add a field to given Soot class.
     *
     * @param cls
     * @param fieldName
     * @param fieldType
     * @param modifiers
     */
    public static void createField(SootClass cls, String fieldName, Type fieldType, int modifiers) {
        SootField field = new SootField(fieldName, fieldType, modifiers);
        cls.addField(field);
    }

    /**
     * Create a new Soot class, set its superclass, and add it to the Soot scene.
     *
     * @param className
     * @param modifiers
     * @param superClass
     * @return
     */
    public static SootClass createEmptyClass(String className, int modifiers, SootClass superClass) {
        SootClass cls = new SootClass(className, modifiers);
        cls.setSuperclass(superClass);
        Scene.v().addClass(cls);
        cls.setApplicationClass();

        return cls;
    }

    /**
     * Add a default constructor to the given class.
     *
     * @param cls
     */
    public static void createEmptyDefaultConstructor(SootClass cls) {
        if (!hasDefaultConstructor(cls)) {
            SootMethod init = makeDefaultConstructor();
            JimpleBody body = Jimple.v().newBody(init);
            init.setActiveBody(body);
            cls.addMethod(init);

            // Call superclass constructor
            body.insertIdentityStmts();

            // Create a default constructor in the super class if it doesn't have one.
            if (!hasDefaultConstructor(cls.getSuperclass())) {
                createEmptyDefaultConstructor(cls.getSuperclass());
            }

            body.getUnits()
                    .add(
                            Jimple.v()
                                    .newInvokeStmt(
                                            Jimple.v()
                                                    .newSpecialInvokeExpr(
                                                            body.getThisLocal(),
                                                            getDefaultConstructor(cls.getSuperclass()).makeRef(),
                                                            Collections.emptyList())));

            // Add return statement
            body.getUnits().addLast(Jimple.v().newReturnVoidStmt());

            // Finally validate the Jimple body
            body.validate();
        }
    }

    /**
     * Add a static initializer to the given class.
     *
     * @param cls
     */
    public static void createStaticInitializer(SootClass cls) {
        SootMethod clinit = makeStaticInitializer();
        JimpleBody body = Jimple.v().newBody(clinit);
        clinit.setActiveBody(body);
        cls.addMethod(clinit);

        // Initialize the static fields
        body.insertIdentityStmts();
        cls.getFields().stream()
                .filter(SootField::isStatic)
                .forEach(
                        f -> {
                            body.getUnits()
                                    .add(
                                            Jimple.v()
                                                    .newAssignStmt(
                                                            Jimple.v().newStaticFieldRef(f.makeRef()),
                                                            getDefaultValue(f.getType())));
                        });

        // Add return statement
        body.getUnits().addLast(Jimple.v().newReturnVoidStmt());

        // Finally validate the Jimple body
        body.validate();
    }

    /**
     * Get a Jimple body creator for this method, based on the options.
     *
     * @param method
     */
    private static AbstractJimpleBody getJimpleBodyCreator(SootMethod method) {
        if (FrameworksOptions.getAnalysis().equalsIgnoreCase("rta")) {
            return new RtaJimpleBody(method);
        } else if (FrameworksOptions.getAnalysis().equalsIgnoreCase("xta")) {
            return new XtaJimpleBody(method);
        } else {
            return new RtaJimpleBody(method);
        }
    }

    /**
     * Check if the given class has a default constructor.
     *
     * @param cls
     * @return
     */
    private static boolean hasDefaultConstructor(SootClass cls) {
        return cls.declaresMethod(Names.DEFAULT_CONSTRUCTOR_SUBSIG);
    }

    /**
     * Get the default value we should use for the given type.
     *
     * @param type
     * @return
     */
    private static Value getDefaultValue(Type type) {
        if (type instanceof PrimType) {
            if (type instanceof LongType) {
                return LongConstant.v(1);
            } else if (type instanceof FloatType) {
                return FloatConstant.v(1);
            } else if (type instanceof DoubleType) {
                return DoubleConstant.v(1);
            } else {
                return IntConstant.v(1);
            }
        } else {
            return NullConstant.v();
        }
    }

    /**
     * Get the default constructor of the given class.
     *
     * @param cls
     * @return
     */
    private static SootMethod getDefaultConstructor(SootClass cls) {
        return cls.getMethod(Names.DEFAULT_CONSTRUCTOR_SUBSIG);
    }

    /**
     * Get a new default constructor method.
     *
     * @return
     */
    private static SootMethod makeDefaultConstructor() {
        return new SootMethod(
                SootMethod.constructorName, Collections.emptyList(), VoidType.v(), Modifier.PUBLIC);
    }

    /**
     * Get a new static initializer method.
     *
     * @return
     */
    private static SootMethod makeStaticInitializer() {
        return new SootMethod(
                SootMethod.staticInitializerName,
                Collections.emptyList(),
                VoidType.v(),
                Modifier.PUBLIC | Modifier.STATIC);
    }
}
