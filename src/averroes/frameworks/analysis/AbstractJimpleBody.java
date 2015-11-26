package averroes.frameworks.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import soot.ArrayType;
import soot.FloatType;
import soot.IntegerType;
import soot.Local;
import soot.LongType;
import soot.PrimType;
import soot.RefLikeType;
import soot.RefType;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
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
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LongConstant;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.SpecialInvokeExpr;

/**
 * Abstract Jimple body creator that declares some common fields and methods.
 * 
 * @author Karim Ali
 *
 */
public abstract class AbstractJimpleBody {
	protected SootMethod method;
	protected JimpleBody originalBody;
	protected JimpleBody body;
	protected LocalGenerator localGenerator;

	protected static IntConstant ARRAY_LENGTH = IntConstant.v(1);
	protected static IntConstant ARRAY_INDEX = IntConstant.v(0);

	protected boolean readsArray = false;
	protected boolean writesArray = false;

	protected Map<Type, Local> casts;

	// Various constructs collected from processing the original method body.
	// Arrays created within the original method body.
	protected LinkedHashSet<Type> arrayCreations = new LinkedHashSet<Type>();

	// Objects, other than arrays, created within the original method body.
	protected LinkedHashSet<SpecialInvokeExpr> objectCreations = new LinkedHashSet<SpecialInvokeExpr>();

	// Invoke statements (i.e., return value not assigned to any local
	// variables)
	protected LinkedHashSet<InvokeExpr> invokeStmts = new LinkedHashSet<InvokeExpr>();

	// Invoke expression (i.e., return value is assigned to some local variable)
	protected LinkedHashSet<InvokeExpr> invokeExprs = new LinkedHashSet<InvokeExpr>();

	/**
	 * Generate the code for the underlying Soot method (which is assumed to be
	 * concrete).
	 */
	public abstract void generateCode();

	/**
	 * Create a new type-based Jimple body creator for method M.
	 * 
	 * @param method
	 */
	protected AbstractJimpleBody(SootMethod method) {
		this.method = method;
		originalBody = (JimpleBody) method.retrieveActiveBody();
		body = Jimple.v().newBody(method);
		localGenerator = new LocalGenerator(body);

		casts = new HashMap<Type, Local>();

		processOriginalMethodBody();
	}

	/**
	 * Scan the original method body for stuff we are looking for so that we
	 * loop over the instructions only once.
	 */
	private void processOriginalMethodBody() {
		originalBody.getUnits().forEach(u -> u.apply(new AbstractStmtSwitch() {
			@Override
			public void caseAssignStmt(AssignStmt stmt) {
				// array creations, reads, and writes
				if (stmt.getRightOp() instanceof NewArrayExpr || stmt.getRightOp() instanceof NewMultiArrayExpr) {
					arrayCreations.add(stmt.getRightOp().getType());
				} else if (!readsArray && isArrayRead(stmt)) {
					readsArray = true;
				} else if (!writesArray && isArrayWrite(stmt)) {
					writesArray = true;
				} else if (isAssignInvoke(stmt)) {
					invokeExprs.add((InvokeExpr) stmt.getRightOp());
				}
			}

			@Override
			public void caseInvokeStmt(InvokeStmt stmt) {
				if (isRelevantObjectCreation(stmt)) {
					objectCreations.add((SpecialInvokeExpr) stmt.getInvokeExpr());
				} else if (!isCallToSuperConstructor(stmt)) {
					invokeStmts.add(stmt.getInvokeExpr());
				}
			}
		}));
	}

	/**
	 * Insert a statement that casts the given local to the given type and
	 * assign it to a new temporary local variable.
	 * 
	 * @param local
	 * @param type
	 * @return temporary variable that holds the result of the cast expression
	 */
	protected Local insertCastStmt(Local local, Type type) {
		if (!casts.keySet().contains(type)) {
			Local tmp = localGenerator.generateLocal(type);
			body.getUnits().add(Jimple.v().newAssignStmt(tmp, Jimple.v().newCastExpr(local, type)));
			casts.put(type, tmp);
		}
		// return tmp;

		return casts.get(type);
	}

	/**
	 * Insert a NEW statement.
	 * 
	 * @param type
	 * @return
	 */
	protected Local insertNewStmt(Type type) {
		Local obj = localGenerator.generateLocal(type);
		body.getUnits().add(Jimple.v().newAssignStmt(obj, buildNewExpr(type)));
		return obj;
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
	 * Store the given local field to a static soot field.
	 * 
	 * @param field
	 * @param from
	 */
	protected void storeStaticField(SootField field, Value from) {
		body.getUnits().add(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(field.makeRef()), from));
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
	 * Is this a relevant object creation? (i.e., a call to a constructor that
	 * is not the constructor of the direct super class)
	 * 
	 * @param stmt
	 * @return
	 */
	protected boolean isRelevantObjectCreation(InvokeStmt stmt) {
		return stmt.getInvokeExpr() instanceof SpecialInvokeExpr && stmt.getInvokeExpr().getMethod().isConstructor()
				&& !originalBody.getFirstNonIdentityStmt().equals(stmt);
	}

	/**
	 * Is this a call to the super constructor?
	 * 
	 * @param stmt
	 * @return
	 */
	protected boolean isCallToSuperConstructor(InvokeStmt stmt) {
		return stmt.getInvokeExpr() instanceof SpecialInvokeExpr && stmt.getInvokeExpr().getMethod().isConstructor()
				&& originalBody.getFirstNonIdentityStmt().equals(stmt);
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
	 * Does the underlying method read an array in any of its statements?
	 * 
	 * @return
	 */
	protected boolean readsArray() {
		return originalBody.getUnits().stream().filter(u -> u instanceof AssignStmt).map(AssignStmt.class::cast)
				.filter(this::isArrayRead).findFirst().isPresent();
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
	 * Does the underlying method writes to an array in any of its statements?
	 * 
	 * @return
	 */
	protected boolean writesArray() {
		return originalBody.getUnits().stream().filter(u -> u instanceof AssignStmt).map(AssignStmt.class::cast)
				.filter(this::isArrayWrite).findFirst().isPresent();
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

	/**
	 * Return a list of all the object created through calls to constructors.
	 * This does not include array creations. Those are collected and handled
	 * somewhere else.
	 * 
	 * @return
	 */
	protected List<SpecialInvokeExpr> getObjectCreations() {
		return originalBody.getUnits().stream().filter(u -> u instanceof InvokeStmt).map(InvokeStmt.class::cast)
				.filter(s -> s.getInvokeExpr() instanceof SpecialInvokeExpr)
				.map(s -> SpecialInvokeExpr.class.cast(s.getInvokeExpr())).collect(Collectors.toList());
	}

	/**
	 * Return a list of all the created in the underlying method.
	 * 
	 * @return
	 */
	protected List<Type> getArrayCreations() {
		return originalBody.getUnits().stream().filter(u -> u instanceof AssignStmt).map(AssignStmt.class::cast)
				.filter(s -> s.getRightOp() instanceof NewArrayExpr || s.getRightOp() instanceof NewMultiArrayExpr)
				.map(s -> s.getRightOp().getType()).collect(Collectors.toList());
	}

	/**
	 * Get all the LHS local variables for the RefLikeType parameters of the
	 * newly created Jimple body.
	 * 
	 * @param method
	 * @return
	 */
	protected List<Local> getRefLikeParameterLocals() {
		List<Local> result = new ArrayList<Local>();
		for (int i = 0; i < body.getMethod().getParameterCount(); i++) {
			if (body.getMethod().getParameterType(i) instanceof RefLikeType) {
				result.add(body.getParameterLocal(i));
			}
		}
		return result;
	}

	/**
	 * Construct the appropriate NEW expression depending on the given Soot
	 * type. It handles RefType and ArrayType types.
	 * 
	 * @param type
	 * @return
	 */
	protected AnyNewExpr buildNewExpr(Type type) {
		if (type instanceof RefType) {
			return Jimple.v().newNewExpr((RefType) type);
		} else if (type instanceof ArrayType) {
			ArrayType arrayType = (ArrayType) type;
			if (arrayType.numDimensions <= 1) {
				return Jimple.v().newNewArrayExpr(arrayType.baseType, ARRAY_LENGTH);
			} else {
				return Jimple.v().newNewMultiArrayExpr(arrayType,
						Collections.nCopies(arrayType.numDimensions, ARRAY_LENGTH));
			}
		}

		throw new IllegalArgumentException("Type " + type + " cannot be instantiated.");
	}
}
