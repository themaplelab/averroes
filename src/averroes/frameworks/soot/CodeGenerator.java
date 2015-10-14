package averroes.frameworks.soot;

import java.util.Collections;

import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.VoidType;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;

/**
 * The master-mind of Averroes. That's where the magic of generating code for
 * library classes happen.
 * 
 * @author Karim Ali
 * 
 */
public class CodeGenerator {

	/**
	 * Create a new Soot class, set its superclass, and add it to the Soot
	 * scene.
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

		return cls;
	}

	/**
	 * Add a default constructor to the given class.
	 * 
	 * @param cls
	 */
	public static void createEmptyDefaultConstructor(SootClass cls) {
		SootMethod init = makeDefaultConstructor();
		JimpleBody body = Jimple.v().newBody(init);
		init.setActiveBody(body);
		cls.addMethod(init);

		// Call superclass constructor
		body.insertIdentityStmts();
		body.getUnits().add(
				Jimple.v().newInvokeStmt(
						Jimple.v().newSpecialInvokeExpr(body.getThisLocal(), getDefaultConstructor(cls).makeRef(),
								Collections.<Value> emptyList())));

		// Add return statement
		body.getUnits().addLast(Jimple.v().newReturnVoidStmt());

		// Finally validate the Jimple body
		body.validate();
	}

	/**
	 * Check if the given class has a default constructor.
	 * 
	 * @param cls
	 * @return
	 */
	public static boolean hasDefaultConstructor(SootClass cls) {
		return cls.declaresMethod(Names.DEFAULT_CONSTRUCTOR_SIG);
	}

	/**
	 * Get the default constructor of the given class.
	 * 
	 * @param cls
	 * @return
	 */
	public static SootMethod getDefaultConstructor(SootClass cls) {
		return cls.getMethod(Names.DEFAULT_CONSTRUCTOR_SIG);
	}

	/**
	 * Check if the method is not public, make it public. Useful for Averroes to
	 * make public constructors for library classes.
	 * 
	 * @param method
	 */
	public static void ensurePublic(SootMethod method) {
		if (!method.isPublic()) {
			// This is a stupid workaround because Soot doesn't allow changing
			// the modifiers of a library method
			method.getDeclaringClass().setApplicationClass();
			method.setModifiers(Modifier.PUBLIC);
			method.getDeclaringClass().setLibraryClass();
		}
	}

	/**
	 * Get a new default constructor method.
	 * 
	 * @return
	 */
	public static SootMethod makeDefaultConstructor() {
		return new SootMethod(SootMethod.constructorName, Collections.<Type> emptyList(), VoidType.v(), Modifier.PUBLIC);
	}

}
