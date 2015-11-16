package averroes.frameworks.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
import soot.Scene;
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
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.ThrowStmt;
import soot.jimple.VirtualInvokeExpr;

/**
 * Common base class for all Jimple body creators. This should be sub-classed by
 * any analysis that wants to provide it's own method code generator.
 * 
 * @author Karim Ali
 *
 */
public abstract class TypeBasedJimpleBodyCreator {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected SootMethod method;
	protected JimpleBody body;
	protected LocalGenerator localGenerator;

	protected Map<SootField, Local> fieldToPtSet;

	protected Map<Type, Local> casts;

	protected Map<Unit, Unit> swap;
	protected List<Unit> insert;

	protected static IntConstant ARRAY_LENGTH = IntConstant.v(1);
	protected static IntConstant ARRAY_INDEX = IntConstant.v(0);

	/**
	 * Create a new type-based Jimple body creator for method M.
	 * 
	 * @param method
	 */
	protected TypeBasedJimpleBodyCreator(SootMethod method) {
		this.method = method;
		body = (JimpleBody) method.retrieveActiveBody();
		localGenerator = new LocalGenerator(body);

		fieldToPtSet = new HashMap<SootField, Local>();

		casts = new HashMap<Type, Local>();

		swap = new HashMap<Unit, Unit>();
		insert = new LinkedList<Unit>();
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
			return insertCastStatement(type);
		}
	}

	/**
	 * Generate the code for the underlying Soot method.
	 */
	public void generateCode() {
		// TODO
		System.out.println("==========================");
		System.out.println("BEFORE transformation");
		System.out.println("==========================");
		System.out.println(body);

		// Loop over all the original method statements and transform them
		// appropriately.
		for (Iterator<Unit> iter = method.retrieveActiveBody().getUnits()
				.iterator(); iter.hasNext();) {
			Unit u = iter.next();

			u.apply(new AbstractStmtSwitch() {
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
					transformThrowStmt(stmt);
				}

				@Override
				public void caseInvokeStmt(InvokeStmt stmt) {
					if (!isCallToSuperConstructor(stmt)) {
						transformInvokeStmt(stmt);
					}
				}

				@Override
				public void caseReturnStmt(ReturnStmt stmt) {
					transformReturnStmt(stmt);
				}
			});
		}

		// Assign method parameters, including "this, if available, to the
		// appropriate set
		assignMethodParameters();

		// Swap and insert all the units in the buffer
		swap.keySet().forEach(u -> body.getUnits().swapWith(u, swap.get(u)));
		body.getUnits().insertBefore(insert, insertPoint());

		// Validate method Jimple body
		body.validate();

		// TODO
		System.out.println("==========================");
		System.out.println("AFTER transformation");
		System.out.println("==========================");
		System.out.println(body);
		System.out.println();
		System.out.println();
	}

	/**
	 * Is this the call to a super constructor?
	 * 
	 * @param stmt
	 * @return
	 */
	protected boolean isCallToSuperConstructor(InvokeStmt stmt) {
		return method.isConstructor()
				&& body.getFirstNonIdentityStmt().equals(stmt)
				&& stmt.getInvokeExpr() instanceof SpecialInvokeExpr;
	}

	/**
	 * The unit before which we insert new statements (mainly casts).
	 * 
	 * @return
	 */
	protected Unit insertPoint() {
		// If it's a constructor then, the first non-identity statement is
		// usually the call to the super constructor. Statements should be
		// inserted after that one. Otherwise, the insert point is the first
		// non-identity statement.
		if (method.isConstructor()) {
			return body.getUnits().getSuccOf(body.getFirstNonIdentityStmt());
		} else {
			return body.getFirstNonIdentityStmt();
		}
	}

	/**
	 * Replace "out" in the underlying method body with "in".
	 * 
	 * @param out
	 * @param in
	 */
	protected void swapWith(Unit out, Unit in) {
		swap.put(out, in);
	}

	/**
	 * Insert a new unit before the first non-identity statement.
	 * 
	 * @param in
	 * @param out
	 */
	protected void insert(Unit toInsert) {
		insert.add(toInsert);
	}

	/**
	 * Transform the given return statement.
	 * 
	 * @param stmt
	 */
	protected void transformReturnStmt(ReturnStmt stmt) {
		swapWith(stmt, Jimple.v().newReturnStmt(setOf(method.getReturnType())));
	}

	/**
	 * Transform the given invoke statement.
	 * 
	 * @param stmt
	 */
	protected void transformInvokeStmt(InvokeStmt stmt) {
		InvokeExpr expr = transformInvokeExpr(stmt.getInvokeExpr());
		swapWith(stmt, Jimple.v().newInvokeStmt(expr));
	}

	/**
	 * Transform the given throw statement.
	 * 
	 * @param stmt
	 */
	protected void transformThrowStmt(ThrowStmt stmt) {
		swapWith(stmt, Jimple.v().newThrowStmt(setOf(stmt.getOp().getType())));
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
		insert(Jimple.v().newAssignStmt(tmp,
				Jimple.v().newStaticFieldRef(field.makeRef())));
		return tmp;
	}

	/**
	 * Transform an array read statement.
	 * 
	 * @param stmt
	 */
	private void transformArrayRead(AssignStmt stmt) {
		swapWith(
				stmt,
				Jimple.v().newAssignStmt(
						set(),
						Jimple.v().newArrayRef(
								Jimple.v().newCastExpr(set(), set().getType()),
								ARRAY_INDEX)));
	}

	/**
	 * Transform an array write statement.
	 * 
	 * @param stmt
	 */
	private void transformArrayWrite(AssignStmt stmt) {
		swapWith(
				stmt,
				Jimple.v().newAssignStmt(
						Jimple.v().newArrayRef(
								Jimple.v().newCastExpr(set(), set().getType()),
								ARRAY_INDEX), set()));
	}

	/**
	 * Transform object creation statements (regular "new", new Array[], and new
	 * MultiArray[][]).
	 * 
	 * @param stmt
	 */
	private void transformNewExpr(AssignStmt stmt) {
		AssignStmt in = null;

		if (stmt.getRightOp() instanceof NewExpr) {
			NewExpr n = (NewExpr) stmt.getRightOp();
			Value rvalue = Jimple.v().newNewExpr(n.getBaseType());
			in = Jimple.v().newAssignStmt(set(), rvalue);
		} else if (stmt.getRightOp() instanceof NewArrayExpr) {
			NewArrayExpr n = (NewArrayExpr) stmt.getRightOp();
			Value rvalue = Jimple.v().newNewArrayExpr(n.getBaseType(),
					ARRAY_LENGTH);
			in = Jimple.v().newAssignStmt(set(), rvalue);
		} else if (stmt.getRightOp() instanceof NewMultiArrayExpr) {
			NewMultiArrayExpr n = (NewMultiArrayExpr) stmt.getRightOp();
			Value rvalue = Jimple.v().newNewMultiArrayExpr(n.getBaseType(),
					Collections.nCopies(n.getSizeCount(), ARRAY_LENGTH));
			in = Jimple.v().newAssignStmt(set(), rvalue);
		}

		swapWith(stmt, in);
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
	 * Insert a statement that casts set() to the given type and assign it to a
	 * new temporary local variable.
	 * 
	 * @param type
	 * @return temporary variable that holds the result of the cast expression
	 */
	private Local insertCastStatement(Type type) {
		if (!casts.keySet().contains(type)) {
			Local tmp = localGenerator.generateLocal(type);
			insert(Jimple.v().newAssignStmt(tmp,
					Jimple.v().newCastExpr(set(), type)));
			casts.put(type, tmp);
		}

		return casts.get(type);
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
		List<Value> args = originalInvokeExpr.getArgs().stream()
				.map(a -> setOf(a.getType())).collect(Collectors.toList());

		// Build the invoke expression
		if (originalInvokeExpr instanceof StaticInvokeExpr) {
			invokeExpr = Jimple.v().newStaticInvokeExpr(callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof SpecialInvokeExpr) {
			Local base = (Local) setOf(((SpecialInvokeExpr) originalInvokeExpr)
					.getBase().getType());
			invokeExpr = Jimple.v().newSpecialInvokeExpr(base,
					callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof InterfaceInvokeExpr) {
			Local base = (Local) setOf(((InterfaceInvokeExpr) originalInvokeExpr)
					.getBase().getType());
			invokeExpr = Jimple.v().newInterfaceInvokeExpr(base,
					callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof VirtualInvokeExpr) {
			Local base = (Local) setOf(((VirtualInvokeExpr) originalInvokeExpr)
					.getBase().getType());
			invokeExpr = Jimple.v().newVirtualInvokeExpr(base,
					callee.makeRef(), args);
		} else {
			logger.error("Cannot handle invoke expression of type: "
					+ originalInvokeExpr.getClass());
		}

		return invokeExpr;
	}

	/**
	 * Construct Jimple code that assigns method parameters, including the
	 * "this" parameter, if available.
	 */
	private void assignMethodParameters() {
		// Chain<Unit> newUnits = new HashChain<Unit>();

		// Loop over all parameters of reference type and create an assignment
		// statement to the appropriate "expression".
		body.getParameterLocals().stream()
				.filter(l -> l.getType() instanceof RefLikeType)
				.forEach(l -> insert(Jimple.v().newAssignStmt(set(), l)));

		// and the "this" parameter for java.lang.Object to capture all objects
		// created throughout the program
		if (method.isConstructor()
				&& method.getDeclaringClass().equals(
						Scene.v().getObjectType().getSootClass())) {
			insert(Jimple.v().newAssignStmt(set(), body.getThisLocal()));
		}

		// Insert those assignment statements before the first non-identity
		// statement
		// body.getUnits().insertBefore(newUnits,
		// body.getFirstNonIdentityStmt());
	}
}
