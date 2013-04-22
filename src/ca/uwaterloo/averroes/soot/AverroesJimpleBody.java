package ca.uwaterloo.averroes.soot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.ArrayType;
import soot.FloatType;
import soot.IntegerType;
import soot.Local;
import soot.LongType;
import soot.PrimType;
import soot.RefLikeType;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.VoidType;
import soot.jimple.AnyNewExpr;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.InstanceFieldRef;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LongConstant;

/**
 * A representation of the {@link JimpleBody} Averroes generates for all the placeholder library methods.
 * 
 * @author karim
 * 
 */
public class AverroesJimpleBody {
	private JimpleBody body;
	private Local lpt;
	private Local fpt;
	private Set<Local> invokeReturnVariables;
	private LocalVariableNumberer numberer;
	private Map<Type, Local> lptCastToType;

	/**
	 * Construct a new Jimple body for an Averroes library method.
	 * 
	 * @param method
	 */
	public AverroesJimpleBody(SootMethod method) {
		createBasicJimpleBody(method);
		lpt = null;
		fpt = null;
		invokeReturnVariables = new HashSet<Local>();
		numberer = new LocalVariableNumberer();
		lptCastToType = new HashMap<Type, Local>();
	}

	/**
	 * Create a basic Jimple body for a method.
	 * 
	 * @param method
	 * @return
	 */
	private void createBasicJimpleBody(SootMethod method) {
		body = Jimple.v().newBody(method);
		method.setActiveBody(body);
		insertStandardJimpleBodyHeader();
	}

	/**
	 * Insert the identity statements, assigns actual parameters (if any) and the this parameter (if any) to the LPT.
	 */
	private void insertStandardJimpleBodyHeader() {
		body.insertIdentityStmts();

		/*
		 * To generate correct bytecode, we need to initialize the object first by calling the superclass constructor
		 * before inserting any more statements. That is if this method is for a constructor.
		 */
		if (isConstructor()) {
			Local base = body.getThisLocal();

			// Call the default constructor of the direct superclass, except for the constructor of java.lang.Object
			if (!Hierarchy.v().isDeclaredInJavaLangObject(body.getMethod())) {
				insertSpecialInvokeStatement(base, Hierarchy.v()
						.getDirectSuperclassDefaultConstructor(body.getMethod()));
			}
		}

		assignActualParametersToLpt();
		assignThisParameter();
	}

	/**
	 * Insert the standard footer for a library method: calling the doItAll method then the return statement.
	 */
	public void insertStandardJimpleBodyFooter() {
		insertInvocationStmtToDoItAll();
		insertReturnStmt();
	}

	/**
	 * Insert an invocation to the doItAll Library method.
	 */
	private void insertInvocationStmtToDoItAll() {
		SootMethod toCall = CodeGenerator.v().getAverroesDoItAll();
		body.getUnits().add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(toCall.makeRef())));
	}

	/**
	 * Insert the appropriate return statement at the end of the underlying Jimple body.
	 */
	public void insertReturnStmt() {
		SootMethod method = body.getMethod();
		Type retType = method.getReturnType();

		if (retType instanceof VoidType) {
			body.getUnits().addLast(Jimple.v().newReturnVoidStmt());
		} else {
			Value ret = getCompatibleValue(retType);
			body.getUnits().addLast(Jimple.v().newReturnStmt(ret));
		}
	}

	/**
	 * Assign actual parameters to LPT. This will make them flow from the application to the library.
	 * 
	 * @param body
	 */
	private void assignActualParametersToLpt() {
		List<Local> params = getRefLikeParameterLocals();
		for (Local param : params) {
			storeLibraryPointsToField(param);
		}
	}

	/**
	 * Assign this parameter to LPT for all library methods, except for java.lang.Object.<init>, assign it to FPT.
	 * 
	 * @param body
	 */
	private void assignThisParameter() {
		if (hasThis()) {
			Local thisLocal = body.getThisLocal();
			if (isJavaLangObjectInit()) {
				storeFinalizePointsToField(thisLocal);
			} else {
				storeLibraryPointsToField(thisLocal);
			}
		}
	}

	/**
	 * Retrieve all the LHS local variables for the RefLikeType parameters.
	 * 
	 * @param method
	 * @return
	 */
	private List<Local> getRefLikeParameterLocals() {
		List<Local> result = new ArrayList<Local>();
		for (int i = 0; i < body.getMethod().getParameterCount(); i++) {
			if (body.getMethod().getParameterType(i) instanceof RefLikeType) {
				result.add(body.getParameterLocal(i));
			}
		}
		return result;
	}

	/**
	 * Store the given local field to a static soot field.
	 * 
	 * @param field
	 * @param from
	 */
	public void storeStaticField(SootField field, Value from) {
		if (field.isStatic()) {
			body.getUnits().add(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(field.makeRef()), from));
		}
	}

	/**
	 * Store a value to the LPT static field.
	 * 
	 * @param from
	 * @param body
	 * @param numberer
	 */
	public void storeLibraryPointsToField(Value from) {
		storeStaticField(CodeGenerator.v().getAverroesLibraryPointsTo(), from);
	}

	/**
	 * Store a value to the FPT static field.
	 * 
	 * @param from
	 * @param body
	 * @param numberer
	 */
	public void storeFinalizePointsToField(Value from) {
		storeStaticField(CodeGenerator.v().getAverroesFinalizePointsTo(), from);
	}

	/**
	 * Store a value to an instances field.
	 * 
	 * @param base
	 * @param field
	 * @param from
	 * @param body
	 */
	public void storeInstanceField(Value base, SootField field, Value from) {
		if (!field.isStatic()) {
			InstanceFieldRef ref = Jimple.v().newInstanceFieldRef(base, field.makeRef());
			body.getUnits().add(Jimple.v().newAssignStmt(ref, from));
		}
	}

	/**
	 * Check if this method body has this parameter or not.
	 * 
	 * @return
	 */
	public boolean hasThis() {
		return !body.getMethod().isStatic();
	}

	/**
	 * Check if this method body is the one for the Object constructor.
	 * 
	 * @return
	 */
	public boolean isJavaLangObjectInit() {
		return body.getMethod().getSignature().equals("<java.lang.Object: void <init>()>");
	}

	/**
	 * Check if this method body is for a constructor.
	 * 
	 * @return
	 */
	public boolean isConstructor() {
		return body.getMethod().getName().equals(SootMethod.constructorName);
	}

	/**
	 * Check if this method body is for a static initializer.
	 * 
	 * @return
	 */
	public boolean isStaticInitializer() {
		return body.getMethod().getName().equals(SootMethod.staticInitializerName);
	}

	/**
	 * Add statements that initialize all the static fields of the declaring class.
	 * 
	 * @return
	 */
	public void initializeStaticFields() {
		// Initialize all the static fields with compatible objects from the LPT
		for (SootField field : Hierarchy.getStaticFields(body.getMethod().getDeclaringClass())) {
			Value from = getCompatibleValue(field.getType());
			storeStaticField(field, from);
		}
	}

	/**
	 * Add statements that initialize all the instance fields of the declaring class.
	 */
	public void initializeInstanceFields() {
		Local base = body.getThisLocal();

		// Initialize all the instance fields with compatible objects either from the LPT or primitive types
		for (SootField field : Hierarchy.getInstanceFields(body.getMethod().getDeclaringClass())) {
			Value from = getCompatibleValue(field.getType());
			storeInstanceField(base, field, from);
		}
	}

	/**
	 * Find the compatible value to the given Soot type. If it's a primary type, a constant is returned. Otherwise, a
	 * cast to the given type from the LPT is returned.
	 * 
	 * @param type
	 * @return
	 */
	public Value getCompatibleValue(Type type) {
		if (type instanceof PrimType) {
			return getPrimValue((PrimType) type);
		} else {
			return castLptToType(type);
		}
	}

	/**
	 * Cast the LPT set to the given type. This is useful in many cases, e.g., determining the base for method
	 * invocations, as well as the actual arguments used to make those invocations.
	 * 
	 * @param type
	 * @return
	 */
	public Local castLptToType(Type type) {
		if (!lptCastToType.containsKey(type)) {
			Local tmp = insertCastStatement(getLpt(), type);
			lptCastToType.put(type, tmp);
		}
		return lptCastToType.get(type);
	}

	/**
	 * Get the local variable that represents the LPT. It also loads the LPT field if it's not loaded already.
	 * 
	 * @return
	 */
	public Local getLpt() {
		if (!hasLpt()) {
			loadLibraryPointsToField();
		}

		return lpt;
	}

	/**
	 * Get the local variable that represents the FPT. It also loads the FPT field if it's not loaded already.
	 * 
	 * @return
	 */
	public Local getFpt() {
		if (!hasFpt()) {
			loadFinalizePointsToField();
		}

		return fpt;
	}

	/**
	 * Get the local variables that represent the return variables of the method invokes in the underlying Jimple body.
	 * 
	 * @return
	 */
	public Set<Local> getInvokeReturnVariables() {
		return invokeReturnVariables;
	}

	/**
	 * Check if this method has a local variable that holds the LPT.
	 * 
	 * @return
	 */
	public boolean hasLpt() {
		return lpt != null;
	}

	/**
	 * Check if this method has a local variable that holds the FPT.
	 * 
	 * @return
	 */
	public boolean hasFpt() {
		return fpt != null;
	}

	/**
	 * Construct a grammar chunk to load the given static field and assign it to a new temporary local variable.
	 * 
	 * @param field
	 * @return
	 */
	public Local loadStaticField(SootField field) {
		Local tmp = newLocal(CodeGenerator.v().getHierarchy().getJavaLangObject().getType());
		body.getUnits().add(Jimple.v().newAssignStmt(tmp, Jimple.v().newStaticFieldRef(field.makeRef())));
		return tmp;
	}

	/**
	 * Load the global LPT static field.
	 */
	private void loadLibraryPointsToField() {
		lpt = loadStaticField(CodeGenerator.v().getAverroesLibraryPointsTo());
	}

	/**
	 * Load the global FPT static field.
	 */
	private void loadFinalizePointsToField() {
		fpt = loadStaticField(CodeGenerator.v().getAverroesFinalizePointsTo());
	}

	/**
	 * Return a constant value corresponding to the primary type.
	 * 
	 * @param type
	 * @return
	 */
	public static Value getPrimValue(PrimType type) {
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
	 * Create a new local variable of the given type, and adds it to the underlying Jimple body.
	 * 
	 * @param type
	 * @return
	 */
	public Local newLocal(Type type) {
		Local tmp = Jimple.v().newLocal(numberer.next(), type);
		body.getLocals().add(tmp);
		return tmp;
	}

	/**
	 * Insert a statement that casts the given local variable to the given type and assign it to a new temporary local
	 * variable.
	 * 
	 * @param local
	 * @param type
	 * @return temporary variable that holds the result of the cast expression
	 */
	public Local insertCastStatement(Local local, Type type) {
		Local tmp = newLocal(type);
		body.getUnits().add(Jimple.v().newAssignStmt(tmp, Jimple.v().newCastExpr(local, type)));
		return tmp;
	}

	/**
	 * Validate the underlying Jimple body.
	 */
	public void validate() {
		body.validate();
	}

	/**
	 * Insert a virtual invoke statement.
	 * 
	 * @param base
	 * @param method
	 */
	public void insertVirtualInvokeStatement(Local base, SootMethod method) {
		List<Value> args = prepareActualArguments(method);
		body.getUnits().add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(base, method.makeRef(), args)));
	}

	/**
	 * Insert a static invoke statement.
	 * 
	 * @param method
	 */
	public void insertStaticInvokeStatement(SootMethod method) {
		List<Value> args = prepareActualArguments(method);
		body.getUnits().add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(method.makeRef(), args)));
	}

	/**
	 * Insert a special invoke statement.
	 * 
	 * @param base
	 * @param method
	 */
	public void insertSpecialInvokeStatement(Local base, SootMethod method) {
		List<Value> args = prepareActualArguments(method);
		body.getUnits().add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(base, method.makeRef(), args)));
	}

	/**
	 * Insert an invoke statement.
	 * 
	 * @param invokeExpression
	 */
	public void insertInvokeStatement(InvokeExpr invokeExpression) {
		body.getUnits().add(Jimple.v().newInvokeStmt(invokeExpression));
	}

	/**
	 * Insert an assignment statement.
	 * 
	 * @param variable
	 * @param rvalue
	 */
	public void insertAssignmentStatement(Value variable, Value rvalue) {
		body.getUnits().add(Jimple.v().newAssignStmt(variable, rvalue));
	}

	/**
	 * Insert a throw statement.
	 * 
	 * @param throwable
	 */
	public void insertThrowStatement(Value throwable) {
		body.getUnits().add(Jimple.v().newThrowStmt(throwable));
	}

	/**
	 * Insert a NEW statement.
	 * 
	 * @param type
	 * @return
	 */
	public Local insertNewStatement(Type type) {
		Local obj = newLocal(type);
		body.getUnits().add(Jimple.v().newAssignStmt(obj, getNewExpression(type)));
		return obj;
	}

	/**
	 * Construct the appropriate NEW expression depending on the given Soot type. It handles RefType and ArrayType
	 * types.
	 * 
	 * @param type
	 * @return
	 */
	public AnyNewExpr getNewExpression(Type type) {
		if (type instanceof RefType) {
			return Jimple.v().newNewExpr((RefType) type);
		} else if (type instanceof ArrayType) {
			ArrayType arrayType = (ArrayType) type;
			if (arrayType.numDimensions <= 1) {
				return Jimple.v().newNewArrayExpr(arrayType.baseType, IntConstant.v(1));
			} else {
				// Create the list of sizes for the array dimensions
				List<Value> sizes = new ArrayList<Value>();
				for (int i = 0; i < arrayType.numDimensions; i++) {
					sizes.add(IntConstant.v(1));
				}

				return Jimple.v().newNewMultiArrayExpr(arrayType, sizes);
			}
		}

		throw new IllegalArgumentException("Type " + type + " cannot be instantiated.");
	}

	/**
	 * Add Jimple code to create an object of the given SootClass.
	 * 
	 * @param cls
	 */
	public void createObjectOfType(SootClass cls) {
		SootMethod init;

		if (Hierarchy.hasDefaultConstructor(cls)) {
			init = Hierarchy.getDefaultConstructor(cls);
		} else {
			init = Hierarchy.getAnyPublicConstructor(cls);
		}

		createObjectByCallingConstructor(init);
	}

	/**
	 * Add Jimple code to create an object of the given RefType.
	 * 
	 * @param type
	 */
	public void createObjectOfType(RefType type) {
		createObjectOfType(type.getSootClass());
	}

	/**
	 * Add Jimple code to create an object of the given ArrayType.
	 * 
	 * @param type
	 */
	public void createObjectOfType(ArrayType type) {
		Local obj = insertNewStatement(type);
		storeLibraryPointsToField(obj);
	}

	/**
	 * Create an object by calling this specific constructor. This method checks if the constructor exists, and its
	 * declaring class is instantiatable, then it creates a new local with this type, assigns it a NEW expression, calls
	 * the constructor and finally assigns the object to the LPT. It also call the static initializer for the class if
	 * it's available.
	 * 
	 * @param init
	 */
	public void createObjectByCallingConstructor(SootMethod init) {
		if (init != null && init.getName().equals(SootMethod.constructorName)) {
			SootClass cls = init.getDeclaringClass();
			if (cls.isConcrete()) {
				Local obj = insertNewStatement(RefType.v(cls));
				insertSpecialInvokeStatement(obj, init);
				storeLibraryPointsToField(obj);

				// Call clinit if found
				if (cls.declaresMethod(SootMethod.staticInitializerName)) {
					insertStaticInvokeStatement(cls.getMethodByName(SootMethod.staticInitializerName));
				}
			}
		}
	}

	/**
	 * Prepare a list of values to be used as the actual arguments used to call the given soot method. Those arguments
	 * will be pulled from the objects in the LPT or constant values for primary types.
	 * 
	 * @param toCall
	 * @return
	 */
	public List<Value> prepareActualArguments(SootMethod toCall) {
		List<Value> result = new ArrayList<Value>();

		for (Object obj : toCall.getParameterTypes()) {
			Type type = (Type) obj;
			Value val = getCompatibleValue(type);
			result.add(val);
		}

		return result;
	}
}
