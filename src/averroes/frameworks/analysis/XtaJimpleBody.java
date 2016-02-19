package averroes.frameworks.analysis;

import java.util.HashMap;

import averroes.frameworks.soot.CodeGenerator;
import averroes.soot.Names;
import averroes.util.io.Printers;
import averroes.util.io.Printers.PrinterType;
import soot.BooleanType;
import soot.Local;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
import soot.jimple.Jimple;

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
	private Local setMLocal = null;
	private SootField setM = null;
	private HashMap<SootField, Local> fieldToSetF = new HashMap<SootField, Local>();

	/**
	 * Create a new XTA Jimple body creator for method M.
	 * 
	 * @param method
	 */
	public XtaJimpleBody(SootMethod method) {
		super(method);
	}

	@Override
	public Local setToCast() {
		return getSetMLocal();
	}

	@Override
	public void storeToSet(Value from) {
		storeField(getSetM(), from);
	}

	@Override
	public Local getGuard() {
		if (xtaGuard == null) {
			xtaGuard = loadField(Scene.v().getField(Names.XTA_GUARD_FIELD_SIGNATURE));
		}
		return xtaGuard;
	}

	@Override
	public void ensureCommonClassExists() {
		if (Scene.v().containsClass(Names.XTA_CLASS)) {
			return;
		}

		// Create a public class and set its super class to java.lang.Object
		SootClass averroesXta = CodeGenerator.createEmptyClass(Names.XTA_CLASS, Modifier.PUBLIC,
				Scene.v().getObjectType().getSootClass());

		// Add a default constructor to it
		CodeGenerator.createEmptyDefaultConstructor(averroesXta);

		// Add static field "guard" to the class
		CodeGenerator.createField(averroesXta, Names.GUARD_FIELD_NAME, BooleanType.v(),
				Modifier.PUBLIC | Modifier.STATIC);

		// Add a static initializer to it (it also initializes the static fields
		// with default values
		CodeGenerator.createStaticInitializer(averroesXta);

		// TODO: Write it to disk
		averroesXta.getMethods().forEach(m -> Printers.print(PrinterType.GENERATED, m));
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
	 * Load the set_m field. This is different from the
	 * {@link AbstractJimpleBody#loadField(SootField)} method in that here we
	 * use the {@code this} variable for loading set_m if it's instance field.
	 * In {@link AbstractJimpleBody#loadField(SootField)}, we use a cast of the
	 * summary set used (e.g., RTA.set in case of RTA, and set_m in case of
	 * XTA). However, using this summary set here raises a StackOverFlowError
	 * since {@link #getSetMLocal()} will call
	 * {@link #getCompatibleValue(soot.Type)} and vice versa.
	 * 
	 * @return
	 */
	private Local loadSetMField() {
		Local tmp = localGenerator.generateLocal(getSetM().getType());

		if (getSetM().isStatic()) {
			body.getUnits().add(Jimple.v().newAssignStmt(tmp, Jimple.v().newStaticFieldRef(getSetM().makeRef())));
		} else {
			body.getUnits().add(Jimple.v().newAssignStmt(tmp,
					Jimple.v().newInstanceFieldRef(body.getThisLocal(), getSetM().makeRef())));
		}

		return tmp;
	}

	/**
	 * Get the local representing set_m.
	 * 
	 * @return
	 */
	private Local getSetMLocal() {
		if (setMLocal == null) {
			setMLocal = loadSetMField();
		}
		return setMLocal;
	}

	/**
	 * Get the Soot field representing set_m.
	 * 
	 * @return
	 */
	private SootField getSetM() {
		if (setM == null) {
			ensureSetMExists();
		}

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
			Local local = loadField(field.getDeclaringClass().getFieldByName(setFName(field)));
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
		storeField(field.getDeclaringClass().getFieldByName(setFName(field)), from);
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
			cls.addField(new SootField(name, field.getType(), field.getModifiers()));
		}
	}

	/**
	 * Ensures that the declaring class of the given Soot method declares the
	 * set_m field.
	 * 
	 */
	private void ensureSetMExists() {
		SootClass cls = method.getDeclaringClass();
		String name = setMName();

		if (!cls.declaresFieldByName(name)) {
			cls.addField(new SootField(name, Scene.v().getObjectType(), method.getModifiers()));
			setM = cls.getFieldByName(setMName());
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

	/**
	 * The name of the set_m field for the given Soot method. We're using the
	 * index of the method here instead of its name because it could be an
	 * overloaded method. In such case, using the name will raise an exception
	 * when we try to add a field for the 2nd overload of the method because a
	 * field with the name set_m_methodname has already been added to the class.
	 * 
	 * @return
	 */
	private String setMName() {
		return Names.SET_METHOD_PREFIX + method.getDeclaringClass().getMethods().indexOf(method);
	}
}
