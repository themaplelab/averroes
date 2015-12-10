package averroes.frameworks.analysis;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ArrayType;
import soot.BooleanType;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.PrimType;
import soot.RefLikeType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.VoidType;
import soot.jimple.ArrayRef;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import averroes.frameworks.soot.ClassWriter;
import averroes.frameworks.soot.CodeGenerator;
import averroes.soot.Names;
import averroes.util.io.Printers;

/**
 * RTA Jimple body creator that over-approximates all objects in the library by
 * just one set represented by the field RTA.set in the newly generated class
 * RTA.
 * 
 * @author Karim Ali
 *
 */
public class RtaJimpleBody extends AbstractJimpleBody {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private Local rtaSet = null;

	/**
	 * Create a new RTA Jimple body creator for method M.
	 * 
	 * @param method
	 */
	public RtaJimpleBody(SootMethod method) {
		super(method);
	}

	@Override
	public void generateCode() {
		// TODO
		Printers.getPrintStream().println("==========================");
		Printers.getPrintStream().println("BEFORE transformation");
		Printers.getPrintStream().println("==========================");
		Printers.getPrintStream().println(method.retrieveActiveBody());

		// Create RTA Class
		ensureRtaClassExists();

		// Create the new Jimple body
		insertJimpleBodyHeader();
		createObjects();
		callMethods();
		handleArrays();
		// handleExceptions();
		insertJimpleBodyFooter();

		// Validate method Jimple body & assign it to the method
		body.validate();
		method.setActiveBody(body);

		// TODO
		Printers.getPrintStream().println("==========================");
		Printers.getPrintStream().println("AFTER transformation");
		Printers.getPrintStream().println("==========================");
		Printers.getPrintStream().println(method.retrieveActiveBody());
		Printers.getPrintStream().println();
		Printers.getPrintStream().println();
	}

	/**
	 * Ensure that the RTA class has been created, along with its fields.
	 */
	private void ensureRtaClassExists() {
		if (Scene.v().containsClass(Names.RTA_CLASS)) {
			return;
		}

		// Create a public class and set its super class to java.lang.Object
		SootClass averroesRta = CodeGenerator.createEmptyClass(Names.RTA_CLASS,
				Modifier.PUBLIC, Scene.v().getObjectType().getSootClass());

		// Add a default constructor to it
		CodeGenerator.createEmptyDefaultConstructor(averroesRta);

		// Add static field "set" to the class
		CodeGenerator.createField(averroesRta, Names.SET_FIELD_NAME, Scene.v()
				.getObjectType(), Modifier.PUBLIC | Modifier.STATIC);
		CodeGenerator.createField(averroesRta, Names.INT_FIELD_NAME,
				IntType.v(), Modifier.PUBLIC | Modifier.STATIC);
		CodeGenerator.createField(averroesRta, Names.BOOLEAN_FIELD_NAME,
				BooleanType.v(), Modifier.PUBLIC | Modifier.STATIC);

		// Write it to disk
		ClassWriter.writeLibraryClassFile(averroesRta);
	}

	/**
	 * Create all the objects that the library could possible instantiate. For
	 * reference types, this includes inserting new statements, invoking
	 * constructors, and static initializers if found. For arrays, we just have
	 * the appropriate NEW instruction.
	 */
	private void createObjects() {
		objectCreations
				.forEach(e -> {
					SootMethod init = e.getMethod();
					SootClass cls = init.getDeclaringClass();
					Local obj = insertNewStmt(RefType.v(cls));
					insertSpecialInvokeStmt(obj, init);
					storeToRtaSet(obj);

					// Call <clinit> if found
					if (cls.declaresMethod(SootMethod.staticInitializerName)) {
						insertStaticInvokeStmt(cls
								.getMethodByName(SootMethod.staticInitializerName));
					}
				});

		arrayCreations.forEach(t -> {
			Local obj = insertNewStmt(t);
			storeToRtaSet(obj);
		});
	}

	/**
	 * Call the methods that are called in the original method body. Averroes
	 * preserves the fact that the return value of some method calls
	 * (invokeExprs) is stored locally, while it's not for other calls
	 * (invokeStmts).
	 */
	private void callMethods() {
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
	private void handleArrays() {
		if (readsArray || writesArray) {
			Local cast = (Local) getCompatibleValue(getRtaSet(),
					ArrayType.v(getRtaSet().getType(), ARRAY_LENGTH.value));
			ArrayRef arrayRef = Jimple.v().newArrayRef(cast, ARRAY_INDEX);

			if (readsArray) {
				body.getUnits().add(
						Jimple.v().newAssignStmt(getRtaSet(), arrayRef));
			} else {
				body.getUnits().add(
						Jimple.v().newAssignStmt(arrayRef, getRtaSet()));
			}
		}
	}

	/**
	 * Insert the identity statements, assigns actual parameters (if any) and
	 * the this parameter (if any) to RTA.set
	 */
	private void insertJimpleBodyHeader() {
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
					.getSuperclass().getMethod(Names.DEFAULT_CONSTRUCTOR_SIG));
		}

		assignMethodParameters();
	}

	/**
	 * Insert the standard footer for a library method.
	 */
	private void insertJimpleBodyFooter() {
		insertReturnStmt();
	}

	/**
	 * Insert the appropriate return statement.
	 */
	private void insertReturnStmt() {
		if (method.getReturnType() instanceof VoidType) {
			body.getUnits().add(Jimple.v().newReturnVoidStmt());
		} else {
			Value ret = getCompatibleValue(getRtaSet(), method.getReturnType());
			body.getUnits().add(Jimple.v().newReturnStmt(ret));
		}
	}

	/**
	 * Store a value to RTA.set
	 * 
	 * @param value
	 */
	private void storeToRtaSet(Value value) {
		storeStaticField(Scene.v().getField(Names.RTA_SET_FIELD_SIGNATURE),
				value);
	}

	/**
	 * Store the return value of a method call to RTA.set
	 * 
	 * @param value
	 */
	private void storeMethodCallReturn(InvokeExpr expr) {
		Local ret = localGenerator.generateLocal(expr.getMethod()
				.getReturnType());
		body.getUnits().add(Jimple.v().newAssignStmt(ret, expr));
		storeToRtaSet(ret);
	}

	/**
	 * Load the RTA.set field into a local variable, if not loaded before.
	 * 
	 * @return
	 */
	private Local getRtaSet() {
		if (rtaSet == null) {
			rtaSet = loadStaticField(Scene.v().getField(
					Names.RTA_SET_FIELD_SIGNATURE));
		}
		return rtaSet;
	}

	/**
	 * Construct Jimple code that assigns method parameters, including the
	 * "this" parameter, if available.
	 */
	private void assignMethodParameters() {
		// Assign the "this" parameter, if available
		if (!method.isStatic()) {
			storeToRtaSet(body.getThisLocal());
		}

		// Loop over all parameters of reference type and create an assignment
		// statement to the appropriate "expression".
		body.getParameterLocals().stream()
				.filter(l -> l.getType() instanceof RefLikeType)
				.forEach(l -> storeToRtaSet(l));
	}

	/**
	 * Find the compatible value to the given Soot type. If it's a primary type,
	 * a constant is returned. Otherwise, a cast to the given type from the
	 * RTA.set is returned.
	 * 
	 * @param type
	 * @return
	 */
	private Value getCompatibleValue(Local local, Type type) {
		if (type instanceof PrimType) {
			return getPrimValue((PrimType) type);
		} else {
			return insertCastStmt(local, type);
		}
	}

	/**
	 * Insert a special invoke statement.
	 * 
	 * @param base
	 * @param method
	 */
	protected void insertSpecialInvokeStmt(Local base, SootMethod method) {
		List<Value> args = method.getParameterTypes().stream()
				.map(p -> getCompatibleValue(getRtaSet(), p))
				.collect(Collectors.toList());
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
				.map(p -> getCompatibleValue(getRtaSet(), p))
				.collect(Collectors.toList());
		body.getUnits()
				.add(Jimple.v().newInvokeStmt(
						Jimple.v().newStaticInvokeExpr(method.makeRef(), args)));
	}

	/**
	 * Insert a new invoke statement based on info in the given original invoke
	 * experssion.
	 * 
	 * @param originalInvokeExpr
	 */
	protected void insertInvokeStmt(InvokeExpr originalInvokeExpr) {
		body.getUnits().add(
				Jimple.v().newInvokeStmt(buildInvokeExpr(originalInvokeExpr)));
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
	private InvokeExpr buildInvokeExpr(InvokeExpr originalInvokeExpr) {
		SootMethod callee = originalInvokeExpr.getMethod();
		InvokeExpr invokeExpr = null;

		// Get the arguments to the call
		List<Value> args = originalInvokeExpr.getArgs().stream()
				.map(a -> getCompatibleValue(getRtaSet(), a.getType()))
				.collect(Collectors.toList());

		// Build the invoke expression
		if (originalInvokeExpr instanceof StaticInvokeExpr) {
			invokeExpr = Jimple.v().newStaticInvokeExpr(callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof SpecialInvokeExpr) {
			Local base = (Local) getCompatibleValue(getRtaSet(),
					((SpecialInvokeExpr) originalInvokeExpr).getBase()
							.getType());
			invokeExpr = Jimple.v().newSpecialInvokeExpr(base,
					callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof InterfaceInvokeExpr) {
			Local base = (Local) getCompatibleValue(getRtaSet(),
					((InterfaceInvokeExpr) originalInvokeExpr).getBase()
							.getType());
			invokeExpr = Jimple.v().newInterfaceInvokeExpr(base,
					callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof VirtualInvokeExpr) {
			Local base = (Local) getCompatibleValue(getRtaSet(),
					((VirtualInvokeExpr) originalInvokeExpr).getBase()
							.getType());
			invokeExpr = Jimple.v().newVirtualInvokeExpr(base,
					callee.makeRef(), args);
		} else {
			logger.error("Cannot handle invoke expression of type: "
					+ originalInvokeExpr.getClass());
		}

		return invokeExpr;
	}
}
