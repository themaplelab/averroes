package averroes.frameworks.analysis;

import soot.BooleanType;
import soot.Local;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.Jimple;
import averroes.frameworks.soot.CodeGenerator;
import averroes.soot.Names;
import averroes.util.io.Printers;
import averroes.util.io.Printers.PrinterType;

/**
 * XTA Jimple body creator that over-approximates objects in the library by
 * using one set per method (set_m) and one set per field (set_f). The analysis
 * also generates a class xta.XTA that holds some commons values (e.g., guard
 * used in conditions).
 * 
 * @author Karim Ali
 *
 */
public class XtaJimpleBody extends AbstractJimpleBody {
	private Local setM = null;
	private Local xtaGuard = null;

	/**
	 * Create a new XTA Jimple body creator for method M.
	 * 
	 * @param method
	 */
	public XtaJimpleBody(SootMethod method) {
		super(method);
		setM = localGenerator.generateLocal(Scene.v().getObjectType());
	}

	@Override
	public void generateCode() {
		// TODO
		Printers.print(PrinterType.ORIGINAL, method);

		// Create XTA Class
		ensureXtaClassExists();

		// Create the new Jimple body
		insertJimpleBodyHeader();
		createObjects();
		callMethods();
		handleArrays();
		// handleFields();
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
		return getSetM();
	}

	@Override
	public void storeToSet(Value from) {
		body.getUnits().add(Jimple.v().newAssignStmt(getSetM(), from));
	}

	@Override
	public Local getGuard() {
		if (xtaGuard == null) {
			xtaGuard = loadStaticField(Scene.v().getField(
					Names.RTA_GUARD_FIELD_SIGNATURE));
		}
		return xtaGuard;
	}

	/**
	 * Ensure that the XTA class has been created, along with its fields.
	 */
	private void ensureXtaClassExists() {
		if (Scene.v().containsClass(Names.XTA_CLASS)) {
			return;
		}

		// Create a public class and set its super class to java.lang.Object
		SootClass averroesXta = CodeGenerator.createEmptyClass(Names.XTA_CLASS,
				Modifier.PUBLIC, Scene.v().getObjectType().getSootClass());

		// Add a default constructor to it
		CodeGenerator.createEmptyDefaultConstructor(averroesXta);

		// Add static field "guard" to the class
		CodeGenerator.createField(averroesXta, Names.GUARD_FIELD_NAME,
				BooleanType.v(), Modifier.PUBLIC | Modifier.STATIC);

		// Add a static initializer to it (it also initializes the static fields
		// with default values
		CodeGenerator.createStaticInitializer(averroesXta);

		// TODO: Write it to disk
		averroesXta.getMethods().forEach(
				m -> Printers.print(PrinterType.GENERATED, m));
		// ClassWriter.writeLibraryClassFile(averroesRta);
	}

	/**
	 * Return the local for set_m.
	 * 
	 * @return
	 */
	private Local getSetM() {
		return setM;
	}
}
