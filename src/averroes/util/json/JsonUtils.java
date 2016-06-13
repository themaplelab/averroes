package averroes.util.json;

import java.util.stream.Collectors;

import soot.RefLikeType;
import soot.SootClass;
import soot.Type;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;

/**
 * Utility class for JSON-related operations.
 * 
 * @author Karim Ali
 * 
 */
public class JsonUtils {

	/**
	 * Get the Json representation of the given Soot class.
	 * 
	 * @param cls
	 * @return
	 */
	public static SootClassJson toJson(SootClass cls) {
		SootClassJson sootClassJson = new SootClassJson();

		cls.getMethods().forEach(m -> {
			if (m.isConcrete()) {
				m.getActiveBody().getUnits().forEach(u -> u.apply(new AbstractStmtSwitch() {

					@Override
					public void caseAssignStmt(AssignStmt stmt) {
						// array creations, reads, and writes
						if (stmt.getRightOp() instanceof NewArrayExpr
								|| stmt.getRightOp() instanceof NewMultiArrayExpr) {
							sootClassJson.addObjectCreation(m, stmt.getRightOp().getType());
						} else if (stmt.getRightOp() instanceof FieldRef
								&& stmt.getRightOp().getType() instanceof RefLikeType) {
							sootClassJson.addFieldRead(m, (FieldRef) stmt.getRightOp());
						} else if (stmt.getLeftOp() instanceof FieldRef
								&& stmt.getLeftOp().getType() instanceof RefLikeType) {
							sootClassJson.addFieldWrite(m, (FieldRef) stmt.getLeftOp());
						} else if (stmt.getRightOp() instanceof InvokeExpr) {
							sootClassJson.addInvocation(m, (InvokeExpr) stmt.getRightOp());
						}
					}

					@Override
					public void caseInvokeStmt(InvokeStmt stmt) {
						sootClassJson.addInvocation(m, (InvokeExpr) stmt.getInvokeExpr());
					}
				}));
			}
		});

		return sootClassJson;
	}

	/**
	 * Get the JSON textual representation of a field reference.
	 * 
	 * @param fieldRef
	 * @return
	 */
	public static String toJson(FieldRef fieldRef) {
		StringBuilder str = new StringBuilder();

		// Append the base (instance field) or declaring class (static field)
		if (fieldRef instanceof StaticFieldRef) {
			str.append(fieldRef.getField().getDeclaringClass().getType().toString());
		} else {
			str.append(((InstanceFieldRef) fieldRef).getBase().getType().toString());
		}

		// append field name
		str.append("." + fieldRef.getField().getName());

		return str.toString();
	}

	/**
	 * Get the JSON textual representation of an invoke expression.
	 * 
	 * @param invoke
	 * @return
	 */
	public static String toJson(InvokeExpr invoke) {
		StringBuilder str = new StringBuilder();

		// Append the base (instance invoke) or declaring class (static invoke)
		if (invoke instanceof StaticInvokeExpr) {
			str.append(invoke.getMethod().getDeclaringClass().getType().toString());
		} else {
			str.append(((InstanceInvokeExpr) invoke).getBase().getType().toString());
		}

		// append method name
		str.append("." + invoke.getMethod().getName());

		// append types of arguments
		str.append("(");
		invoke.getArgs().stream().map(a -> a.getType().toString()).collect(Collectors.joining(", "));
		invoke.getArgs().forEach(a -> str.append(a.getType().toString()));
		str.append(")");

		// ignore return type for now

		return str.toString();
	}

	/**
	 * Get the JSON textual representation of a type.
	 * 
	 * @param type
	 * @return
	 */
	public static String toJson(Type type) {
		return type.toString();
	}
}
