package averroes.util;

import averroes.soot.LocalVariableRenamer;
import averroes.soot.LocalVariableSorter;
import averroes.soot.UnnecessaryMethodCallEliminator;
import soot.Body;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.scalar.NopEliminator;
import soot.toolkits.scalar.UnusedLocalEliminator;

/**
 * Some utility methods for Soot.
 * 
 * @author Karim Ali
 *
 */
public class SootUtils {

	/**
	 * Perform some code cleanup.
	 * <ul>
	 * <li>{@link UnusedLocalEliminator} removes local variables that are
	 * defined but never used.</li>
	 * <li>{@link LocalVariableSorter} sorts local variables according to their
	 * first use/def in the method body.</li>
	 * <li>{@link LocalNameStandardizer} standardizes the names of local
	 * variables (including "this" and method parameters. This makes Jimple code
	 * comparisons easier.</li>
	 * <li>using {@link NopEliminator} eliminates the NOP statements introduced
	 * by guards.</li>
	 * <li>using {@link LocalVariableRenamer} cleans up names of local variables
	 * to enable text-base comparisons of Jimple files.</li>
	 * </ul>
	 * 
	 * @param body
	 */
	public static void cleanup(Body body) {
//		UnnecessaryMethodCallEliminator.transform(body);
//		UnusedLocalEliminator.v().transform(body);
		LocalVariableSorter.transform(body);
		LocalNameStandardizer.v().transform(body);
		NopEliminator.v().transform(body);
		LocalVariableRenamer.transform(body);
	}

}
