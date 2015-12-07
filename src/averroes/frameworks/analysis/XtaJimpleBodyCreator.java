package averroes.frameworks.analysis;

import soot.Local;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.Jimple;

/**
 * XTA Jimple body creator that overapproximates objects in each method in the
 * library by just one set represented by a local variable set_m.
 * 
 * @author Karim Ali
 *
 */
@Deprecated
public class XtaJimpleBodyCreator extends TypeBasedJimpleBodyCreator {

	// private final Logger logger = LoggerFactory.getLogger(getClass());

	// private static SootClass averroesXta;

	private Local xtaLocal = null;

	// // Create the singleton XTA class
	// static {
	// // Create a public class and set its super class to java.lang.Object
	// averroesXta = CodeGenerator.createEmptyClass(Names.XTA_CLASS,
	// Modifier.PUBLIC, Scene.v().getObjectType()
	// .getSootClass());
	//
	// // Add a default constructor to it
	// CodeGenerator.createEmptyDefaultConstructor(averroesXta);
	//
	// // Add static field "set" to the class
	// CodeGenerator.createField(averroesXta, Names.INT_FIELD_NAME, IntType.v(),
	// Modifier.PUBLIC | Modifier.STATIC);
	//
	// // TODO: initialize all the static fields
	//
	// // Write it to disk
	// ClassWriter.writeLibraryClassFile(averroesXta);
	// }

	public XtaJimpleBodyCreator(SootMethod method) {
		super(method);
	}

	@Override
	protected Local set() {
		if (xtaLocal == null) {
			xtaLocal = localGenerator.generateLocal(Scene.v().getObjectType());
		}

		return xtaLocal;
	}

	@Override
	protected Local setAsRightOp() {
		return set();
	}

	@Override
	protected void transformFieldRead(AssignStmt stmt) {
		SootField fr = ((FieldRef) stmt.getRightOp()).getField();
		swapWith(
				stmt,
				Jimple.v().newAssignStmt(
						set(),
						fieldToPtSet.getOrDefault(fr,
								localGenerator.generateLocal(fr.getType()))));
	}

	@Override
	protected void transformFieldWrite(AssignStmt stmt) {
		SootField fr = ((FieldRef) stmt.getLeftOp()).getField();
		swapWith(
				stmt,
				Jimple.v().newAssignStmt(
						fieldToPtSet.getOrDefault(fr,
								localGenerator.generateLocal(fr.getType())),
						set()));
	}
}
