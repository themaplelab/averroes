package averroes.frameworks.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ArrayType;
import soot.DoubleType;
import soot.FloatType;
import soot.Local;
import soot.LongType;
import soot.PrimType;
import soot.RefLikeType;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Trap;
import soot.Type;
import soot.Value;
import soot.VoidType;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AnyNewExpr;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.DoubleConstant;
import soot.jimple.EqExpr;
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
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NopStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.scalar.NopEliminator;
import soot.toolkits.scalar.UnusedLocalEliminator;
import averroes.soot.Names;
import averroes.util.io.Printers;
import averroes.util.io.Printers.PrinterType;

/**
 * Abstract Jimple body creator that declares some common fields and methods.
 * 
 * @author Karim Ali
 *
 */
public abstract class AbstractJimpleBody {
	private final Logger logger = LoggerFactory.getLogger(getClass());

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

	// Thrown types
	protected LinkedHashSet<Type> throwables = new LinkedHashSet<Type>();

	// Checked exceptions (declared in the method signature)
	protected LinkedHashSet<SootClass> checkedExceptions = new LinkedHashSet<SootClass>();

	// Field reads
	protected LinkedHashSet<SootField> fieldReads = new LinkedHashSet<SootField>();

	// Field writes
	protected LinkedHashSet<SootField> fieldWrites = new LinkedHashSet<SootField>();

	/**
	 * Generate the code for the underlying Soot method (which is assumed to be
	 * concrete).
	 */
	public void generateCode() {
		Printers.print(PrinterType.ORIGINAL, method);

		// Create Common Class
		ensureCommonClassExists();

		// Create the new Jimple body
		insertJimpleBodyHeader();
		createObjects();
		callMethods();
		handleArrays();
		handleFields();
		handleExceptions();
		insertJimpleBodyFooter();

		// Cleanup the generated body
		cleanup();

		// Validate method Jimple body & assign it to the method
		body.validate();
		method.setActiveBody(body);

		Printers.print(PrinterType.GENERATED, method);
	}

	/**
	 * Handle field reads and writes.
	 */
	public abstract void handleFields();

	/**
	 * Get the set that will be cast to a certain type for various operations.
	 * This is RTA.set for the RTA analysis and set_m for XTA.
	 * 
	 * @return
	 */
	public abstract Local setToCast();

	/**
	 * Store the given value to the set that is used to over-approximate objects
	 * in the library. This could be set_m for XTA or RTA.set for RTA.
	 * 
	 * @param from
	 */
	public abstract void storeToSet(Value from);

	/**
	 * Load the guard field that is used to guard conditionals. See
	 * {@link #insertAndGuardStmt(Stmt) for more details}.
	 * 
	 * @return
	 */
	public abstract Local getGuard();

	/**
	 * Ensure that the common class has been created, along with its fields. For
	 * example, this class is rta.RTA for the RTA analysis.
	 */
	public abstract void ensureCommonClassExists();

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
	 * Perform some code cleanup.
	 * <ul>
	 * <li>{@link UnusedLocalEliminator} removes local variables that are
	 * defined but never used.
	 * <li>{@link LocalNameStandardizer} standardizes the names of local
	 * variables (including "this" and method parameters. This makes Jimple code
	 * comparisons easier.</li>
	 * <li>using {@link NopEliminator} eliminates the NOP statements introduced
	 * by guards.</li>
	 * </ul>
	 */
	protected void cleanup() {
//		UnusedLocalEliminator.v().transform(body);
		LocalNameStandardizer.v().transform(body);
		NopEliminator.v().transform(body);
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
				if (stmt.getRightOp() instanceof NewArrayExpr
						|| stmt.getRightOp() instanceof NewMultiArrayExpr) {
					arrayCreations.add(stmt.getRightOp().getType());
				} else if (isFieldRead(stmt)
						&& stmt.getRightOp().getType() instanceof RefLikeType) {
					fieldReads.add(((FieldRef) stmt.getRightOp()).getField());
				} else if (isFieldWrite(stmt)
						&& stmt.getLeftOp().getType() instanceof RefLikeType) {
					fieldWrites.add(((FieldRef) stmt.getLeftOp()).getField());
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
					objectCreations.add((SpecialInvokeExpr) stmt
							.getInvokeExpr());
				} else if (!isCallToSuperConstructor(stmt)) {
					invokeStmts.add(stmt.getInvokeExpr());
				}
			}

			@Override
			public void caseThrowStmt(ThrowStmt stmt) {
				throwables.add(stmt.getOp().getType());
			}
		}));

		processTraps();
	}

	/**
	 * Process the traps of a method (i.e., catch blocks) to simulate the
	 * creation and assignment of the caught-exception to a local variable that
	 * could be used in the catch block.
	 */
	private void processTraps() {
		originalBody.getTraps().stream().map(Trap::getException)
				.forEach(checkedExceptions::add);
	}

	/**
	 * Insert the identity statements, and assign actual parameters (if any) and
	 * the this parameter (if any) to set_m
	 */
	protected void insertJimpleBodyHeader() {
		body.insertIdentityStmts();

		/*
		 * To generate correct bytecode, we need to initialize the object first
		 * by calling the direct superclass default constructor before inserting
		 * any more statements. That is if this method is for a constructor and
		 * its declaring class has a superclass.
		 */
		if (method.isConstructor()
				&& method.getDeclaringClass().hasSuperclass()) {
			Local base = body.getThisLocal();
			insertSpecialInvokeStmt(base, method.getDeclaringClass()
					.getSuperclass()
					.getMethod(Names.DEFAULT_CONSTRUCTOR_SUBSIG));
		}

		assignMethodParameters();
	}

	/**
	 * Insert the standard footer for a library method.
	 */
	protected void insertJimpleBodyFooter() {
		/*
		 * Insert the return statement, only if there are no exceptions to
		 * throw. Otherwise, it's dead code and the Jimple validator will choke
		 * on it!
		 */
		if (throwables.isEmpty()) {
			insertReturnStmt();
		}
	}

	/**
	 * Create all the objects that the library could possible instantiate. For
	 * reference types, this includes inserting new statements, invoking
	 * constructors, and static initializers if found. For arrays, we just have
	 * the appropriate NEW instruction. For checked exceptions, we create the
	 * object, call the constructor, and, if available, call the static
	 * initializer.
	 */
	protected void createObjects() {
		objectCreations.forEach(e -> {
			SootMethod init = e.getMethod();
			Local obj = createObjectByMethod(init);
			storeToSet(obj);
		});

		arrayCreations.forEach(t -> {
			Local obj = insertNewStmt(t);
			storeToSet(obj);
		});

		checkedExceptions.forEach(cls -> {
			SootMethod init = cls.getMethod(Names.DEFAULT_CONSTRUCTOR_SUBSIG);
			Local obj = createObjectByMethod(init);
			storeToSet(obj);
		});
	}

	/**
	 * Create an object by calling that specific constructor.
	 * 
	 * @param cls
	 * @param init
	 * @return
	 */
	protected Local createObjectByMethod(SootMethod init) {
		SootClass cls = init.getDeclaringClass();
		Local obj = insertNewStmt(RefType.v(cls));
		insertSpecialInvokeStmt(obj, init);

		// Call <clinit> if found
		if (cls.declaresMethod(SootMethod.staticInitializerName)) {
			insertStaticInvokeStmt(cls
					.getMethodByName(SootMethod.staticInitializerName));
		}

		return obj;
	}

	/**
	 * Call the methods that are called in the original method body. Averroes
	 * preserves the fact that the return value of some method calls
	 * (invokeExprs) is stored locally, while it's not for other calls
	 * (invokeStmts).
	 */
	protected void callMethods() {
		invokeStmts.forEach(this::insertInvokeStmt);

		invokeExprs.forEach(e -> {
			InvokeExpr expr = buildInvokeExpr(e);
			// Only store the return value if it's a reference, otherwise just
			// call the method.
				if (expr.getMethod().getReturnType() instanceof RefLikeType) {
					storeMethodCallReturn(expr);
				} else {
					insertInvokeStmt(expr);
				}
			});
	}

	/**
	 * Handle array reads and writes.
	 */
	protected void handleArrays() {
		if (readsArray || writesArray) {
			Local cast = (Local) getCompatibleValue(ArrayType.v(setToCast()
					.getType(), ARRAY_LENGTH.value));
			ArrayRef arrayRef = Jimple.v().newArrayRef(cast, ARRAY_INDEX);

			if (readsArray) {
				body.getUnits().add(
						Jimple.v().newAssignStmt(setToCast(), arrayRef));
			} else {
				body.getUnits().add(
						Jimple.v().newAssignStmt(arrayRef, setToCast()));
			}
		}
	}

	/**
	 * Handle throwing exceptions and try-catch blocks.
	 */
	protected void handleExceptions() {
		throwables.forEach(x -> insertThrowStmt(x, throwables.size() > 1));
	}

	/**
	 * Insert a statement that casts the given local to the given type and
	 * assign it to a new temporary local variable. If {@code local} is of the
	 * same type as {@code type}, then return that local instead of using a
	 * temporary variable.
	 * 
	 * @param local
	 * @param type
	 * @return temporary variable that holds the result of the cast expression
	 */
	protected Local insertCastStmt(Local local, Type type) {
		if (!casts.keySet().contains(type)) {
			if (local.getType().equals(type)) {
				casts.put(type, local);
			} else {
				Local tmp = localGenerator.generateLocal(type);
				body.getUnits().add(
						Jimple.v().newAssignStmt(tmp,
								Jimple.v().newCastExpr(local, type)));
				casts.put(type, tmp);
			}
		}

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
	 * Insert a new invoke statement based on info in the given original invoke
	 * expression.
	 * 
	 * @param originalInvokeExpr
	 */
	protected void insertInvokeStmt(InvokeExpr originalInvokeExpr) {
		body.getUnits().add(
				Jimple.v().newInvokeStmt(buildInvokeExpr(originalInvokeExpr)));
	}

	/**
	 * Insert a special invoke statement.
	 * 
	 * @param base
	 * @param method
	 */
	protected void insertSpecialInvokeStmt(Local base, SootMethod method) {
		List<Value> args = method.getParameterTypes().stream()
				.map(p -> getCompatibleValue(p)).collect(Collectors.toList());
		body.getUnits().add(
				Jimple.v().newInvokeStmt(
						Jimple.v().newSpecialInvokeExpr(base, method.makeRef(),
								args)));
	}

	/**
	 * Insert a static invoke statement.
	 * 
	 * @param method
	 */
	protected void insertStaticInvokeStmt(SootMethod method) {
		List<Value> args = method.getParameterTypes().stream()
				.map(p -> getCompatibleValue(p)).collect(Collectors.toList());
		body.getUnits()
				.add(Jimple.v().newInvokeStmt(
						Jimple.v().newStaticInvokeExpr(method.makeRef(), args)));
	}

	/**
	 * Insert a throw statement that throws an exception of the given type.
	 * 
	 * @param type
	 */
	protected void insertThrowStmt(Type type, boolean guard) {
		Local tmp = (Local) getCompatibleValue(type);
		if (guard) {
			insertAndGuardStmt(Jimple.v().newThrowStmt(tmp));
		} else {
			body.getUnits().add(Jimple.v().newThrowStmt(tmp));
		}
	}

	/**
	 * Insert the appropriate return statement.
	 */
	protected void insertReturnStmt() {
		if (method.getReturnType() instanceof VoidType) {
			body.getUnits().add(Jimple.v().newReturnVoidStmt());
		} else {
			Value ret = getCompatibleValue(method.getReturnType());
			body.getUnits().add(Jimple.v().newReturnStmt(ret));
		}
	}

	/**
	 * Guard a statement by an if-statement whose condition always evaluates to
	 * true. This helps inserting multiple {@link ThrowStmt}, for example, in a
	 * Jimple method.
	 * 
	 * @param stmt
	 * @return
	 */
	protected void insertAndGuardStmt(Stmt stmt) {
		// TODO: this condition can produce dead code. That's why we should use
		// the RTA.guard field as a condition instead.
		// NeExpr cond = Jimple.v().newNeExpr(IntConstant.v(1),
		// IntConstant.v(1));
		EqExpr cond = Jimple.v().newEqExpr(getGuard(), IntConstant.v(0));
		NopStmt nop = Jimple.v().newNopStmt();

		body.getUnits().add(Jimple.v().newIfStmt(cond, nop));
		body.getUnits().add(stmt);
		body.getUnits().add(nop);
	}

	/**
	 * Store the return value of a method call to {@link #storeToSet(Value)}
	 * 
	 * @param value
	 */
	protected void storeMethodCallReturn(InvokeExpr expr) {
		Local ret = localGenerator.generateLocal(expr.getMethod()
				.getReturnType());
		body.getUnits().add(Jimple.v().newAssignStmt(ret, expr));
		storeToSet(ret);
	}

	/**
	 * Construct Jimple code that loads a given field and assigns it to a new
	 * temporary local variable.
	 * 
	 * @param field
	 * @return
	 */
	protected Local loadField(SootField field) {
		Local tmp = localGenerator.generateLocal(field.getType());

		if (field.isStatic()) {
			body.getUnits().add(
					Jimple.v().newAssignStmt(tmp,
							Jimple.v().newStaticFieldRef(field.makeRef())));
		} else {
			body.getUnits().add(
					Jimple.v().newAssignStmt(
							tmp,
							Jimple.v().newInstanceFieldRef(
									getCompatibleValue(field
											.getDeclaringClass().getType()),
									field.makeRef())));
		}

		return tmp;
	}

	/**
	 * Store the given value to a Soot field.
	 * 
	 * @param field
	 * @param from
	 */
	protected void storeField(SootField field, Value from) {
		if (field.isStatic()) {
			body.getUnits().add(
					Jimple.v()
							.newAssignStmt(
									Jimple.v().newStaticFieldRef(
											field.makeRef()), from));
		} else {
			body.getUnits().add(
					Jimple.v().newAssignStmt(
							Jimple.v().newInstanceFieldRef(
									getCompatibleValue(field
											.getDeclaringClass().getType()),
									field.makeRef()), from));
		}
	}

	/**
	 * Construct Jimple code that assigns method parameters, including the
	 * "this" parameter, if available, to {@link #storeToSet(Value)}.
	 */
	protected void assignMethodParameters() {
		// Assign the "this" parameter, if available
		if (!method.isStatic()) {
			storeToSet(body.getThisLocal());
		}

		// Loop over all parameters of reference type and create an assignment
		// statement to the appropriate "expression".
		body.getParameterLocals().stream()
				.filter(l -> l.getType() instanceof RefLikeType)
				.forEach(l -> storeToSet(l));
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
		return stmt.getInvokeExpr() instanceof SpecialInvokeExpr
				&& stmt.getInvokeExpr().getMethod().isConstructor()
				&& !isCallToSuperConstructor(stmt);
		// && !originalBody.getFirstNonIdentityStmt().equals(stmt);
	}

	/**
	 * Is this a call to the super constructor?
	 * 
	 * @param stmt
	 * @return
	 */
	protected boolean isCallToSuperConstructor(InvokeStmt stmt) {
		return !method.getDeclaringClass().hasSuperclass() ? false : stmt
				.getInvokeExpr() instanceof SpecialInvokeExpr
				&& method.isConstructor()
				&& stmt.getInvokeExpr().getMethod().isConstructor()
				&& method.getDeclaringClass().getSuperclass().getMethods()
						.contains(stmt.getInvokeExpr().getMethod());
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
		return originalBody.getUnits().stream()
				.filter(u -> u instanceof AssignStmt)
				.map(AssignStmt.class::cast).filter(this::isArrayRead)
				.findFirst().isPresent();
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
		return originalBody.getUnits().stream()
				.filter(u -> u instanceof AssignStmt)
				.map(AssignStmt.class::cast).filter(this::isArrayWrite)
				.findFirst().isPresent();
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
	 * Find the compatible value to the given Soot type. If it's a primary type,
	 * a constant is returned. Otherwise, the methods returns a cast of {@link
	 * setToCast()} to the given type.
	 * 
	 * @param type
	 * @return
	 */
	protected Value getCompatibleValue(Type type) {
		if (type instanceof PrimType) {
			return getPrimValue((PrimType) type);
		} else {
			return insertCastStmt(setToCast(), type);
		}
	}

	/**
	 * Return a constant value corresponding to the primary type.
	 * 
	 * @param type
	 * @return
	 */
	protected Value getPrimValue(PrimType type) {
		if (type instanceof LongType) {
			return LongConstant.v(1);
		} else if (type instanceof FloatType) {
			return FloatConstant.v(1);
		} else if (type instanceof DoubleType) {
			return DoubleConstant.v(1);
		} else {
			return IntConstant.v(1);
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
		return originalBody.getUnits().stream()
				.filter(u -> u instanceof InvokeStmt)
				.map(InvokeStmt.class::cast)
				.filter(s -> s.getInvokeExpr() instanceof SpecialInvokeExpr)
				.map(s -> SpecialInvokeExpr.class.cast(s.getInvokeExpr()))
				.collect(Collectors.toList());
	}

	/**
	 * Return a list of all the created in the underlying method.
	 * 
	 * @return
	 */
	protected List<Type> getArrayCreations() {
		return originalBody
				.getUnits()
				.stream()
				.filter(u -> u instanceof AssignStmt)
				.map(AssignStmt.class::cast)
				.filter(s -> s.getRightOp() instanceof NewArrayExpr
						|| s.getRightOp() instanceof NewMultiArrayExpr)
				.map(s -> s.getRightOp().getType())
				.collect(Collectors.toList());
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
				return Jimple.v().newNewArrayExpr(arrayType.baseType,
						ARRAY_LENGTH);
			} else {
				return Jimple.v().newNewMultiArrayExpr(
						arrayType,
						Collections.nCopies(arrayType.numDimensions,
								ARRAY_LENGTH));
			}
		}

		throw new IllegalArgumentException("Type " + type
				+ " cannot be instantiated.");
	}

	/**
	 * Build the grammar of an invoke expression based on the given original
	 * invoke expression. This method does not insert the grammar chunk into the
	 * method body. It only inserts any code needed to prepare the arguments to
	 * the call (i.e., casts of RTA.set).
	 * 
	 * @param originalInvokeExpr
	 * @return
	 */
	protected InvokeExpr buildInvokeExpr(InvokeExpr originalInvokeExpr) {
		SootMethod callee = originalInvokeExpr.getMethod();
		InvokeExpr invokeExpr = null;

		// Get the arguments to the call
		List<Value> args = originalInvokeExpr.getArgs().stream()
				.map(a -> getCompatibleValue(a.getType()))
				.collect(Collectors.toList());

		// Build the invoke expression
		if (originalInvokeExpr instanceof StaticInvokeExpr) {
			invokeExpr = Jimple.v().newStaticInvokeExpr(callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof SpecialInvokeExpr) {
			Local base = (Local) getCompatibleValue(((SpecialInvokeExpr) originalInvokeExpr)
					.getBase().getType());
			invokeExpr = Jimple.v().newSpecialInvokeExpr(base,
					callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof InterfaceInvokeExpr) {
			Local base = (Local) getCompatibleValue(((InterfaceInvokeExpr) originalInvokeExpr)
					.getBase().getType());
			invokeExpr = Jimple.v().newInterfaceInvokeExpr(base,
					callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof VirtualInvokeExpr) {
			Local base = (Local) getCompatibleValue(((VirtualInvokeExpr) originalInvokeExpr)
					.getBase().getType());
			invokeExpr = Jimple.v().newVirtualInvokeExpr(base,
					callee.makeRef(), args);
		} else {
			logger.error("Cannot handle invoke expression of type: " + originalInvokeExpr.getClass());
		}

		return invokeExpr;
	}
}
