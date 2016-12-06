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
	public static final String name = "xta";
	
	private Local xtaGuard = null;

	private SootField setM = null;
	private Local setMLocal = null;

	private HashMap<SootField, SootField> setF = new HashMap<SootField, SootField>();
	private HashMap<SootField, Local> setFLocal = new HashMap<SootField, Local>();

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
	protected void storeToSet(Value from) {
		storeField(getSetM(), from);
	}

	@Override
	protected Local getGuard() {
		if (xtaGuard == null) {
			xtaGuard = loadField(Scene.v().getField(Names.XTA_GUARD_FIELD_SIGNATURE));
		}
		return xtaGuard;
	}

	@Override
	protected void ensureCommonClassExists() {
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
		averroesXta.getMethods().forEach(m -> Printers.printJimple(PrinterType.GENERATED, m));
		// ClassWriter.writeLibraryClassFile(averroesXta);
	}

	@Override
	protected void handleFields() {
		fieldReads.forEach(f -> {
			storeToSet(getSetFLocal(f));
		});

		fieldWrites.forEach(f -> {
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
			setMLocal = loadField(getSetM());
		}

		return setMLocal;
	}

	/**
	 * This basically special cases the loadField grammar chunk for the set_m
	 * and set_f field of the underlying method. For those loads, use the "this"
	 * variable instead of the regular cast as long as the method is not static.
	 * Otherwise, cyclic calls happen and the flying spaghetti monster will show
	 * up on your doorstep.
	 * 
	 * @param field
	 * @return
	 */
	@Override
	protected Value getFieldRef(SootField field) {
		if (field.isStatic()) {
			return Jimple.v().newStaticFieldRef(field.makeRef());
		} else if ((field.equals(getSetM()) || setF.containsValue(field)) && !method.isStatic()) {
			return Jimple.v().newInstanceFieldRef(body.getThisLocal(), field.makeRef());
		} else {
			return Jimple.v().newInstanceFieldRef(getCompatibleValue(field.getDeclaringClass().getType()),
					field.makeRef());
		}
	}

	/**
	 * Get the local representing the set_f field of the given Soot field.
	 * 
	 * @param field
	 * @return
	 */
	private Local getSetFLocal(SootField field) {
		if (!setFLocal.containsKey(field)) {
			setFLocal.put(field, loadField(getSetF(field)));
		}

		return setFLocal.get(field);
	}

	/**
	 * Get the Soot field representing the set_m field of the underlying Soot
	 * method.
	 * 
	 * @return
	 */
	private SootField getSetM() {
		if (setM == null) {
			ensureSetMExists();
			setM = method.getDeclaringClass().getFieldByName(setMName());
		}

		return setM;
	}

	/**
	 * Get the Soot field representing the set_f field of the given Soot field.
	 * 
	 * @param field
	 * @return
	 */
	private SootField getSetF(SootField field) {
		if (!setF.containsKey(field)) {
			// ensureSetFExists(field);
			// setF.put(field,
			// field.getDeclaringClass().getFieldByName(setFName(field)));
			setF.put(field, field.getDeclaringClass().getFieldByName(field.getName()));
		}

		return setF.get(field);
	}

	/**
	 * Model the statement set_f = (Type(f) set_m)
	 * 
	 * @param field
	 */
	private void storeToSetF(SootField field) {
		Value from = getCompatibleValue(getSetF(field).getType());
		storeField(getSetF(field), from);
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
	 * Ensures that the declaring class of the given Soot method declares the
	 * set_m field.
	 * 
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
