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
	public Local setToCast() {
		return getRtaSet();
	}

	@Override
	public void storeToSet(Value from) {
		storeField(Scene.v().getField(Names.RTA_SET_FIELD_SIGNATURE), from);
	}

	@Override
	public Local getGuard() {
		if (rtaGuard == null) {
			rtaGuard = loadField(Scene.v().getField(
					Names.RTA_GUARD_FIELD_SIGNATURE));
		}
		return rtaGuard;
	}

	@Override
	public void ensureCommonClassExists() {
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
				m -> Printers.printJimple(PrinterType.GENERATED, m));
		// ClassWriter.writeLibraryClassFile(averroesRta);
	}

	@Override
	public void handleFields() {

	}

	/**
	 * Load the RTA.set field into a local variable, if not loaded before.
	 * 
	 * @return
	 */
	private Local getRtaSet() {
		if (rtaSet == null) {
			rtaSet = loadField(Scene.v()
					.getField(Names.RTA_SET_FIELD_SIGNATURE));
		}
		return rtaSet;
	}
}
