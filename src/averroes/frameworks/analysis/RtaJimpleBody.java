package averroes.frameworks.analysis;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.Modifier;
import soot.PrimType;
import soot.RefLikeType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.toolkits.scalar.NopEliminator;
import averroes.frameworks.soot.ClassWriter;
import averroes.frameworks.soot.CodeGenerator;
import averroes.frameworks.soot.Names;

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

	private static SootClass averroesRta;

	private boolean readsArray = false;
	private boolean writesArray = false;

	// Create the singleton RTA class
	static {
		// Create a public class and set its super class to java.lang.Object
		averroesRta = CodeGenerator.createEmptyClass(Names.RTA_CLASS, Modifier.PUBLIC, Scene.v().getObjectType()
				.getSootClass());

		// Add a default constructor to it
		CodeGenerator.createEmptyDefaultConstructor(averroesRta);

		// Add static field "set" to the class
		CodeGenerator.createField(averroesRta, Names.RTA_SET_FIELD_NAME, Scene.v().getObjectType(), Modifier.PUBLIC
				| Modifier.STATIC);

		// Write it to disk
		ClassWriter.writeLibraryClassFile(averroesRta);
	}

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
		System.out.println("==========================");
		System.out.println("BEFORE transformation");
		System.out.println("==========================");
		System.out.println(body);

		// Loop over all the original method statements and transform them
		// appropriately.
		for (Iterator<Unit> iter = method.retrieveActiveBody().getUnits().iterator(); iter.hasNext();) {
			Unit u = iter.next();

			u.apply(new AbstractStmtSwitch() {
				@Override
				public void caseIdentityStmt(IdentityStmt stmt) {
					super.defaultCase(stmt);
				}

				@Override
				public void caseAssignStmt(AssignStmt stmt) {
					if (isNewExpr(stmt)) {
						storeToRtaSet(stmt.getLeftOp(), stmt);
					} else if (!readsArray && isArrayRead(stmt)) {
						readsArray = true;
					} else if (!writesArray && isArrayWrite(stmt)) {
						writesArray = true;
					} else if (isFieldRead(stmt)) {
						defaultCase(stmt);
					} else if (isFieldWrite(stmt)) {
						defaultCase(stmt);
					} else if (isAssignInvoke(stmt)) {
						transformAssignInvoke(stmt);
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
				public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
					super.defaultCase(stmt);
				}

				@Override
				public void caseReturnStmt(ReturnStmt stmt) {
					transformReturnStmt(stmt);
				}

				@Override
				public void defaultCase(Object obj) {
					swapWith((Unit) obj, Jimple.v().newNopStmt());
				}
			});
		}

		// Assign method parameters, including "this, if available, to the
		// appropriate set
		assignMethodParameters();

		// Swap and insert all the units in the buffer
		insertAfter.keySet().forEach(u -> body.getUnits().insertAfter(u, insertAfter.get(u)));
		insertBefore.keySet().forEach(u -> body.getUnits().insertBefore(u, insertBefore.get(u)));
		swap.keySet().forEach(u -> body.getUnits().swapWith(u, swap.get(u)));

		// Eliminate NOP statements
		NopEliminator.v().transform(body);

//		// Validate method Jimple body
//		body.validate();

		// TODO
		System.out.println("==========================");
		System.out.println("AFTER transformation");
		System.out.println("==========================");
		System.out.println(body);
		System.out.println();
		System.out.println();
		
		body.validate();
	}

	/**
	 * Store a value to RTA.set after a given program point.
	 * 
	 * @param value
	 * @param point
	 */
	private void storeToRtaSet(Value value, Unit point) {
		storeStaticField(Scene.v().getField(Names.RTA_SET_FIELD_SIGNATURE), value, point);
	}

	/**
	 * Store a value to RTA.set after the insertPoint().
	 * 
	 * @param value
	 */
	private void storeToRtaSet(Value value) {
		storeToRtaSet(value, insertPoint());
	}

	/**
	 * Load the RTA.set field before the given program point.
	 * 
	 * @param point
	 * @return
	 */
	private Local loadRtaSet(Unit point) {
		return loadStaticField(Scene.v().getField(Names.RTA_SET_FIELD_SIGNATURE), point);
	}

	/**
	 * Construct Jimple code that assigns method parameters, including the
	 * "this" parameter, if available.
	 */
	private void assignMethodParameters() {
		// Loop over all parameters of reference type and create an assignment
		// statement to the appropriate "expression".
		body.getParameterLocals().stream().filter(l -> l.getType() instanceof RefLikeType)
				.forEach(l -> storeToRtaSet(l));

		// and the "this" parameter for java.lang.Object to capture all objects
		// created throughout the program
		if (method.isConstructor() && method.getDeclaringClass().equals(Scene.v().getObjectType().getSootClass())) {
			storeToRtaSet(body.getThisLocal());
		}
	}

	/**
	 * Find the compatible value to the given Soot type. If it's a primary type,
	 * a constant is returned. Otherwise, a cast to the given type from the
	 * RTA.set is returned.
	 * 
	 * @param type
	 * @param point
	 * @return
	 */
	private Value getCompatibleValue(Local local, Type type, Unit point) {
		if (type instanceof PrimType) {
			return getPrimValue((PrimType) type);
		} else {
			return insertCastStatement(local, type, point);
		}
	}

	/**
	 * Transform the given invoke statement.
	 * 
	 * @param stmt
	 */
	protected void transformInvokeStmt(InvokeStmt stmt) {
		InvokeExpr expr = transformInvokeExpr(stmt);
		swapWith(stmt, Jimple.v().newInvokeStmt(expr));
	}

	/**
	 * Transform the given throw statement.
	 * 
	 * @param stmt
	 */
	protected void transformThrowStmt(ThrowStmt stmt) {
		Local rta = loadRtaSet(stmt);
		swapWith(stmt, Jimple.v().newThrowStmt(getCompatibleValue(rta, stmt.getOp().getType(), stmt)));
	}

	/**
	 * Transform the given return statement.
	 * 
	 * @param stmt
	 */
	private void transformReturnStmt(ReturnStmt stmt) {
		Local rta = loadRtaSet(stmt);
		swapWith(stmt, Jimple.v().newReturnStmt(getCompatibleValue(rta, method.getReturnType(), stmt)));
	}

	/**
	 * Transform the given invoke statement.
	 * 
	 * @param stmt
	 */
	private void transformAssignInvoke(AssignStmt stmt) {
		// Transform the invoke expression
		InvokeExpr expr = transformInvokeExpr(stmt);

		// Replace the old statement and assign the return value to RTA.set
		Local tmp = localGenerator.generateLocal(expr.getMethod().getReturnType());
		AssignStmt assign = Jimple.v().newAssignStmt(tmp, expr);
		swapWith(stmt, assign);
		storeToRtaSet(tmp, stmt);
	}

	/**
	 * Transform an invoke expression from the original method to its equivalent
	 * according the definition of the library model.
	 * 
	 * @param originalInvokeExpr
	 * @return
	 */
	private InvokeExpr transformInvokeExpr(Stmt stmt) {
		InvokeExpr originalInvokeExpr = stmt.getInvokeExpr();
		SootMethod callee = originalInvokeExpr.getMethod();
		InvokeExpr invokeExpr = null;
		Local rta = loadRtaSet(stmt);

		// Get the arguments to the call
		List<Value> args = originalInvokeExpr.getArgs().stream().map(a -> getCompatibleValue(rta, a.getType(), stmt))
				.collect(Collectors.toList());

		// Build the invoke expression
		if (originalInvokeExpr instanceof StaticInvokeExpr) {
			invokeExpr = Jimple.v().newStaticInvokeExpr(callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof SpecialInvokeExpr) {
			Local base = (Local) getCompatibleValue(rta, ((SpecialInvokeExpr) originalInvokeExpr).getBase().getType(),
					stmt);
			invokeExpr = Jimple.v().newSpecialInvokeExpr(base, callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof InterfaceInvokeExpr) {
			Local base = (Local) getCompatibleValue(rta,
					((InterfaceInvokeExpr) originalInvokeExpr).getBase().getType(), stmt);
			invokeExpr = Jimple.v().newInterfaceInvokeExpr(base, callee.makeRef(), args);
		} else if (originalInvokeExpr instanceof VirtualInvokeExpr) {
			Local base = (Local) getCompatibleValue(rta, ((VirtualInvokeExpr) originalInvokeExpr).getBase().getType(),
					stmt);
			invokeExpr = Jimple.v().newVirtualInvokeExpr(base, callee.makeRef(), args);
		} else {
			logger.error("Cannot handle invoke expression of type: " + originalInvokeExpr.getClass());
		}

		return invokeExpr;
	}
}
