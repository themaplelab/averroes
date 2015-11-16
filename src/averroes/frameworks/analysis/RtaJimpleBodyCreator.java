package averroes.frameworks.analysis;

import soot.Local;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.Jimple;
import averroes.frameworks.soot.ClassWriter;
import averroes.frameworks.soot.CodeGenerator;
import averroes.frameworks.soot.Names;

/**
 * RTA Jimple body creator that over-approximates all objects in the library by
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
		averroesRta = CodeGenerator.createEmptyClass(Names.RTA_CLASS,
				Modifier.PUBLIC, Scene.v().getObjectType().getSootClass());

		// Add a default constructor to it
		CodeGenerator.createEmptyDefaultConstructor(averroesRta);

		// Add static field "set" to the class
		CodeGenerator.createField(averroesRta, Names.RTA_SET_FIELD_NAME, Scene
				.v().getObjectType(), Modifier.PUBLIC | Modifier.STATIC);

		// Write it to disk
		ClassWriter.writeLibraryClassFile(averroesRta);
	}

	public RtaJimpleBodyCreator(SootMethod method) {
		super(method);
	}

	@Override
	public FieldRef set() {
		return Jimple.v().newStaticFieldRef(
				Scene.v().getField(Names.RTA_SET_FIELD_SIGNATURE).makeRef());
	}

	@Override
	public Local setAsRightOp() {
		if (rtaLocal == null) {
			rtaLocal = loadStaticField(Scene.v().getField(
					Names.RTA_SET_FIELD_SIGNATURE));
		}

		return rtaLocal;
	}

	@Override
	protected void transformFieldRead(AssignStmt stmt) {
		swapWith(stmt, Jimple.v().newNopStmt());
	}

	@Override
	protected void transformFieldWrite(AssignStmt stmt) {
		swapWith(stmt, Jimple.v().newNopStmt());
	}
}
