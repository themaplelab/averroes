package averroes.frameworks.soot;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
import averroes.frameworks.analysis.RtaJimpleBodyCreator;
import averroes.frameworks.analysis.TypeBasedJimpleBodyCreator;
import averroes.frameworks.analysis.XtaJimpleBodyCreator;
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

	private static HashMap<String, SootClass> nameToClass;

	static {
		nameToClass = new HashMap<String, SootClass>();

		// Create our own hierarchy
		Scene.v().getClasses().forEach(CodeGenerator::addSootClassSkeleton);

	}

	/**
	 * Add a skeleton representing the given soot class to the code generator.
	 * The main reason is that when we generate, for example, an invoke
	 * instruction, we need to make sure that all the methods and classes are
	 * present (regardless of the code within the method bodies). Otherwise,
	 * Soot will throw exceptions whenever we try to get a non-existent method
	 * or class.
	 * 
	 * @param cls
	 */
	private static void addSootClassSkeleton(SootClass cls) {
		nameToClass.put(cls.getName(),
				new SootClass(cls.getName(), cls.getModifiers()));
		cls.getMethods().forEach(CodeGenerator::addSootMethodSkeleton);
	}

	/**
	 * Add a skeleton representing the given soot method, for the same reasons
	 * we do that for Soot classes. This method also adds the newly generated
	 * method to the corresponding class.
	 * 
	 * @param original
	 */
	private static void addSootMethodSkeleton(SootMethod original) {
		SootMethod method = new SootMethod(original.getName(),
				original.getParameterTypes(), original.getReturnType(),
				original.getModifiers(), original.getExceptions());
		nameToClass.get(original.getDeclaringClass().getName()).addMethod(
				method);
	}

	/**
	 * Get the soot method that we have created and that corresponds to the
	 * given original soot method.
	 * 
	 * @param method
	 * @return
	 */
	public static SootMethod getSootMethod(SootMethod original) {
		return nameToClass.values().stream().map(sc -> sc.getMethods())
				.flatMap(List::stream)
				.filter(m -> m.getSignature().equals(original.getSignature()))
				.findFirst().get();
	}

	/**
	 * Get a Jimple body creator for this method, based on the options.
	 * 
	 * @param method
	 */
	public static TypeBasedJimpleBodyCreator getJimpleBodyCreator(
			SootClass cls, SootMethod method) {
		if (FrameworksOptions.getAnalysis().equalsIgnoreCase("rta")) {
			return new RtaJimpleBodyCreator(cls, method);
		} else if (FrameworksOptions.getAnalysis().equalsIgnoreCase("xta")) {
			return new XtaJimpleBodyCreator(cls, method);
		} else {
			return new RtaJimpleBodyCreator(cls, method);
		}
	}

	/**
	 * Transform a soot class (by transforming its methods) based on the
	 * underlying anlaysis.
	 * 
	 * @param method
	 */
	public static void transformSootClass(SootClass cls) {
		SootClass newClass = new SootClass(cls.getName(), cls.getModifiers());
		cls.getMethods().forEach(
				m -> getJimpleBodyCreator(newClass, m).generateCode());
	}

	/**
	 * Add a field to given Soot class.
	 * 
	 * @param cls
	 * @param fieldName
	 * @param fieldType
	 * @param modifiers
	 */
	public static void createField(SootClass cls, String fieldName,
			Type fieldType, int modifiers) {
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
	public static SootClass createEmptyClass(String className, int modifiers,
			SootClass superClass) {
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
						Jimple.v().newSpecialInvokeExpr(body.getThisLocal(),
								getDefaultConstructor(cls).makeRef(),
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
		return new SootMethod(SootMethod.constructorName,
				Collections.<Type> emptyList(), VoidType.v(), Modifier.PUBLIC);
	}

}
