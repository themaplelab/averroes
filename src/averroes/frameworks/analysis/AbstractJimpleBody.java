package averroes.frameworks.analysis;

import java.util.HashMap;
import java.util.Map;

import soot.FloatType;
import soot.IntegerType;
import soot.Local;
import soot.LongType;
import soot.PrimType;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AnyNewExpr;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.DoubleConstant;
import soot.jimple.FieldRef;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LongConstant;
import soot.jimple.SpecialInvokeExpr;

/**
 * Abstract Jimple body creator that declares some common fields and methods.
 * 
 * @author Karim Ali
 *
 */
public abstract class AbstractJimpleBody {
	protected SootMethod method;
	protected JimpleBody body;
	protected LocalGenerator localGenerator;

	// protected Map<Type, Local> casts;

	protected Map<Unit, Unit> swap;
	protected Map<Unit, Unit> insertAfter;
	protected Map<Unit, Unit> insertBefore;

	protected static IntConstant ARRAY_LENGTH = IntConstant.v(1);
	protected static IntConstant ARRAY_INDEX = IntConstant.v(0);

	/**
	 * Generate the code for the underlying Soot method.
	 */
	public abstract void generateCode();

	/**
	 * Create a new type-based Jimple body creator for method M.
	 * 
	 * @param method
	 */
	protected AbstractJimpleBody(SootMethod method) {
		this.method = method;
		body = (JimpleBody) method.retrieveActiveBody();
		localGenerator = new LocalGenerator(body);

		// casts = new HashMap<Type, Local>();

		swap = new HashMap<Unit, Unit>();
		insertAfter = new HashMap<Unit, Unit>();
		insertBefore = new HashMap<Unit, Unit>();
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
		insertBefore(toInsert, insertPoint());
	}

	/**
	 * Insert a new unit after a given point.
	 * 
	 * @param in
	 * @param out
	 */
	protected void insertAfter(Unit toInsert, Unit point) {
		insertAfter.put(toInsert, point);
	}

	/**
	 * Insert a new unit before a given point.
	 * 
	 * @param in
	 * @param out
	 */
	protected void insertBefore(Unit toInsert, Unit point) {
		insertBefore.put(toInsert, point);
	}

	/**
	 * Insert a statement that casts the given local to the given type and
	 * assign it to a new temporary local variable.
	 * 
	 * @param local
	 * @param type
	 * @return temporary variable that holds the result of the cast expression
	 */
	protected Local insertCastStatement(Local local, Type type, Unit point) {
		// if (!casts.keySet().contains(type)) {
		Local tmp = localGenerator.generateLocal(type);
		insertBefore(Jimple.v().newAssignStmt(tmp, Jimple.v().newCastExpr(local, type)), point);
		// casts.put(type, tmp);
		// }
		return tmp;

		// return casts.get(type);
	}

	/**
	 * The unit before which we insert new statements (mainly casts). If it's a
	 * constructor then, the first non-identity statement is usually the call to
	 * the super constructor. Statements should be inserted after that one.
	 * Otherwise, the insert point is the first non-identity statement.
	 * 
	 * @return
	 */
	protected Unit insertPoint() {
		if (method.isConstructor()) {
			return body.getUnits().getSuccOf(body.getFirstNonIdentityStmt());
		} else {
			return body.getFirstNonIdentityStmt();
		}
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
		insert(Jimple.v().newAssignStmt(tmp, Jimple.v().newStaticFieldRef(field.makeRef())));
		return tmp;
	}

	/**
	 * Construct Jimple code that load the given static field before a given
	 * program point and assign it to a new temporary local variable.
	 * 
	 * @param field
	 * @param point
	 * @return
	 */
	protected Local loadStaticField(SootField field, Unit point) {
		Local tmp = localGenerator.generateLocal(field.getType());
		insertBefore(Jimple.v().newAssignStmt(tmp, Jimple.v().newStaticFieldRef(field.makeRef())), point);
		return tmp;
	}

	/**
	 * Store the given local field to a static soot field.
	 * 
	 * @param field
	 * @param from
	 */
	protected void storeStaticField(SootField field, Value from) {
		insert(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(field.makeRef()), from));
	}

	/**
	 * Store the given local field to a static soot field after a given program
	 * point.
	 * 
	 * @param field
	 * @param from
	 * @param point
	 */
	protected void storeStaticField(SootField field, Value from, Unit point) {
		insertAfter(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(field.makeRef()), from), point);
	}

	/**
	 * Is this the call to a super constructor?
	 * 
	 * @param stmt
	 * @return
	 */
	protected boolean isCallToSuperConstructor(InvokeStmt stmt) {
		return method.isConstructor() && body.getFirstNonIdentityStmt().equals(stmt)
				&& stmt.getInvokeExpr() instanceof SpecialInvokeExpr;
	}

	/**
	 * Is this assignment a method invocation?
	 * 
	 * @param assign
	 * @return
	 */
	protected boolean isAssignInvoke(AssignStmt assign) {
		return assign.getRightOp() instanceof InvokeExpr;
	}

	/**
	 * Is this assignment an object creation?
	 * 
	 * @param assign
	 * @return
	 */
	protected boolean isNewExpr(AssignStmt assign) {
		return assign.getRightOp() instanceof AnyNewExpr;
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
	 * Return a constant value corresponding to the primary type.
	 * 
	 * @param type
	 * @return
	 */
	protected Value getPrimValue(PrimType type) {
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
}
