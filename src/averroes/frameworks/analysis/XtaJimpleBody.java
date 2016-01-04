package averroes.frameworks.analysis;

import java.util.HashMap;

import soot.BooleanType;
import soot.Local;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
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
	private Local xtaGuard = null;
	private Local setM;
	private HashMap<SootField, Local> fieldToSetF = new HashMap<SootField, Local>();

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
			xtaGuard = loadField(Scene.v().getField(
					Names.XTA_GUARD_FIELD_SIGNATURE));
		}
		return xtaGuard;
	}

	@Override
	public void ensureCommonClassExists() {
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
		// ClassWriter.writeLibraryClassFile(averroesXta);
	}

	@Override
	public void handleFields() {
		fieldReads.forEach(f -> {
			storeToSet(getSetF(f));
		});

		fieldWrites.forEach(f -> {
			storeToSetF(f);
		});
	}

	/**
	 * Get the local representing set_m.
	 * 
	 * @return
	 */
	private Local getSetM() {
		return setM;
	}

	/**
	 * Get the local representing set_f for the given field.
	 * 
	 * @param field
	 * @return
	 */
	private Local getSetF(SootField field) {
		if (!fieldToSetF.containsKey(field)) {
			ensureSetFExists(field);
			Local local = loadField(field.getDeclaringClass().getFieldByName(
					setFName(field)));
			fieldToSetF.put(field, local);
		}

		return fieldToSetF.get(field);
	}

	/**
	 * Model the statement set_f = (Type(f) set_m)
	 * 
	 * @param field
	 */
	private void storeToSetF(SootField field) {
		ensureSetFExists(field);
		Value from = getCompatibleValue(field.getType());
		storeField(field.getDeclaringClass().getFieldByName(setFName(field)),
				from);
	}

	/**
	 * Ensures that the declaring class of the given Soot field declares the
	 * set_f field.
	 * 
	 * @param field
	 */
	private void ensureSetFExists(SootField field) {
		SootClass cls = field.getDeclaringClass();
		String name = setFName(field);

		if (!cls.declaresFieldByName(name)) {
			cls.addField(new SootField(name, field.getType(), field
					.getModifiers()));
		}
	}

	/**
	 * The name of the set_f field for the given Soot field.
	 * 
	 * @param field
	 * @return
	 */
	private String setFName(SootField field) {
		return Names.SET_FIELD_PREFIX + field.getName();
	}
}
