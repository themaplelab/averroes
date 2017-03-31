package averroes.frameworks.soot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.FieldRef;

/**
 * A phase that does some cleaning up of the generated models.
 * 
 * @author Karim Ali
 * 
 */
public class Cleanup {

	/**
	 * Perform cleanup for all classes in the model.
	 */
	public static void cleanupClasses() {
		removeUnusedFields();
	}

	/**
	 * Remove any fields that are not used in the generated model.
	 * 
	 * This will remove, for example, all private fields in the RTA model.
	 */
	private static void removeUnusedFields() {
		Set<SootField> usedFields = Scene.v().getApplicationClasses().stream().map(c -> c.getMethods())
				.flatMap(List::stream).filter(SootMethod::hasActiveBody).map(m -> m.getActiveBody().getUseAndDefBoxes())
				.flatMap(List::stream).map(vb -> vb.getValue()).filter(v -> v instanceof FieldRef)
				.map(v -> ((FieldRef) v).getField()).collect(Collectors.toSet());

		Scene.v().getApplicationClasses().stream().map(c -> new ArrayList<SootField>(c.getFields()))
				.flatMap(List::stream).filter(f -> f.isPrivate() && !usedFields.contains(f))
				.forEach(f -> f.getDeclaringClass().removeField(f));
	}
}
