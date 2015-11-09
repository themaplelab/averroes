package averroes.frameworks.analysis;

import java.util.List;
import java.util.stream.Collectors;

import soot.Local;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.FieldRef;
import soot.jimple.Jimple;
import soot.util.Chain;
import soot.util.HashChain;

/**
 * XTA Jimple body creator that overapproximates objects in each method in the
 * library by just one set represented by a local variable set_m.
 * 
 * @author Karim Ali
 *
 */
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

	/**
	 * Handle all field reads and writes.
	 */
	@Override
	protected void handleFields() {
		Chain<Unit> newUnits = new HashChain<Unit>();

		getFieldReads().forEach(
				fr -> newUnits.add(Jimple.v().newAssignStmt(set(),
						fieldToPtSet.getOrDefault(fr, localGenerator.generateLocal(fr.getType())))));

		getFieldWrites()
				.forEach(
						fw -> newUnits.add(Jimple.v().newAssignStmt(
								fieldToPtSet.getOrDefault(fw, localGenerator.generateLocal(fw.getType())),
								setOf(fw.getType()))));

		body.getUnits().insertBefore(newUnits, body.getUnits().getLast());
	}

	/**
	 * Get all field reads.
	 * 
	 * @return
	 */
	private List<SootField> getFieldReads() {
		return original.getActiveBody().getUseBoxes().stream().filter(vb -> vb.getValue() instanceof FieldRef)
				.map(vb -> ((FieldRef) vb).getField()).distinct().collect(Collectors.toList());
	}

	/**
	 * Get all field writes.
	 * 
	 * @return
	 */
	private List<SootField> getFieldWrites() {
		return original.getActiveBody().getDefBoxes().stream().filter(vb -> vb.getValue() instanceof FieldRef)
				.map(vb -> ((FieldRef) vb).getField()).distinct().collect(Collectors.toList());
	}
}
