package averroes.frameworks.analysis;

import soot.Local;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import averroes.frameworks.soot.ClassWriter;
import averroes.frameworks.soot.CodeGenerator;
import averroes.frameworks.soot.Names;

/**
 * RTA Jimple body creator that overapproximates all objects in the library by
 * just one set represented by the field RTA.set in the newly generated class
 * RTA.
 * 
 * @author Karim Ali
 *
 */
public class RtaJimpleBodyCreator extends TypeBasedJimpleBodyCreator {

	// private final Logger logger = LoggerFactory.getLogger(getClass());

	private static SootClass averroesRta;

	private Local rtaLocal = null;

	// Create the singleton RTA class
	static {
		// Create a public class and set its super class to java.lang.Object
		averroesRta = CodeGenerator.createEmptyClass(Names.RTA_CLASS, Modifier.PUBLIC, Scene.v().getObjectType()
				.getSootClass());

		// Add a default constructor to it
		CodeGenerator.createEmptyDefaultConstructor(averroesRta);

		// Add static field "set" to the class
		CodeGenerator.createField(averroesRta, Names.RTA_SET_FIELD_NAME, Scene.v().getObjectType(), Modifier.PUBLIC
				| Modifier.STATIC);

		// Write it to disk
		ClassWriter.writeLibraryClassFile(averroesRta);
	}

	public RtaJimpleBodyCreator(SootMethod method) {
		super(method);
	}

	@Override
	public Local set() {
		if (rtaLocal == null) {
			rtaLocal = loadStaticField(Scene.v().getField(Names.RTA_SET_FIELD_SIGNATURE));
		}

		return rtaLocal;
	}
}
