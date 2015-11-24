package averroes.frameworks.soot;

import java.util.Collections;

import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.VoidType;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import averroes.frameworks.analysis.AbstractJimpleBody;
import averroes.frameworks.analysis.RtaJimpleBody;
import averroes.frameworks.options.FrameworksOptions;
import averroes.soot.Names;

/**
 * The master-mind of Averroes. That's where the magic of generating code for
 * library classes happen.
 * 
 * @author Karim Ali
 * 
 */
public class CodeGenerator {

	/**
	 * Get a Jimple body creator for this method, based on the options.
	 * 
	 * @param method
	 */
	public static AbstractJimpleBody getJimpleBodyCreator(SootMethod method) {
		if (FrameworksOptions.getAnalysis().equalsIgnoreCase("rta")) {
			return new RtaJimpleBody(method);
			// } else if
			// (FrameworksOptions.getAnalysis().equalsIgnoreCase("xta")) {
			// return new XtaJimpleBodyCreator(method);
		} else {
			return new RtaJimpleBody(method);
		}
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
	 * Get the default constructor of the given class.
	 * 
	 * @param cls
	 * @return
	 */
	private static SootMethod getDefaultConstructor(SootClass cls) {
		return cls.getMethod(Names.DEFAULT_CONSTRUCTOR_SIG);
	}

	/**
	 * Get a new default constructor method.
	 * 
	 * @return
	 */
	private static SootMethod makeDefaultConstructor() {
		return new SootMethod(SootMethod.constructorName, Collections.<Type> emptyList(), VoidType.v(), Modifier.PUBLIC);
	}

}
