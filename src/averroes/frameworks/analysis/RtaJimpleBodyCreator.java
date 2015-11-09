package averroes.frameworks.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
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
public class RtaJimpleBodyCreator extends JimpleBodyCreator {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static SootClass averroesRta;

	private boolean isRtaSetLoaded = false;

	private Local rtaLocal;

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
	public Value setOf(Value value) {
		if (!isRtaSetLoaded) {
			rtaLocal = loadStaticField(Scene.v().getField(Names.RTA_SET_FIELD_SIGNATURE));
			isRtaSetLoaded = true;
		}

		return rtaLocal;
	}
}
