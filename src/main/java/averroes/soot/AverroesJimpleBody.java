/**
 * ***************************************************************************** Copyright (c) 2015
 * Karim Ali and Ondřej Lhoták. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Karim Ali - initial API and implementation and/or initial documentation
 * *****************************************************************************
 */
package averroes.soot;

import averroes.frameworks.options.FrameworksOptions;
import averroes.options.AverroesOptions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AnyNewExpr;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.DoubleConstant;
import soot.jimple.EqExpr;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LongConstant;
import soot.jimple.NopStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;

/**
 * A representation of the {@link JimpleBody} Averroes generates for all the placeholder library
 * methods.
 *
 * @author Karim Ali
 */
public class AverroesJimpleBody {
  protected static IntConstant ARRAY_LENGTH = IntConstant.v(1);
  private JimpleBody body;
  private Local lpt;
  private Local fpt;
  private Local instance;
  private Local aveGuard;
  private Set<Local> invokeReturnVariables;
  private LocalVariableNumberer numberer;
  private Map<Type, Local> lptCastToType;

  /**
   * Construct a new Jimple body for an Averroes library method.
   *
   * @param method
   */
  public AverroesJimpleBody(SootMethod method) {
    lpt = null;
    fpt = null;
    instance = null;
    aveGuard = null;
    invokeReturnVariables = new HashSet<Local>();
    numberer = new LocalVariableNumberer();
    lptCastToType = new HashMap<Type, Local>();

    createBasicJimpleBody(method);
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
   * Insert the identity statements, assigns actual parameters (if any) and the this parameter (if
   * any) to the LPT.
   */
  private void insertStandardJimpleBodyHeader() {
    body.insertIdentityStmts();

    /*
     * To generate correct bytecode, we need to initialize the object first
     * by calling the superclass constructor before inserting any more
     * statements. That is if this method is for a constructor.
     */
    if (isConstructor()) {
      Local base = body.getThisLocal();

      // Call the default constructor of the direct superclass, except for
      // the constructor of java.lang.Object
      if (!Hierarchy.v().isDeclaredInJavaLangObject(body.getMethod())) {
        insertSpecialInvokeStatement(
            base, Hierarchy.v().getDirectSuperclassDefaultConstructor(body.getMethod()));
      }
    }

    assignActualParametersToLpt();
    assignThisParameter();
  }

  /**
   * Insert the standard footer for a library method: calling the doItAll method then the return
   * statement.
   */
  public void insertStandardJimpleBodyFooter() {
    insertInvocationStmtToDoItAll();
    insertReturnStmt();
  }

  /** Insert an invocation to the doItAll Library method. */
  private void insertInvocationStmtToDoItAll() {
    insertVirtualInvokeStatement(getInstance(), CodeGenerator.v().getAverroesAbstractDoItAll());
  }

  /** Insert the appropriate return statement at the end of the underlying Jimple body. */
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
   * @param originalBody
   */
  private void assignActualParametersToLpt() {
    List<Local> params = getRefLikeParameterLocals();
    for (Local param : params) {
      storeLibraryPointsToField(param);
    }
  }

  /**
   * Assign this parameter to LPT for all library methods, except for java.lang.Object.<init>,
   * assign it to FPT.
   *
   * @param originalBody
   */
  private void assignThisParameter() {
    if (hasThis() && !isAverroesLibraryDoItAll()) {
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
  public void storeStaticField(SootField field, Value from, boolean overrideGuard) {
    if (field.isStatic()) {
      if (overrideGuard) {
        body.getUnits()
            .add(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(field.makeRef()), from));
      } else {
        insertStmt(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(field.makeRef()), from));
      }
    }
  }

  /**
   * Store the given local field to a static soot field.
   *
   * @param field
   * @param from
   */
  public void storeStaticField(SootField field, Value from) {
    storeStaticField(field, from, false);
  }

  /**
   * Store a value to the LPT static field.
   *
   * @param from
   * @param originalBody
   * @param numberer
   */
  public void storeLibraryPointsToField(Value from) {
    // storeStaticField(CodeGenerator.v().getAverroesLibraryPointsTo(),
    // from);
    storeInstanceField(getInstance(), CodeGenerator.v().getAverroesLibraryPointsTo(), from);
  }

  /**
   * Store a value to the FPT static field.
   *
   * @param from
   * @param originalBody
   * @param numberer
   */
  public void storeFinalizePointsToField(Value from) {
    // storeStaticField(CodeGenerator.v().getAverroesFinalizePointsTo(),
    // from);
    storeInstanceField(getInstance(), CodeGenerator.v().getAverroesFinalizePointsTo(), from);
  }

  /**
   * Store a value to an instances field.
   *
   * @param base
   * @param field
   * @param from
   * @param originalBody
   */
  public void storeInstanceField(Value base, SootField field, Value from) {
    if (!field.isStatic()) {
      insertStmt(
          Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(base, field.makeRef()), from));
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
   * Check if this method body is the one for the Names.AVERROES_LIBRARY_DO_IT_ALL_METHOD_SIGNATURE
   *
   * @return
   */
  public boolean isAverroesLibraryDoItAll() {
    return body.getMethod()
        .getSignature()
        .equals(Names.AVERROES_LIBRARY_DO_IT_ALL_METHOD_SIGNATURE);
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
   * Get the underlying Jimple body.
   *
   * @return
   */
  public JimpleBody getJimpleBody() {
    return body;
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

  /** Add statements that initialize all the instance fields of the declaring class. */
  public void initializeInstanceFields() {
    Local base = body.getThisLocal();

    // Initialize all the instance fields with compatible objects either
    // from the LPT or primitive types
    for (SootField field : Hierarchy.getInstanceFields(body.getMethod().getDeclaringClass())) {
      Value from = getCompatibleValue(field.getType());
      storeInstanceField(base, field, from);
    }
  }

  /**
   * Find the compatible value to the given Soot type. If it's a primary type, a constant is
   * returned. Otherwise, a cast to the given type from the LPT is returned.
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
   * Cast the LPT set to the given type. This is useful in many cases, e.g., determining the base
   * for method invocations, as well as the actual arguments used to make those invocations.
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
   * Get the local variable that represents the LPT. It also loads the LPT field if it's not loaded
   * already.
   *
   * @return
   */
  public Local getLpt() {
    if (!hasLpt()) {
      lpt = loadField(getInstance(), CodeGenerator.v().getAverroesLibraryPointsTo(), true);
    }

    return lpt;
  }

  /**
   * Get the local variable that represents the FPT. It also loads the FPT field if it's not loaded
   * already.
   *
   * @return
   */
  public Local getFpt() {
    if (!hasFpt()) {
      fpt = loadField(getInstance(), CodeGenerator.v().getAverroesFinalizePointsTo(), true);
    }

    return fpt;
  }

  /**
   * Get the local variable that represents the Instance. It also loads the Instance field if it's
   * not loaded already.
   *
   * @return
   */
  public Local getInstance() {
    if (!hasInstance()) {
      instance = loadField(CodeGenerator.v().getAverroesInstanceField(), true);
    }

    return instance;
  }

  /**
   * Get the local variables that represent the return variables of the method invokes in the
   * underlying Jimple body.
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
   * Check if this method has a local variable that holds the Instance.
   *
   * @return
   */
  public boolean hasInstance() {
    return instance != null;
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
   * Inserts a statement to the body of the underlying method. The statement will be protected by a
   * guard if {@link FrameworksOptions#isEnableGuards()} returns true, as long as it is not a return
   * statement or a cast statement.
   *
   * @param stmt
   */
  private void insertStmt(Stmt stmt) {
    stmt.apply(
        new AbstractStmtSwitch() {
          @Override
          public void caseReturnStmt(ReturnStmt stmt) {
            body.getUnits().add(stmt);
          }

          @Override
          public void caseAssignStmt(AssignStmt stmt) {
            // cast statements
            if (stmt.getRightOp() instanceof CastExpr) {
              body.getUnits().add(stmt);
            } else if (isNewArrayExpression(stmt.getRightOp())) {
              insertAndGuardStmt(stmt, stmt.getLeftOp());
            } else {
              insertAndGuardStmt(stmt);
            }
          }

          @Override
          public void caseInvokeStmt(InvokeStmt stmt) {
            // don't guard calls to the super constructor => causes uninitialized bytecode
            // verification errors
            if (!AverroesOptions.isEnableGuards()
                || (stmt.getInvokeExpr() instanceof SpecialInvokeExpr && isConstructor())) {
              body.getUnits().add(stmt);
            } else {
              insertAndGuardStmt(stmt);
            }
          }

          @Override
          public void defaultCase(Object obj) {
            if (AverroesOptions.isEnableGuards()) {
              insertAndGuardStmt((Stmt) obj);
            } else {
              body.getUnits().add((Stmt) obj);
            }
          }
        });
  }

  /**
   * Guard a statement by an if-statement whose condition always evaluates to true. This helps
   * inserting multiple {@link ThrowStmt}, for example, in a Jimple method.
   *
   * @param stmt
   * @return
   */
  private void insertAndGuardStmt(Stmt stmt) {
    NopStmt nop = insertGuardCondition();
    body.getUnits().add(stmt);
    body.getUnits().add(nop);
  }

  /**
   * Guard a statement by an if-statement whose condition always evaluates to true. This helps
   * inserting multiple {@link ThrowStmt}, for example, in a Jimple method. This also assigned the
   * return of that stmt to the LPT field.
   *
   * @param stmt
   * @return
   */
  private void insertAndGuardStmt(Stmt stmt, Value from) {
    NopStmt nop = insertGuardCondition();
    body.getUnits().add(stmt);
    body.getUnits()
        .add(
            Jimple.v()
                .newAssignStmt(
                    Jimple.v()
                        .newInstanceFieldRef(
                            getInstance(),
                            CodeGenerator.v().getAverroesLibraryPointsTo().makeRef()),
                    from));
    body.getUnits().add(nop);
  }

  /**
   * Guard an object creation. This is different from guarding regular statements. An object
   * creation requires 2 statements: new statement and an invocation to the constructor if the
   * created object is not an array.
   *
   * @param stmt
   * @return
   */
  private void insertAndGuardStmt(AssignStmt newStmt, InvokeStmt invokeStmt, Value from) {
    NopStmt nop = insertGuardCondition();
    body.getUnits().add(newStmt);
    body.getUnits().add(invokeStmt);
    body.getUnits()
        .add(
            Jimple.v()
                .newAssignStmt(
                    Jimple.v()
                        .newInstanceFieldRef(
                            getInstance(),
                            CodeGenerator.v().getAverroesLibraryPointsTo().makeRef()),
                    from));
    body.getUnits().add(nop);
  }

  /**
   * Guard a sequence of assignment statements.
   *
   * @param stmt
   * @return
   */
  public void insertAndGuardAssignStmts(AssignStmt... stmts) {
    NopStmt nop = insertGuardCondition();
    Arrays.stream(stmts)
        .forEach(
            s -> {
              body.getUnits().add(s);
              body.getUnits()
                  .add(
                      Jimple.v()
                          .newAssignStmt(
                              Jimple.v()
                                  .newInstanceFieldRef(
                                      getInstance(),
                                      CodeGenerator.v().getAverroesLibraryPointsTo().makeRef()),
                              s.getLeftOp()));
            });
    body.getUnits().add(nop);
  }

  /**
   * Inserts a guard condition.
   *
   * @return
   */
  private NopStmt insertGuardCondition() {
    // This condition can produce dead code. That's why we should use
    // the "guard" field as a condition instead.
    // NeExpr cond = Jimple.v().newNeExpr(IntConstant.v(1),
    // IntConstant.v(1));
    EqExpr cond = Jimple.v().newEqExpr(getGuard(), IntConstant.v(0));
    NopStmt nop = Jimple.v().newNopStmt();

    body.getUnits().add(Jimple.v().newIfStmt(cond, nop));

    return nop;
  }

  /**
   * Load the guard field that is used to guard conditionals. See {@link #insertAndGuardStmt(Stmt)
   * for more details}.
   *
   * @return
   */
  private Local getGuard() {
    if (aveGuard == null) {
      aveGuard = loadField(CodeGenerator.v().getAverroesGuardField(), true);
    }
    return aveGuard;
  }

  /**
   * Construct Jimple code that loads a given field and assigns it to a new temporary local
   * variable.
   *
   * @param field
   * @param overrideGuard
   * @return
   */
  private Local loadField(SootField field, boolean overrideGuard) {
    Value fieldRef = getFieldRef(field);
    Local tmp = newLocal(field.getType());
    if (overrideGuard) {
      body.getUnits().add(Jimple.v().newAssignStmt(tmp, fieldRef));
    } else {
      insertStmt(Jimple.v().newAssignStmt(tmp, fieldRef));
    }
    return tmp;
  }

  /**
   * Construct Jimple code that loads a given field and assigns it to a new temporary local
   * variable.
   *
   * @param field
   * @param overrideGuard
   * @return
   */
  private Local loadField(Value base, SootField field, boolean overrideGuard) {
    Local tmp = newLocal(field.getType());
    if (overrideGuard) {
      body.getUnits()
          .add(
              Jimple.v().newAssignStmt(tmp, Jimple.v().newInstanceFieldRef(base, field.makeRef())));
    } else {
      insertStmt(
          Jimple.v().newAssignStmt(tmp, Jimple.v().newInstanceFieldRef(base, field.makeRef())));
    }
    return tmp;
  }

  /**
   * Get the field reference for the given Soot field.
   *
   * @param field
   * @return
   */
  private Value getFieldRef(SootField field) {
    if (field.isStatic()) {
      return Jimple.v().newStaticFieldRef(field.makeRef());
    } else {
      return Jimple.v()
          .newInstanceFieldRef(
              getCompatibleValue(field.getDeclaringClass().getType()), field.makeRef());
    }
  }

  /**
   * Insert a statement that casts the given local variable to the given type and assign it to a new
   * temporary local variable.
   *
   * @param local
   * @param type
   * @return temporary variable that holds the result of the cast expression
   */
  public Local insertCastStatement(Local local, Type type) {
    Local tmp = newLocal(type);
    insertStmt(Jimple.v().newAssignStmt(tmp, Jimple.v().newCastExpr(local, type)));
    return tmp;
  }

  /** Validate the underlying Jimple body. */
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
    insertStmt(
        Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(base, method.makeRef(), args)));
  }

  /**
   * Insert a static invoke statement.
   *
   * @param method
   */
  public void insertStaticInvokeStatement(SootMethod method) {
    List<Value> args = prepareActualArguments(method);
    insertStmt(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(method.makeRef(), args)));
  }

  /**
   * Insert a special invoke statement.
   *
   * @param base
   * @param method
   */
  public void insertSpecialInvokeStatement(Local base, SootMethod method) {
    List<Value> args = prepareActualArguments(method);
    insertStmt(
        Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(base, method.makeRef(), args)));
  }

  /**
   * Insert an invoke statement.
   *
   * @param invokeExpression
   */
  public void insertInvokeStatement(InvokeExpr invokeExpression) {
    insertStmt(Jimple.v().newInvokeStmt(invokeExpression));
  }

  /**
   * Insert an assignment statement.
   *
   * @param variable
   * @param rvalue
   */
  public void insertAssignmentStatement(Value variable, Value rvalue) {
    insertStmt(Jimple.v().newAssignStmt(variable, rvalue));
  }

  /**
   * Insert a throw statement.
   *
   * @param throwable
   */
  public void insertThrowStatement(Value throwable) {
    insertStmt(Jimple.v().newThrowStmt(throwable));
  }

  /**
   * Insert a NEW statement.
   *
   * @param type
   * @return
   */
  public Local insertNewStatement(Type type) {
    Local obj = newLocal(type);
    insertStmt(Jimple.v().newAssignStmt(obj, getNewExpression(type)));
    return obj;
  }

  /**
   * Construct the appropriate NEW expression depending on the given Soot type. It handles RefType
   * and ArrayType types.
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
   * Is this value a new array expression, possibly a multi-dimensional one?
   *
   * @param expr
   * @return
   */
  public boolean isNewArrayExpression(Value expr) {
    return expr instanceof AnyNewExpr && expr.getType() instanceof ArrayType;
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
   * Add Jimple code to create an object of the given ArrayType.
   *
   * @param type
   */
  public void createObjectOfType(ArrayType type) {
    insertNewStatement(type);
  }

  /**
   * Create an object by calling this specific constructor. This method checks if the constructor
   * exists, and its declaring class is instantiatable, then it creates a new local with this type,
   * assigns it a NEW expression, calls the constructor and finally assigns the object to the LPT.
   * It also call the static initializer for the class if it's available.
   *
   * @param init
   */
  public void createObjectByCallingConstructor(SootMethod init) {
    if (init != null && init.getName().equals(SootMethod.constructorName)) {
      createObjectByMethod(init);
    }
  }

  /**
   * Create an object by calling that specific constructor.
   *
   * @param init
   * @return
   */
  protected Local createObjectByMethod(SootMethod init) {
    SootClass cls = init.getDeclaringClass();
    Local obj = insertSpecialInvokeNewStmt(RefType.v(cls), init);

    // Call <clinit> if found
    if (cls.declaresMethod(SootMethod.staticInitializerName)) {
      insertStaticInvokeStatement(cls.getMethodByName(SootMethod.staticInitializerName));
    }

    return obj;
  }

  /**
   * Insert a code snippet that creates a new object and calls its constructor. The order should be:
   * prepare arguments, new instruction, call to constructor.
   *
   * @param type
   * @param toInvoke
   * @return
   */
  public Local insertSpecialInvokeNewStmt(Type type, SootMethod toInvoke) {
    List<Value> args =
        toInvoke.getParameterTypes().stream()
            .map(p -> getCompatibleValue(p))
            .collect(Collectors.toList());

    Local base = newLocal(type);

    if (AverroesOptions.isEnableGuards()
        && !type.equals(CodeGenerator.v().getAverroesLibraryClass().getType())) {
      insertAndGuardStmt(
          Jimple.v().newAssignStmt(base, buildNewExpr(type)),
          Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(base, toInvoke.makeRef(), args)),
          base);
    } else {
      body.getUnits().add(Jimple.v().newAssignStmt(base, buildNewExpr(type)));
      body.getUnits()
          .add(
              Jimple.v()
                  .newInvokeStmt(Jimple.v().newSpecialInvokeExpr(base, toInvoke.makeRef(), args)));
      storeLibraryPointsToField(base);
    }

    return base;
  }

  /**
   * Construct the appropriate NEW expression depending on the given Soot type. It handles RefType
   * and ArrayType types.
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
        return Jimple.v()
            .newNewMultiArrayExpr(
                arrayType, Collections.nCopies(arrayType.numDimensions, ARRAY_LENGTH));
      }
    }

    throw new IllegalArgumentException("Type " + type + " cannot be instantiated.");
  }

  /**
   * Prepare a list of values to be used as the actual arguments used to call the given soot method.
   * Those arguments will be pulled from the objects in the LPT or constant values for primary
   * types.
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
