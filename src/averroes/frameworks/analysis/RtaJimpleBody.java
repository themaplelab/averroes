package averroes.frameworks.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.BooleanType;
import soot.Local;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.VoidType;
import soot.jimple.Jimple;
import averroes.frameworks.soot.CodeGenerator;
import averroes.soot.Names;
import averroes.util.io.Printers;
import averroes.util.io.Printers.PrinterType;

/**
 * RTA Jimple body creator that over-approximates all objects in the library by
 * just one set represented by the field RTA.set in the newly generated class
 * RTA.
 * 
 * @author Karim Ali
 *
 */
public class RtaJimpleBody extends AbstractJimpleBody {
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
	public void generateCode() {
		// TODO
		Printers.print(PrinterType.ORIGINAL, method);

		// Create RTA Class
		ensureRtaClassExists();

		// Create the new Jimple body
		insertJimpleBodyHeader();
		createObjects();
		callMethods();
		handleArrays();
		handleExceptions();
		insertJimpleBodyFooter();

		// Cleanup the generated body
		cleanup();

		// Validate method Jimple body & assign it to the method
		body.validate();
		method.setActiveBody(body);

		// TODO
		Printers.print(PrinterType.GENERATED, method);
	}

	@Override
	public Local setToCast() {
		return getRtaSet();
	}

	@Override
	public void storeToSet(Value from) {
		storeStaticField(Scene.v().getField(Names.RTA_SET_FIELD_SIGNATURE),
				from);
	}

	@Override
	public Local getGuard() {
		if (rtaGuard == null) {
			rtaGuard = loadStaticField(Scene.v().getField(
					Names.RTA_GUARD_FIELD_SIGNATURE));
		}
		return rtaGuard;
	}

	/**
	 * Ensure that the RTA class has been created, along with its fields.
	 */
	private void ensureRtaClassExists() {
		if (Scene.v().containsClass(Names.RTA_CLASS)) {
			return;
		}

		// Create a public class and set its super class to java.lang.Object
		SootClass averroesRta = CodeGenerator.createEmptyClass(Names.RTA_CLASS,
				Modifier.PUBLIC, Scene.v().getObjectType().getSootClass());

		// Add a default constructor to it
		CodeGenerator.createEmptyDefaultConstructor(averroesRta);

		// Add static field "set" to the class
		CodeGenerator.createField(averroesRta, Names.SET_FIELD_NAME, Scene.v()
				.getObjectType(), Modifier.PUBLIC | Modifier.STATIC);
		CodeGenerator.createField(averroesRta, Names.GUARD_FIELD_NAME,
				BooleanType.v(), Modifier.PUBLIC | Modifier.STATIC);

		// Add a static initializer to it (it also initializes the static fields
		// with default values
		CodeGenerator.createStaticInitializer(averroesRta);

		// TODO: Write it to disk
		averroesRta.getMethods().forEach(
				m -> Printers.print(PrinterType.GENERATED, m));
		// ClassWriter.writeLibraryClassFile(averroesRta);
	}

	/**
	 * Insert identity statements. The version in {@link JimpleBody} does not
	 * use the {@link LocalGenerator} class to generate locals (it actually
	 * can't because it doesn't have a reference to the {@link LocalGenerator}
	 * that was used to generated this method body). This causes inconsistency
	 * in the names of the generated local variables and this method tries to
	 * fix that.
	 */
	// private void insertIdentityStmts() {
	// // Add "this" before anything else
	// if (!method.isStatic()) {
	// Local r0 = localGenerator.generateLocal(method.getDeclaringClass()
	// .getType());
	// ThisRef thisRef = Jimple.v().newThisRef((RefType) r0.getType());
	// body.getUnits().addFirst(
	// Jimple.v().newIdentityStmt(thisRef, thisRef));
	// }
	//
	// // Add identity statements for any method parameters next
	// for (int i = 0; i < method.getParameterCount(); i++) {
	// Local p = localGenerator.generateLocal(method.getParameterType(i));
	// ParameterRef paramRef = Jimple.v().newParameterRef(p.getType(), i);
	// body.getUnits().add(Jimple.v().newIdentityStmt(p, paramRef));
	// }
	// }

	/**
	 * Insert the standard footer for a library method.
	 */
	private void insertJimpleBodyFooter() {
		/*
		 * Insert the return statement, only if there are no throwables to
		 * throw. Otherwise, it's dead code and the Jimple validator will choke
		 * on it!
		 */
		if (throwables.isEmpty()) {
			insertReturnStmt();
		}
	}

	/**
	 * Insert the appropriate return statement.
	 */
	private void insertReturnStmt() {
		if (method.getReturnType() instanceof VoidType) {
			body.getUnits().add(Jimple.v().newReturnVoidStmt());
		} else {
			Value ret = getCompatibleValue(method.getReturnType());
			body.getUnits().add(Jimple.v().newReturnStmt(ret));
		}
	}

	/**
	 * Load the RTA.set field into a local variable, if not loaded before.
	 * 
	 * @return
	 */
	private Local getRtaSet() {
		if (rtaSet == null) {
			rtaSet = loadStaticField(Scene.v().getField(
					Names.RTA_SET_FIELD_SIGNATURE));
		}
		return rtaSet;
	}
}
