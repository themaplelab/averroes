package averroes.frameworks.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.FloatType;
import soot.IntegerType;
import soot.Local;
import soot.LongType;
import soot.PrimType;
import soot.RefLikeType;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AnyNewExpr;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.DoubleConstant;
import soot.jimple.FieldRef;
import soot.jimple.FloatConstant;
import soot.jimple.IdentityStmt;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LongConstant;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
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
public abstract class TypeBasedJimpleBodyCreator {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected SootMethod original;
	protected SootMethod method;
	protected JimpleBody body;
	protected LocalGenerator localGenerator;

	protected Map<SootField, Local> fieldToPtSet;

	protected static IntConstant ARRAY_LENGTH = IntConstant.v(1);
	protected static IntConstant ARRAY_INDEX = IntConstant.v(0);

	protected TypeBasedJimpleBodyCreator(SootMethod m) {
		original = m;
		method = new SootMethod(original.getName(), original.getParameterTypes(), original.getReturnType(),
				original.getModifiers(), original.getExceptions());
		body = Jimple.v().newBody(method);
		method.setActiveBody(body);
		localGenerator = new LocalGenerator(body);

		fieldToPtSet = new HashMap<SootField, Local>();
	}

	protected abstract Local set();

	/**
	 * The corresponding pt-set of the given value. This depends on the
	 * underlying analysis:
	 * <ul>
	 * <li>for RTA => RTA.set</li>
	 * <li>for XTA => set_m or set_f</li>
	 * <li>for CFA => ???</li>
	 * </ul>
	 * 
	 * @param type
	 * @return
	 */
	protected Value setOf(Type type) {
		if (type instanceof PrimType) {
			return getPrimValue((PrimType) type);
		} else {
			return insertCastStatement(set(), type);
		}
	}

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
				if (isNewExpr(stmt)) {
					transformNewExpr(stmt);
				} else if (isArrayRead(stmt)) {
					transformArrayRead(stmt);
				} else if (isArrayWrite(stmt)) {
					transformArrayWrite(stmt);
				} else if (isFieldRead(stmt)) {
					transformFieldRead(stmt);
				} else if (isFieldWrite(stmt)) {
					transformFieldWrite(stmt);
				}

			}

			@Override
			public void caseThrowStmt(ThrowStmt stmt) {
				body.getUnits().add(Jimple.v().newThrowStmt(setOf(stmt.getOp().getType())));
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
				body.getUnits().add(Jimple.v().newReturnStmt(setOf(original.getReturnType())));
			}
		}));

		// Assign method parameters, including "this, if available, to the
		// appropriate set
		assignMethodParameters();

		// Validate method Jimple body
		body.validate();
	}
	
	/**
	 * Transform an array read statement.
	 * 
	 * @param stmt
	 */
	protected void transformFieldRead(AssignStmt stmt) {
	}

	/**
	 * Transform an array write statement.
	 * 
	 * @param stmt
	 */
	protected void transformFieldWrite(AssignStmt stmt) {
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
	 * Transform an array read statement.
	 * 
	 * @param stmt
	 */
	private void transformArrayRead(AssignStmt stmt) {
		body.getUnits().add(
				Jimple.v().newAssignStmt(set(),
						Jimple.v().newArrayRef(Jimple.v().newCastExpr(set(), set().getType()), ARRAY_INDEX)));
	}

	/**
	 * Transform an array write statement.
	 * 
	 * @param stmt
	 */
	private void transformArrayWrite(AssignStmt stmt) {
		body.getUnits().add(
				Jimple.v().newAssignStmt(
						Jimple.v().newArrayRef(Jimple.v().newCastExpr(set(), set().getType()), ARRAY_INDEX), set()));
	}

	/**
	 * Transform object creation statements (regular "new", new Array[], and new
	 * MultiArray[][]).
	 * 
	 * @param stmt
	 */
	private void transformNewExpr(AssignStmt stmt) {
		if (stmt.getRightOp() instanceof NewExpr) {
			NewExpr n = (NewExpr) stmt.getRightOp();
			Value rvalue = Jimple.v().newNewExpr(n.getBaseType());
			body.getUnits().add(Jimple.v().newAssignStmt(set(), rvalue));
		} else if (stmt.getRightOp() instanceof NewArrayExpr) {
			NewArrayExpr n = (NewArrayExpr) stmt.getRightOp();
			Value rvalue = Jimple.v().newNewArrayExpr(n.getBaseType(), ARRAY_LENGTH);
			body.getUnits().add(Jimple.v().newAssignStmt(set(), rvalue));
		} else if (stmt.getRightOp() instanceof NewMultiArrayExpr) {
			NewMultiArrayExpr n = (NewMultiArrayExpr) stmt.getRightOp();
			Value rvalue = Jimple.v().newNewMultiArrayExpr(n.getBaseType(),
					Collections.nCopies(n.getSizeCount(), ARRAY_LENGTH));
			body.getUnits().add(Jimple.v().newAssignStmt(set(), rvalue));
		}
	}

	/**
	 * Return a constant value corresponding to the primary type.
	 * 
	 * @param type
	 * @return
	 */
	private Value getPrimValue(PrimType type) {
		if (type instanceof IntegerType) {
			return IntConstant.v(1);
		} else if (type instanceof LongType) {
			return LongConstant.v(1);
		} else if (type instanceof FloatType) {
			return FloatConstant.v(1);
		} else {
			return DoubleConstant.v(1);
		}
	}

	/**
	 * Insert a statement that casts the given local variable to the given type
	 * and assign it to a new temporary local variable.
	 * 
	 * @param local
	 * @param type
	 * @return temporary variable that holds the result of the cast expression
	 */
	private Local insertCastStatement(Local local, Type type) {
		Local tmp = localGenerator.generateLocal(type);
		body.getUnits().add(Jimple.v().newAssignStmt(tmp, Jimple.v().newCastExpr(local, type)));
		return tmp;
	}

	/**
	 * Is this assignment an object creation?
	 * 
	 * @param assign
	 * @return
	 */
	private boolean isNewExpr(AssignStmt assign) {
		return assign.getRightOp() instanceof AnyNewExpr;
	}

	/**
	 * Is this assignment an array read?
	 * 
	 * @param assign
	 * @return
	 */
	private boolean isArrayRead(AssignStmt assign) {
		return assign.getRightOp() instanceof ArrayRef;
	}

	/**
	 * Is this assignment an array write?
	 * 
	 * @param assign
	 * @return
	 */
	private boolean isArrayWrite(AssignStmt assign) {
		return assign.getLeftOp() instanceof ArrayRef;
	}

	/**
	 * Is this assignment a field read?
	 * 
	 * @param assign
	 * @return
	 */
	private boolean isFieldRead(AssignStmt assign) {
		return assign.getRightOp() instanceof FieldRef;
	}

	/**
	 * Is this assignment a field write?
	 * 
	 * @param assign
	 * @return
	 */
	private boolean isFieldWrite(AssignStmt assign) {
		return assign.getLeftOp() instanceof FieldRef;
	}

	/**
	 * Transform an invoke expression from the original method to its equivalent
	 * according the definition of the library model.
	 * 
	 * @param originalInvokeExpr
	 * @return
	 */
	private InvokeExpr transformInvokeExpr(InvokeExpr originalInvokeExpr) {
		InvokeExpr invokeExpr = null;
		SootMethod callee = originalInvokeExpr.getMethod();

		// Get the arguments to the call
		List<Value> args = originalInvokeExpr.getArgs().stream().map(a -> setOf(a.getType()))
				.collect(Collectors.toList());

		// Build the invoke expression
		if (originalInvokeExpr instanceof StaticInvokeExpr) {
			invokeExpr = Jimple.v().newStaticInvokeExpr(callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof SpecialInvokeExpr) {
			Local base = (Local) setOf(((SpecialInvokeExpr) originalInvokeExpr).getBase().getType());
			invokeExpr = Jimple.v().newSpecialInvokeExpr(base, callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof InterfaceInvokeExpr) {
			Local base = (Local) setOf(((InterfaceInvokeExpr) originalInvokeExpr).getBase().getType());
			invokeExpr = Jimple.v().newInterfaceInvokeExpr(base, callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof VirtualInvokeExpr) {
			Local base = (Local) setOf(((VirtualInvokeExpr) originalInvokeExpr).getBase().getType());
			invokeExpr = Jimple.v().newVirtualInvokeExpr(base, callee.makeRef(), args);
		} else {
			logger.error("Cannot handle invoke expression of type: " + originalInvokeExpr.getClass());
		}

		return invokeExpr;
	}

	/**
	 * Construct Jimple code that assigns method parameters, including the
	 * "this" parameter, if available.
	 */
	private void assignMethodParameters() {
		Chain<Unit> newUnits = new HashChain<Unit>();

		// Loop over all parameters of reference type and create an assignment
		// statement to the appropriate "expression".
		body.getParameterLocals().stream().filter(l -> l.getType() instanceof RefLikeType)
				.forEach(l -> newUnits.add(Jimple.v().newAssignStmt(set(), l)));

		// and the "this" parameter for non-static methods
		if (!method.isStatic()) {
			newUnits.add(Jimple.v().newAssignStmt(set(), body.getThisLocal()));
		}

		// Insert those assignment statements after the first non-identity
		// statement
		body.getUnits().insertAfter(newUnits, body.getFirstNonIdentityStmt());
	}
}
