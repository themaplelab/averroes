package averroes.frameworks.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.Type;
import soot.Value;

/**
 * Common base class for all jimple body creators. This should be sub-classed by
 * any analysis that wants to provide it's own method code generator.
 * 
 * @author Karim Ali
 *
 */
public abstract class JimpleBodyCreator {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected SootMethod method;

	protected JimpleBodyCreator(SootMethod method) {
		this.method = method;
	}

	/**
	 * Build an expression for the given type. This depends on the underlying
	 * analysis:
	 * <ul>
	 * <li>for RTA => RTA.set</li>
	 * <li>for XTA => set_m or set_f</li>
	 * <li>for CFA => ???</li>
	 * </ul>
	 * 
	 * @param type
	 * @return
	 */
	public abstract Value buildExpression(Type type);

	/**
	 * Generate the code for the underlying Soot method.
	 */
	public abstract void generateCode();

}
