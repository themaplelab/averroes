package averroes.frameworks.analysis;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.RefLikeType;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.ThrowStmt;
import soot.jimple.VirtualInvokeExpr;
import soot.util.Chain;
import soot.util.HashChain;

/**
 * Common base class for all jimple body creators. This should be sub-classed by
 * any analysis that wants to provide it's own method code generator.
 * 
 * @author Karim Ali
 *
 */
public abstract class JimpleBodyCreator {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected SootMethod original;
	protected SootMethod method;
	protected JimpleBody body;
	protected LocalGenerator localGenerator;

	protected JimpleBodyCreator(SootMethod original) {
		this.original = original;
		this.method = new SootMethod(original.getName(), original.getParameterTypes(), original.getReturnType(),
				original.getModifiers(), original.getExceptions());
		this.body = Jimple.v().newBody(method);
		this.method.setActiveBody(body);
		this.localGenerator = new LocalGenerator(body);
		// this.localSetOf =
		// localGenerator.generateLocal(Scene.v().getObjectType());

		// Insert header of Jimple method body (identity statements, call to
		// super constructor, etc)
		// insertStandardJimpleBodyHeader();
	}

	/**
	 * The corresponding pt-set of the given value. This depends on the
	 * underlying analysis:
	 * <ul>
	 * <li>for RTA => RTA.set</li>
	 * <li>for XTA => set_m or set_f</li>
	 * <li>for CFA => ???</li>
	 * </ul>
	 * 
	 * @param value
	 * @return
	 */
	protected abstract Value setOf(Value value);

	/**
	 * Generate the code for the underlying Soot method.
	 */
	public void generateCode() {
		// Loop over all the original method statements and transform them
		// appropriately.
		original.getActiveBody().getUnits().forEach(u -> u.apply(new AbstractStmtSwitch() {
			@Override
			public void caseIdentityStmt(IdentityStmt stmt) {
				body.getUnits().add(Jimple.v().newIdentityStmt(stmt.getLeftOp(), stmt.getRightOp()));
			}

			@Override
			public void caseAssignStmt(AssignStmt stmt) {
				body.getUnits().add(Jimple.v().newAssignStmt(setOf(stmt.getLeftOp()), setOf(stmt.getRightOp())));

				// if (isArrayRead(stmt)) {
				// body.getUnits().add(Jimple.v().newAssignStmt(setOf(method),
				// setOf(stmt.getRightOp().getType())));
				// }
				//
				// if (isArrayWrite(stmt)) {
				// body.getUnits().add(Jimple.v().newAssignStmt(setOf(stmt.getRightOp().getType()),
				// setOf(method)));
				// }
				//
				// if (isFieldRead(stmt)) {
				// body.getUnits().add(
				// Jimple.v().newAssignStmt(setOf(method), setOf(((FieldRef)
				// stmt.getRightOp()).getField())));
				// }
				//
				// if (isFieldWrite(stmt)) {
				// body.getUnits().add(
				// Jimple.v().newAssignStmt(setOf(((FieldRef)
				// stmt.getRightOp()).getField()),
				// Jimple.v().newCastExpr(setOf(method),
				// stmt.getRightOp().getType())));
				// }
			}

			@Override
			public void caseThrowStmt(ThrowStmt stmt) {
				body.getUnits().add(Jimple.v().newThrowStmt(setOf(stmt.getOp())));
			}

			@Override
			public void caseInvokeStmt(InvokeStmt stmt) {
				InvokeExpr expr = transformInvokeExpr(stmt.getInvokeExpr());
				body.getUnits().add(Jimple.v().newInvokeStmt(expr));
			}

			@Override
			public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
				body.getUnits().add(Jimple.v().newReturnVoidStmt());
			}

			@Override
			public void caseReturnStmt(ReturnStmt stmt) {
				body.getUnits().add(Jimple.v().newReturnStmt(setOf(stmt.getOp())));
			}
		}));

		// Assign method parameters, including "this, if available, to the
		// appropriate set
		assignMethodParameters();

		// Validate method Jimple body
		body.validate();
	}

	/**
	 * Is this assignment an array read?
	 * 
	 * @param assign
	 * @return
	 */
	protected boolean isArrayRead(AssignStmt assign) {
		return assign.getRightOp() instanceof ArrayRef;
	}

	/**
	 * Is this assignment an array write?
	 * 
	 * @param assign
	 * @return
	 */
	protected boolean isArrayWrite(AssignStmt assign) {
		return assign.getLeftOp() instanceof ArrayRef;
	}

	/**
	 * Is this assignment a field read?
	 * 
	 * @param assign
	 * @return
	 */
	protected boolean isFieldRead(AssignStmt assign) {
		return assign.getRightOp() instanceof FieldRef;
	}

	/**
	 * Is this assignment a field write?
	 * 
	 * @param assign
	 * @return
	 */
	protected boolean isFieldWrite(AssignStmt assign) {
		return assign.getLeftOp() instanceof FieldRef;
	}

	/**
	 * Transform an invoke expression from the original method to its equivalent
	 * according the definition of the library model.
	 * 
	 * @param originalInvokeExpr
	 * @return
	 */
	protected InvokeExpr transformInvokeExpr(InvokeExpr originalInvokeExpr) {
		InvokeExpr invokeExpr = null;
		SootMethod callee = originalInvokeExpr.getMethod();

		// Get the arguments to the call
		List<Value> args = originalInvokeExpr.getArgs().stream().map(this::setOf).collect(Collectors.toList());

		// Build the invoke expression
		if (originalInvokeExpr instanceof StaticInvokeExpr) {
			invokeExpr = Jimple.v().newStaticInvokeExpr(callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof SpecialInvokeExpr) {
			Local base = (Local) setOf(((SpecialInvokeExpr) originalInvokeExpr).getBase());
			invokeExpr = Jimple.v().newSpecialInvokeExpr(base, callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof InterfaceInvokeExpr) {
			Local base = (Local) setOf(((InterfaceInvokeExpr) originalInvokeExpr).getBase());
			invokeExpr = Jimple.v().newInterfaceInvokeExpr(base, callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof VirtualInvokeExpr) {
			Local base = (Local) setOf(((VirtualInvokeExpr) originalInvokeExpr).getBase());
			invokeExpr = Jimple.v().newVirtualInvokeExpr(base, callee.makeRef(), args);
		} else {
			logger.error("Cannot handle invoke expression of type: " + originalInvokeExpr.getClass());
		}

		return invokeExpr;
	}

	/**
	 * Construct Jimple code that load the given static field and assign it to a
	 * new temporary local variable.
	 * 
	 * @param field
	 * @return
	 */
	protected Local loadStaticField(SootField field) {
		Local tmp = localGenerator.generateLocal(field.getType());
		body.getUnits().add(Jimple.v().newAssignStmt(tmp, Jimple.v().newStaticFieldRef(field.makeRef())));
		return tmp;
	}

	/**
	 * Construct Jimple code that assigns method parameters, including the
	 * "this" parameter, if available.
	 */
	protected void assignMethodParameters() {
		Chain<Unit> newUnits = new HashChain<Unit>();

		// Loop over all parameters of reference type and create an assignment
		// statement to the appropriate "expression".
		body.getParameterLocals().stream().filter(l -> l.getType() instanceof RefLikeType)
				.forEach(l -> newUnits.add(Jimple.v().newAssignStmt(setOf(l), l)));

		// and the "this" parameter for non-static methods
		if (!method.isStatic()) {
			newUnits.add(Jimple.v().newAssignStmt(setOf(body.getThisLocal()), body.getThisLocal()));
		}

		// Insert those assignment statements after the first non-identity
		// statement
		body.getUnits().insertAfter(newUnits, body.getFirstNonIdentityStmt());
	}
}
