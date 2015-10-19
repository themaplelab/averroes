package averroes.frameworks.analysis;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.RefLikeType;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NopStmt;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StmtSwitch;
import soot.jimple.TableSwitchStmt;
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
		localGenerator = new LocalGenerator(body);

		// Insert header of Jimple method body (identity statements, call to
		// super constructor, etc)
		// insertStandardJimpleBodyHeader();
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
	protected abstract Value buildExpression(Type type);

	/**
	 * Generate the code for the underlying Soot method.
	 */
	public void generateCode() {
		// Loop over all the original method statements and transform them
		// appropriately.
		original.getActiveBody().getUnits().forEach(u -> u.apply(new StmtSwitch() {
			@Override
			public void caseInvokeStmt(InvokeStmt stmt) {
				InvokeExpr expr = transformInvokeExpr(stmt.getInvokeExpr());
				body.getUnits().add(Jimple.v().newInvokeStmt(expr));
			}

			@Override
			public void caseAssignStmt(AssignStmt stmt) {
				// TODO Auto-generated method stub

			}

			@Override
			public void caseReturnStmt(ReturnStmt stmt) {
				ReturnStmt ret = transformReturnStmt(stmt);
				body.getUnits().add(ret);
			}

			@Override
			public void caseThrowStmt(ThrowStmt stmt) {
				// TODO Auto-generated method stub
			}

			@Override
			public void caseBreakpointStmt(BreakpointStmt stmt) {
				defaultCase(stmt);
			}

			@Override
			public void caseIdentityStmt(IdentityStmt stmt) {
				defaultCase(stmt);
			}

			@Override
			public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
				defaultCase(stmt);
			}

			@Override
			public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
				defaultCase(stmt);
			}

			@Override
			public void caseGotoStmt(GotoStmt stmt) {
				defaultCase(stmt);
			}

			@Override
			public void caseIfStmt(IfStmt stmt) {
				defaultCase(stmt);
			}

			@Override
			public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
				defaultCase(stmt);
			}

			@Override
			public void caseNopStmt(NopStmt stmt) {
				defaultCase(stmt);
			}

			@Override
			public void caseRetStmt(RetStmt stmt) {
				defaultCase(stmt);
			}

			@Override
			public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
				defaultCase(stmt);
			}

			@Override
			public void caseTableSwitchStmt(TableSwitchStmt stmt) {
				defaultCase(stmt);
			}

			@Override
			public void defaultCase(Object obj) {
				if (obj instanceof Stmt) {
					body.getUnits().add((Stmt) obj);
				}
			}
		}));

		// Assign method parameters, including "this, if available, to the
		// appropriate set
		assignMethodParameters();

		// Validate method Jimple body
		body.validate();
	}

	/**
	 * Transform a return statement from the original method to its equivalent
	 * according the definition of the library model.
	 * 
	 * @param stmt
	 * @return
	 */
	protected ReturnStmt transformReturnStmt(ReturnStmt stmt) {
		Value op = buildExpression(stmt.getOp().getType());
		return Jimple.v().newReturnStmt(op);
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
		List<Value> args = callee.getParameterTypes().stream().map(this::buildExpression).collect(Collectors.toList());

		// Build the invoke expression
		if (originalInvokeExpr instanceof StaticInvokeExpr) {
			invokeExpr = Jimple.v().newStaticInvokeExpr(callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof SpecialInvokeExpr) {
			Local base = (Local) buildExpression(((SpecialInvokeExpr) originalInvokeExpr).getBase().getType());
			invokeExpr = Jimple.v().newSpecialInvokeExpr(base, callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof InterfaceInvokeExpr) {
			Local base = (Local) buildExpression(((InterfaceInvokeExpr) originalInvokeExpr).getBase().getType());
			invokeExpr = Jimple.v().newInterfaceInvokeExpr(base, callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof VirtualInvokeExpr) {
			Local base = (Local) buildExpression(((VirtualInvokeExpr) originalInvokeExpr).getBase().getType());
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
				.forEach(l -> newUnits.add(Jimple.v().newAssignStmt(buildExpression(l.getType()), l)));

		// and the "this" parameter for non-static methods
		if (!method.isStatic()) {
			newUnits.add(Jimple.v().newAssignStmt(buildExpression(body.getThisLocal().getType()), body.getThisLocal()));
		}

		// Insert those assignment statements after the first non-identity
		// statement
		body.getUnits().insertAfter(newUnits, body.getFirstNonIdentityStmt());
	}
}
