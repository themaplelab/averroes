package averroes.frameworks.analysis;

import averroes.frameworks.options.FrameworksOptions;
import averroes.soot.Names;
import averroes.util.SootUtils;
import averroes.util.io.Printers;
import averroes.util.io.Printers.PrinterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.*;
import soot.tagkit.InnerClassTag;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract Jimple body creator that declares some common fields and methods.
 *
 * @author Karim Ali
 */
public abstract class AbstractJimpleBody {
    protected static IntConstant ARRAY_LENGTH = IntConstant.v(1);
    protected static IntConstant ARRAY_INDEX = IntConstant.v(0);
    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected SootMethod method;
    protected JimpleBody originalBody;
    protected JimpleBody body;
    protected LocalGenerator localGenerator;
    protected boolean readsArray = false;
    protected boolean writesArray = false;

    protected Map<Type, Local> casts;

    // Various constructs collected from processing the original method body.
    // Arrays created within the original method body.
    protected LinkedHashSet<Type> arrayCreations = new LinkedHashSet<Type>();

    // Objects, other than arrays, created within the original method body.
    protected LinkedHashSet<SpecialInvokeExpr> objectCreations =
            new LinkedHashSet<SpecialInvokeExpr>();

    // Invoke statements (i.e., return value not assigned to any local
    // variables)
    protected LinkedHashSet<InvokeExpr> invokeStmts = new LinkedHashSet<>();

    // Invoke expression (i.e., return value is assigned to some local variable)
    protected LinkedHashSet<InvokeExpr> invokeExprs = new LinkedHashSet<>();

    // Thrown types
    protected LinkedHashSet<Type> throwables = new LinkedHashSet<>();

    // Checked exceptions (declared in the method signature)
    protected LinkedHashSet<SootClass> checkedExceptions = new LinkedHashSet<>();

    // Field reads
    protected LinkedHashSet<SootFieldRef> fieldReads = new LinkedHashSet<>();

    // Field writes
    protected LinkedHashSet<SootFieldRef> fieldWrites = new LinkedHashSet<>();

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

        casts = new HashMap<>();

        processOriginalMethodBody();
    }

    /**
     * Generate the code for the underlying Soot method (which is assumed to be concrete).
     */
    public void generateCode() {
        Printers.printJimple(PrinterType.ORIGINAL, method);

        // Create Common Class
        // NOTE: we will skip guarding any statements in this class
        // because it contains the guard
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
        SootUtils.cleanup(body);

        // Validate method Jimple body & assign it to the method
        body.validate();
        method.setActiveBody(body);

        Printers.printJimple(PrinterType.GENERATED, method);
    }

    /**
     * Handle field reads and writes.
     */
    protected abstract void handleFields();

    /**
     * Get the set that will be cast to a certain type for various operations. This is RTA.set for the
     * RTA analysis and set_m for XTA.
     *
     * @return
     */
    protected abstract Local setToCast();

    /**
     * Store the given value to the set that is used to over-approximate objects in the library. This
     * could be set_m for XTA or RTA.set for RTA.
     *
     * @param from
     */
    protected abstract void storeToSet(Value from);

    /**
     * Builds a Jimple expresion to store the given value to the set that is used to over-approximate
     * objects in the library. This could be set_m for XTA or RTA.set for RTA. This method is useful
     * for cases when multiple statements have to be guarded using the same guard.
     *
     * @param from
     */
    protected abstract AssignStmt buildStoreToSetExpr(Value from);

    /**
     * Load the guard field that is used to guard conditionals. See {@link #insertAndGuardStmt(Stmt)
     * for more details}.
     *
     * @return
     */
    protected abstract Local getGuard();

    /**
     * Ensure that the common class has been created, along with its fields. For example, this class
     * is rta.RTA for the RTA analysis.
     */
    protected abstract void ensureCommonClassExists();

    /**
     * Scan the original method body for stuff we are looking for so that we loop over the
     * instructions only once.
     */
    private void processOriginalMethodBody() {
        originalBody
                .getUnits()
                .forEach(
                        u ->
                                u.apply(
                                        new AbstractStmtSwitch() {
                                            @Override
                                            public void caseAssignStmt(AssignStmt stmt) {
                                                // array creations, reads, and writes
                                                if (stmt.getRightOp() instanceof NewArrayExpr
                                                        || stmt.getRightOp() instanceof NewMultiArrayExpr) {
                                                    arrayCreations.add(stmt.getRightOp().getType());
                                                } else if (isFieldRead(stmt)
                                                        && stmt.getRightOp().getType() instanceof RefLikeType) {
                                                    fieldReads.add(((FieldRef) stmt.getRightOp()).getFieldRef());
                                                } else if (isFieldWrite(stmt)
                                                        && stmt.getLeftOp().getType() instanceof RefLikeType) {
                                                    fieldWrites.add(((FieldRef) stmt.getLeftOp()).getFieldRef());
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
                                                } else if (!isCallToSuperOrOverloadedConstructor(stmt)) {
                                                    invokeStmts.add(stmt.getInvokeExpr());
                                                }
                                            }

                                            @Override
                                            public void caseThrowStmt(ThrowStmt stmt) {
                                                // Only consider throwables that are not handled locally
                                                if (!TrapManager.getTrappedUnitsOf(originalBody).contains(stmt)) {
                                                    throwables.add(stmt.getOp().getType());
                                                }
                                            }
                                        }));

        processTraps();
    }

    /**
     * Process the traps of a method (i.e., catch blocks) to simulate the creation and assignment of
     * the caught-exception to a local variable that could be used in the catch block.
     */
    private void processTraps() {
        originalBody.getTraps().stream().map(Trap::getException).forEach(checkedExceptions::add);
    }

    /**
     * Insert the identity statements, and assign actual parameters (if any) and the this parameter
     * (if any) to set_m
     */
    protected void insertJimpleBodyHeader() {
        body.insertIdentityStmts();

        /*
         * To generate correct bytecode, we need to initialize the object first
         * by calling the direct superclass default constructor before inserting
         * any more statements. That is if this method is for a constructor and
         * its declaring class has a superclass.
         */
        if (method.isConstructor()) {
            Local base = body.getThisLocal();
            Stmt firstNonIdentity = originalBody.getFirstNonIdentityStmt();

            // don't guard calls to the super constructor => causes
            // uninitialized bytecode verification errors
            if (isCallToSuperOrOverloadedConstructor(firstNonIdentity)) {
                InvokeStmt stmt = (InvokeStmt) firstNonIdentity;
                insertSpecialInvokeStmt(base, stmt.getInvokeExpr(), true, false);
            } else if (method.getDeclaringClass().hasSuperclass()) {
                insertSpecialInvokeStmt(
                        base,
                        method.getDeclaringClass().getSuperclass().getMethod(Names.DEFAULT_CONSTRUCTOR_SUBSIG),
                        true);
            }

            /*
             * If this is the constructor of an inner class, insert an
             * assignment statement for the first parameter to the special
             * this$0 field.
             */
            if (isDeclaringClassNonStaticInnerClass() && !body.getParameterLocals().isEmpty()) {
                InstanceFieldRef fieldRef =
                        Jimple.v()
                                .newInstanceFieldRef(
                                        base, method.getDeclaringClass().getFieldByName("this$0").makeRef());
                insertStmt(Jimple.v().newAssignStmt(fieldRef, body.getParameterLocal(0)), true);
            }
        }

        assignMethodParameters();

        /*
         * If this is a method in an inner class, assign the implicit "this"
         * parameter of the outer class to the underlying set.
         */
        if (!method.isConstructor() && isDeclaringClassNonStaticInnerClass() && !method.isStatic()) {
            Local base = body.getThisLocal();
            InstanceFieldRef fieldRef =
                    Jimple.v()
                            .newInstanceFieldRef(
                                    base, method.getDeclaringClass().getFieldByName("this$0").makeRef());
            Local elem = localGenerator.generateLocal(fieldRef.getType());
            insertAssignStmts(Jimple.v().newAssignStmt(elem, fieldRef), buildStoreToSetExpr(elem));
        }
    }

    /**
     * Insert the standard footer for a library method.
     */
    protected void insertJimpleBodyFooter() {
        /*
         * Insert the return statement, only if there are no exceptions to throw
         * or they are guarded. Otherwise, it's dead code and the Jimple
         * validator will choke on it!
         */
        if (throwables.isEmpty() || isThrowStatementGuardRequired()) {
            insertReturnStmt();
        }
    }

    /**
     * Create all the objects that the library could possible instantiate. For reference types, this
     * includes inserting new statements, invoking constructors, and static initializers if found. For
     * arrays, we just have the appropriate NEW instruction. For checked exceptions, we create the
     * object, call the constructor, and, if available, call the static initializer.
     */
    protected void createObjects() {
        objectCreations.forEach(e -> createObjectByInvokeExpr(e));
        arrayCreations.forEach(t -> insertNewStmt(t));
        checkedExceptions.forEach(
                cls -> createObjectByMethod(cls.getMethod(Names.DEFAULT_CONSTRUCTOR_SUBSIG)));
    }

    /**
     * Create an object by calling that specific constructor.
     *
     * @param invoke
     * @return
     */
    protected Local createObjectByInvokeExpr(SpecialInvokeExpr invoke) {
        SootClass cls = invoke.getMethod().getDeclaringClass();
        Local obj = insertSpecialInvokeNewStmt(RefType.v(cls), invoke);

        // Call <clinit> if found
        if (cls.declaresMethod(SootMethod.staticInitializerName)) {
            insertStaticInvokeStmt(cls.getMethodByName(SootMethod.staticInitializerName));
        }

        return obj;
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
            insertStaticInvokeStmt(cls.getMethodByName(SootMethod.staticInitializerName));
        }

        return obj;
    }

    /**
     * Call the methods that are called in the original method body. Averroes preserves the fact that
     * the return value of some method calls (invokeExprs) is stored locally, while it's not for other
     * calls (invokeStmts).
     */
    protected void callMethods() {
        invokeStmts.forEach(this::buildAndInsertInvokeStmt);

        invokeExprs.forEach(
                e -> {
                    InvokeExpr expr = buildInvokeExpr(e);

                    // ignore invokedynamic
                    if (!(expr instanceof DynamicInvokeExpr)) {
                        // Only store the return value if it's a reference, otherwise just call the method.
                        if (expr.getMethod().getReturnType() instanceof RefLikeType) {
                            storeMethodCallReturn(expr);
                        } else {
                            insertInvokeStmt(expr);
                        }
                    }
                });
    }

    /**
     * Handle array reads and writes.
     */
    protected void handleArrays() {
        if (readsArray || writesArray) {
            Local cast =
                    (Local) getCompatibleValue(ArrayType.v(setToCast().getType(), ARRAY_LENGTH.value));
            ArrayRef arrayRef = Jimple.v().newArrayRef(cast, ARRAY_INDEX);

            if (readsArray) {
                Local elem = localGenerator.generateLocal(Scene.v().getObjectType());

                insertAssignStmts(Jimple.v().newAssignStmt(elem, arrayRef), buildStoreToSetExpr(elem));
            } else {
                insertStmt(Jimple.v().newAssignStmt(arrayRef, setToCast()));
            }
        }
    }

    /**
     * Handle throwing exceptions and try-catch blocks. The throw statements should always be guarded,
     * regardless of how many there are. This way, the return statement, that is inserted later, is
     * never unreachable.
     */
    protected void handleExceptions() {
        throwables.forEach(x -> insertThrowStmt(x, isThrowStatementGuardRequired()));
    }

    /**
     * Should throw statements be guarded by a boolean condition? They should if there is more than
     * one throw statement, or if there's only one throw statement and the method has a non-void
     * return type.
     *
     * @return
     */
    protected boolean isThrowStatementGuardRequired() {
        return throwables.size() > 1
                || (throwables.size() == 1 && !(method.getReturnType() instanceof VoidType));
    }

    /**
     * Insert a statement that casts the given local to the given type and assign it to a new
     * temporary local variable. If {@code local} is of the same type as {@code type}, then return
     * that local instead of using a temporary variable.
     *
     * @param local
     * @param type
     * @return temporary variable that holds the result of the cast expression
     */
    protected Local insertCastStmt(Local local, Type type) {
        if (!casts.containsKey(type)) {
            if (local.getType().equals(type)) {
                casts.put(type, local);
            } else {
                Local tmp = localGenerator.generateLocal(type);
                insertStmt(Jimple.v().newAssignStmt(tmp, Jimple.v().newCastExpr(local, type)));
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
        insertStmt(Jimple.v().newAssignStmt(obj, buildNewExpr(type)));
        return obj;
    }

    /**
     * Builds a new invoke statement based on info in the given original invoke expression and inserts
     * it in the underlying Jimple body.
     *
     * @param originalInvokeExpr
     */
    protected void buildAndInsertInvokeStmt(InvokeExpr originalInvokeExpr) {
        InvokeExpr invokeExpr = buildInvokeExpr(originalInvokeExpr);
        // ignore invokedynamic
        if (!(invokeExpr instanceof DynamicInvokeExpr)) insertInvokeStmt(invokeExpr);
    }

    /**
     * Insert an new invoke statement.
     *
     * @param invokeExpr
     */
    protected void insertInvokeStmt(InvokeExpr invokeExpr) {
        insertStmt(Jimple.v().newInvokeStmt(invokeExpr));
    }

    /**
     * Insert a special invoke statement.
     *
     * @param base
     * @param toInvoke
     */
    protected void insertSpecialInvokeStmt(Local base, InvokeExpr toInvoke) {
        insertSpecialInvokeStmt(base, toInvoke, false);
    }

    /**
     * Insert a special invoke statement.
     *
     * @param base
     * @param toInvoke
     */
    protected void insertSpecialInvokeStmt(Local base, InvokeExpr toInvoke, boolean overrideGuard) {
        insertSpecialInvokeStmt(base, toInvoke, overrideGuard, true);
    }

    /**
     * Insert a special invoke statement.
     *
     * @param base
     * @param toInvoke
     */
    protected void insertSpecialInvokeStmt(
            Local base, InvokeExpr toInvoke, boolean overrideGuard, boolean generateCasts) {
        List<Value> args =
                toInvoke.getArgs().stream()
                        .map(
                                a -> {
                                    if (generateCasts || a.getType() instanceof PrimType) {
                                        return getCompatibleValue(a.getType());
                                    } else if (a.getType().equals(NullType.v())) {
                                        return NullConstant.v();
                                    } else if (a instanceof StringConstant) {
                                        return StringConstant.v("");
                                    } else {
                                        return body.getParameterLocals().stream()
                                                .filter(l -> a.getType().equals(l.getType()))
                                                .findFirst()
                                                .get();
                                    }
                                })
                        .collect(Collectors.toList());
        insertStmt(
                Jimple.v()
                        .newInvokeStmt(
                                Jimple.v().newSpecialInvokeExpr(base, toInvoke.getMethod().makeRef(), args)),
                overrideGuard);
    }

    /**
     * Insert a special invoke statement.
     *
     * @param base
     * @param toInvoke
     */
    protected void insertSpecialInvokeStmt(Local base, SootMethod toInvoke) {
        insertSpecialInvokeStmt(base, toInvoke, false);
    }

    /**
     * Insert a special invoke statement.
     *
     * @param base
     * @param toInvoke
     */
    protected void insertSpecialInvokeStmt(Local base, SootMethod toInvoke, boolean overrideGuard) {
        insertSpecialInvokeStmt(base, toInvoke, overrideGuard, true);
    }

    /**
     * Insert a special invoke statement.
     *
     * @param base
     * @param toInvoke
     */
    protected void insertSpecialInvokeStmt(
            Local base, SootMethod toInvoke, boolean overrideGuard, boolean generateCasts) {
        List<Value> args =
                toInvoke.getParameterTypes().stream()
                        .map(
                                p -> {
                                    if (generateCasts) {
                                        return getCompatibleValue(p);
                                    } else if (p.equals(NullType.v())) {
                                        return NullConstant.v();
                                    } else {
                                        return body.getParameterLocals().stream()
                                                .filter(l -> p.equals(l.getType()))
                                                .findFirst()
                                                .get();
                                    }
                                })
                        .collect(Collectors.toList());
        insertStmt(
                Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(base, toInvoke.makeRef(), args)),
                overrideGuard);
    }

    /**
     * Insert a code snippet that creates a new object and calls its constructor. The order should be:
     * prepare arguments, new instruction, call to constructor.
     *
     * @param type
     * @param toInvoke
     * @return
     */
    protected Local insertSpecialInvokeNewStmt(Type type, SootMethod toInvoke) {
        List<Value> args =
                toInvoke.getParameterTypes().stream()
                        .map(p -> getCompatibleValue(p))
                        .collect(Collectors.toList());

        Local base = localGenerator.generateLocal(type);

        if (FrameworksOptions.isEnableGuards()) {
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
            storeToSet(base);
        }

        return base;
    }

    /**
     * Insert a code snippet that creates a new object and calls its constructor. The order should be:
     * prepare arguments, new instruction, call to constructor.
     *
     * @param type
     * @param originalInvokeExpression
     * @return
     */
    protected Local insertSpecialInvokeNewStmt(
            Type type, SpecialInvokeExpr originalInvokeExpression) {
        List<Value> args = prepareArguments(originalInvokeExpression);

        Local base = localGenerator.generateLocal(type);

        if (FrameworksOptions.isEnableGuards()) {
            insertAndGuardStmt(
                    Jimple.v().newAssignStmt(base, buildNewExpr(type)),
                    Jimple.v()
                            .newInvokeStmt(
                                    Jimple.v()
                                            .newSpecialInvokeExpr(
                                                    base, originalInvokeExpression.getMethod().makeRef(), args)),
                    base);
        } else {
            body.getUnits().add(Jimple.v().newAssignStmt(base, buildNewExpr(type)));
            body.getUnits()
                    .add(
                            Jimple.v()
                                    .newInvokeStmt(
                                            Jimple.v()
                                                    .newSpecialInvokeExpr(
                                                            base, originalInvokeExpression.getMethod().makeRef(), args)));
            storeToSet(base);
        }

        return base;
    }

    /**
     * Insert a static invoke statement.
     *
     * @param toInvoke
     */
    protected void insertStaticInvokeStmt(SootMethod toInvoke) {
        List<Value> args =
                toInvoke.getParameterTypes().stream()
                        .map(p -> getCompatibleValue(p))
                        .collect(Collectors.toList());
        insertStmt(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(toInvoke.makeRef(), args)));
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
            insertStmt(Jimple.v().newThrowStmt(tmp));
        }
    }

    /**
     * Insert the appropriate return statement.
     */
    protected void insertReturnStmt() {
        if (method.getReturnType() instanceof VoidType) {
            insertStmt(Jimple.v().newReturnVoidStmt());
        } else {
            Value ret = getCompatibleValue(method.getReturnType());
            insertStmt(Jimple.v().newReturnStmt(ret));
        }
    }

    /**
     * Guard a statement by an if-statement whose condition always evaluates to true. This helps
     * inserting multiple {@link ThrowStmt}, for example, in a Jimple method.
     *
     * @param stmt
     * @return
     */
    protected void insertAndGuardStmt(Stmt stmt) {
        NopStmt nop = insertGuardCondition();
        body.getUnits().add(stmt);
        body.getUnits().add(nop);
    }

    /**
     * Guard an object creation. This is different from guarding regular statements. An object
     * creation requires 2 statements: new statement and an invocation to the constructor if the
     * created object is not an array.
     *
     * @param newStmt
     * @param invokeStmt
     * @param from
     * @return
     */
    protected void insertAndGuardStmt(AssignStmt newStmt, InvokeStmt invokeStmt, Value from) {
        NopStmt nop = insertGuardCondition();
        body.getUnits().add(newStmt);
        body.getUnits().add(invokeStmt);
        body.getUnits().add(buildStoreToSetExpr(from));
        body.getUnits().add(nop);
    }

    /**
     * Guard a statement by an if-statement whose condition always evaluates to true. This helps
     * inserting multiple {@link ThrowStmt}, for example, in a Jimple method. This also assigns the
     * return of that stmt to the LPT field.
     *
     * @param stmt
     * @return
     */
    protected void insertAndGuardStmt(Stmt stmt, Value from) {
        NopStmt nop = insertGuardCondition();
        body.getUnits().add(stmt);
        body.getUnits().add(buildStoreToSetExpr(from));
        body.getUnits().add(nop);
    }

    /**
     * Guard a sequence of assignment statements.
     *
     * @param stmts
     * @return
     */
    protected void insertAndGuardAssignStmts(AssignStmt... stmts) {
        NopStmt nop = insertGuardCondition();
        Arrays.stream(stmts).forEach(body.getUnits()::add);
        body.getUnits().add(nop);
    }

    /**
     * Inserts a guard condition.
     *
     * @return
     */
    protected NopStmt insertGuardCondition() {
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
     * Inserts a sequence of assignment statement to the body of the underlying method. The sequence
     * will be protected by a guard if {@link FrameworksOptions#isEnableGuards()} returns true, as
     * long as it is not a return statement or a cast statement.
     *
     * @param stmts
     */
    protected void insertAssignStmts(AssignStmt... stmts) {
        if (FrameworksOptions.isEnableGuards()) {
            insertAndGuardAssignStmts(stmts);
        } else {
            Arrays.stream(stmts).forEach(body.getUnits()::add);
        }
    }

    /**
     * Inserts a statement to the body of the underlying method. The statement will be protected by a
     * guard if {@link FrameworksOptions#isEnableGuards()} returns true, as long as it is not a return
     * statement or a cast statement.
     *
     * @param stmt
     */
    protected void insertStmt(Stmt stmt) {
        insertStmt(stmt, false);
    }

    /**
     * Inserts a statement to the body of the underlying method. The statement will be protected by a
     * guard if {@link FrameworksOptions#isEnableGuards()} returns true, as long as it is not a return
     * statement or a cast statement.
     *
     * @param stmt
     * @param overrideGuard
     */
    protected void insertStmt(Stmt stmt, boolean overrideGuard) {
        if (overrideGuard || !FrameworksOptions.isEnableGuards()) {
            body.getUnits().add(stmt);
        } else {
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
                        public void defaultCase(Object obj) {
                            insertAndGuardStmt((Stmt) obj);
                        }
                    });
        }
    }

    /**
     * Is this value a new array expression, possibly a multi-dimensional one?
     *
     * @param expr
     * @return
     */
    protected boolean isNewArrayExpression(Value expr) {
        return expr instanceof AnyNewExpr && expr.getType() instanceof ArrayType;
    }

    /**
     * Store the return value of a method call to {@link #storeToSet(Value)}
     *
     * @param expr
     */
    protected void storeMethodCallReturn(InvokeExpr expr) {
        Local ret = localGenerator.generateLocal(expr.getMethod().getReturnType());
        insertAssignStmts(Jimple.v().newAssignStmt(ret, expr), buildStoreToSetExpr(ret));
    }

    /**
     * Construct Jimple code that loads a given field and assigns it to a new temporary local
     * variable.
     *
     * @param fieldRef
     * @return
     */
    protected Local loadField(SootFieldRef fieldRef) {
        return loadField(fieldRef, false);
    }

    /**
     * Construct Jimple code that loads a given field and assigns it to a new temporary local
     * variable.
     *
     * @param field
     * @param overrideGuard
     * @return
     */
    protected Local loadField(SootFieldRef field, boolean overrideGuard) {
        Value fieldRef = getFieldRef(field);
        Local tmp = localGenerator.generateLocal(field.type());
        if (overrideGuard) {
            body.getUnits().add(Jimple.v().newAssignStmt(tmp, fieldRef));
        } else {
            insertStmt(Jimple.v().newAssignStmt(tmp, fieldRef));
        }
        return tmp;
    }

    /**
     * Store the given value to a Soot field.
     *
     * @param fieldRef
     * @param from
     */
    protected void storeField(SootFieldRef fieldRef, Value from) {
        insertStmt(Jimple.v().newAssignStmt(getFieldRef(fieldRef), from));
    }

    /**
     * Construct Jimple code that stores the given value to a Soot field.
     *
     * @param fieldRef
     * @param from
     * @return
     */
    protected AssignStmt buildStoreFieldExpr(SootFieldRef fieldRef, Value from) {
        return Jimple.v().newAssignStmt(getFieldRef(fieldRef), from);
    }

    /**
     * Get the field reference for the given Soot field.
     *
     * @param fieldRef
     * @return
     */
    protected Value getFieldRef(SootFieldRef fieldRef) {
        if (fieldRef.isStatic()) {
            return Jimple.v().newStaticFieldRef(fieldRef);
        } else {
            return Jimple.v()
                    .newInstanceFieldRef(
                            getCompatibleValue(fieldRef.declaringClass().getType()), fieldRef);
        }
    }

    /**
     * Construct Jimple code that assigns method parameters, including the "this" parameter, if
     * available, to {@link #storeToSet(Value)}.
     */
    protected void assignMethodParameters() {
        // Assign the "this" parameter, if available
        if (!method.isStatic()) {
            storeToSet(body.getThisLocal());
        }

        // Loop over all parameters of reference type and create an assignment
        // statement to the appropriate "expression".
        body.getParameterLocals().stream()
                .filter(
                        l -> !isConstructorOuterClassParameterLocal(l) && l.getType() instanceof RefLikeType)
                .forEach(l -> storeToSet(l));
    }

    /**
     * Is this local variable the parameter of the outer class in the constructor of the inner class?
     *
     * @param local
     * @return
     */
    protected boolean isConstructorOuterClassParameterLocal(Local local) {
        return method.isConstructor()
                && isDeclaringClassNonStaticInnerClass()
                && local.equals(body.getParameterLocal(0));
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
     * Is this a relevant object creation? (i.e., a call to a constructor that is not the constructor
     * of the direct super class)
     *
     * @param stmt
     * @return
     */
    protected boolean isRelevantObjectCreation(InvokeStmt stmt) {
        return stmt.getInvokeExpr() instanceof SpecialInvokeExpr
                && stmt.getInvokeExpr().getMethod().isConstructor()
                && !isCallToSuperOrOverloadedConstructor(stmt);
        // && !originalBody.getFirstNonIdentityStmt().equals(stmt);
    }

    /**
     * Is this a call to the super constructor? (e.g., super(e)).
     *
     * @param stmt
     * @return
     */
    protected boolean isCallToSuperConstructor(InvokeStmt stmt) {
        return method.getDeclaringClass().hasSuperclass()
                && (stmt.getInvokeExpr() instanceof SpecialInvokeExpr
                && method.isConstructor()
                && stmt.getInvokeExpr().getMethod().isConstructor()
                && method
                .getDeclaringClass()
                .getSuperclass()
                .getMethods()
                .contains(stmt.getInvokeExpr().getMethod()));
    }

    /**
     * Is this a call to another overloaded constructor? (e.g., this(e)).
     *
     * @param stmt
     * @return
     */
    protected boolean isCallToOverloadedConstructor(InvokeStmt stmt) {
        return stmt.getInvokeExpr() instanceof SpecialInvokeExpr
                && method.isConstructor()
                && stmt.getInvokeExpr().getMethod().isConstructor()
                && method.getDeclaringClass().getMethods().contains(stmt.getInvokeExpr().getMethod());
    }

    /**
     * Is this stmt a call to a super or overloaded constructor?
     *
     * @param stmt
     * @return
     */
    protected boolean isCallToSuperOrOverloadedConstructor(Stmt stmt) {
        if (stmt instanceof InvokeStmt) {
            InvokeStmt invoke = (InvokeStmt) stmt;
            return isCallToSuperConstructor(invoke) || isCallToOverloadedConstructor(invoke);
        }

        return false;
    }

    /**
     * Is this the constructor of an inner class. It's unfortunate we can't use the method {@link
     * SootClass#isInnerClass()} for that. We have to resort to the class tags, hoping that they are
     * parsed properly.
     *
     * @return
     */
    protected boolean isDeclaringClassInnerClass() {
        return method.getDeclaringClass().getTags().stream().anyMatch(t -> t instanceof InnerClassTag);
    }

    /**
     * Checks if the declaring class of this method is a static inner class.
     *
     * @return
     */
    protected boolean isDeclaringClassStaticInnerClass() {
        return isDeclaringClassInnerClass()
                && !method.getDeclaringClass().declaresFieldByName("this$0");
    }

    /**
     * Checks if the declaring class of this method is a non-static inner class.
     *
     * @return
     */
    protected boolean isDeclaringClassNonStaticInnerClass() {
        return isDeclaringClassInnerClass() && method.getDeclaringClass().declaresFieldByName("this$0");
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
                .map(AssignStmt.class::cast)
                .filter(this::isArrayRead)
                .findFirst()
                .isPresent();
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
                .map(AssignStmt.class::cast)
                .filter(this::isArrayWrite)
                .findFirst()
                .isPresent();
    }

    /**
     * Is this assignment a field read?
     *
     * @param assign
     * @return
     */
    protected boolean isFieldRead(AssignStmt assign) {
        return assign.getRightOp() instanceof FieldRef;
        //        && !(isDeclaringClassInnerClass()
        //            && ((FieldRef) assign.getRightOp()).getField().getName().equals("this$0"));
    }

    /**
     * Is this assignment a field write?
     *
     * @param assign
     * @return
     */
    protected boolean isFieldWrite(AssignStmt assign) {
        return assign.getLeftOp() instanceof FieldRef
                && !(isDeclaringClassInnerClass()
                && ((FieldRef) assign.getLeftOp()).getField().getName().equals("this$0"));
    }

    /**
     * Find the compatible value to the given Soot type. If it's a primary type, a constant is
     * returned. Otherwise, the methods returns a cast of {@link setToCast()} to the given type. In
     * the case that the cast is to the same type as the class declaring this method, then return the
     * {@code this} variable. This is used in calls to the constructors of anonymous classes, as well
     * as accessing fields in the same class.
     *
     * @param type
     * @return
     */
    protected Value getCompatibleValue(Type type) {
        if (type instanceof PrimType) {
            return getPrimValue((PrimType) type);
        }
        // else if (type.equals(method.getDeclaringClass().getType())) {
        // return body.getThisLocal();
        // }
        else {
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
     * Get the proper base for the given instance invoke. If the original invoke expression uses
     * {@code this}, then use the {@code this} variable of the newly-constructed method. Otherwise,
     * return the result of {@link #getCompatibleValue(Type)}.
     *
     * @param invoke
     * @return
     */
    protected Value getInvokeReceiver(InstanceInvokeExpr invoke) {
        if (!method.isStatic() && invoke.getBase().equals(originalBody.getThisLocal())) {
            return body.getThisLocal();
        } else {
            return getCompatibleValue(invoke.getBase().getType());
        }
    }

    // protected Value getFieldAccessReceiver(SootField field) {
    // if()
    // }

    /**
     * Return a list of all the object created through calls to constructors. This does not include
     * array creations. Those are collected and handled somewhere else.
     *
     * @return
     */
    protected List<SpecialInvokeExpr> getObjectCreations() {
        return originalBody.getUnits().stream()
                .filter(u -> u instanceof InvokeStmt)
                .map(InvokeStmt.class::cast)
                .filter(s -> s.getInvokeExpr() instanceof SpecialInvokeExpr)
                .map(s -> (SpecialInvokeExpr) s.getInvokeExpr())
                .collect(Collectors.toList());
    }

    /**
     * Return a list of all the created in the underlying method.
     *
     * @return
     */
    protected List<Type> getArrayCreations() {
        return originalBody.getUnits().stream()
                .filter(u -> u instanceof AssignStmt)
                .map(AssignStmt.class::cast)
                .filter(
                        s ->
                                s.getRightOp() instanceof NewArrayExpr
                                        || s.getRightOp() instanceof NewMultiArrayExpr)
                .map(s -> s.getRightOp().getType())
                .collect(Collectors.toList());
    }

    /**
     * Get all the LHS local variables for the RefLikeType parameters of the newly created Jimple
     * body.
     *
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
     * Build the grammar of an invoke expression based on the given original invoke expression. This
     * method does not insert the grammar chunk into the method body. It only inserts any code needed
     * to prepare the arguments to the call (i.e., casts of RTA.set).
     *
     * @param originalInvokeExpr
     * @return
     */
    protected InvokeExpr buildInvokeExpr(InvokeExpr originalInvokeExpr) {
        SootMethodRef callee = originalInvokeExpr.getMethodRef();
        InvokeExpr invokeExpr = null;

        // Get the arguments to the call
        List<Value> args = prepareArguments(originalInvokeExpr);

        // Build the invoke expression
        if (originalInvokeExpr instanceof StaticInvokeExpr) {
            invokeExpr = Jimple.v().newStaticInvokeExpr(callee, args);
        } else if (originalInvokeExpr instanceof SpecialInvokeExpr) {
            Local base = (Local) getInvokeReceiver((SpecialInvokeExpr) originalInvokeExpr);
            invokeExpr = Jimple.v().newSpecialInvokeExpr(base, callee, args);
        } else if (originalInvokeExpr instanceof InterfaceInvokeExpr) {
            Local base = (Local) getInvokeReceiver((InterfaceInvokeExpr) originalInvokeExpr);
            invokeExpr = Jimple.v().newInterfaceInvokeExpr(base, callee, args);
        } else if (originalInvokeExpr instanceof VirtualInvokeExpr) {
            Local base = (Local) getInvokeReceiver((VirtualInvokeExpr) originalInvokeExpr);
            invokeExpr = Jimple.v().newVirtualInvokeExpr(base, callee, args);
        } else if (originalInvokeExpr instanceof DynamicInvokeExpr) {
            SootMethodRef bootstrap = ((DynamicInvokeExpr) originalInvokeExpr).getBootstrapMethodRef();
            List<Value> bootstrapArgs = ((DynamicInvokeExpr) originalInvokeExpr).getBootstrapArgs();
            int tag = ((DynamicInvokeExpr) originalInvokeExpr).getHandleTag();
            invokeExpr = Jimple.v().newDynamicInvokeExpr(bootstrap, bootstrapArgs, callee, tag, args);
        } else {
            logger.error("Cannot handle invoke expression of type: " + originalInvokeExpr.getClass());
        }

        return invokeExpr;
    }

    /**
     * Prepare the arguments to a call based on the information from the original invoke expression.
     *
     * @param originalInvokeExpr
     * @return
     */
    protected List<Value> prepareArguments(InvokeExpr originalInvokeExpr) {
        List<Value> result = new ArrayList<Value>();

        // Do not use getCompatibleValue(a.getType). This causes errors as it
        // will happily cast an object to the NullType.
        for (int i = 0; i < originalInvokeExpr.getArgCount(); i++) {
            if (!method.isStatic() && originalInvokeExpr.getArg(i).equals(originalBody.getThisLocal())) {
                result.add(body.getThisLocal());
            } else if (originalInvokeExpr.getArg(i).getType().equals(NullType.v())) {
                //				result.add(getCompatibleValue(originalInvokeExpr.getMethod().getParameterType(i)));
                result.add(NullConstant.v());
            } else {
                result.add(getCompatibleValue(originalInvokeExpr.getArg(i).getType()));
            }
        }

        return result;
    }
}
