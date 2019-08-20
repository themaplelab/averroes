package averroes.soot;

import java.util.List;
import java.util.stream.Collectors;
import soot.Body;
import soot.Unit;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;

public class UnnecessaryMethodCallEliminator {

  /**
   * Eliminate unnecessary method calls (e.g., calls to access$0 methods). This makes text-based
   * diff of Jimple files much easier.
   *
   * @param body
   */
  public static void transform(Body body) {
    // List<Unit> toRemove = new ArrayList<Unit>();

    List<Unit> toRemove =
        body.getUnits().stream()
            .filter(
                u ->
                    (u instanceof DefinitionStmt
                            && ((DefinitionStmt) u).getRightOp() instanceof InvokeExpr
                            && isRemovable((InvokeExpr) ((DefinitionStmt) u).getRightOp()))
                        || (u instanceof InvokeStmt
                            && isRemovable(((InvokeStmt) u).getInvokeExpr())))
            .collect(Collectors.toList());

    // for (Unit unit : body.getUnits()) {
    // if (unit instanceof DefinitionStmt && ((DefinitionStmt)
    // unit).getRightOp() instanceof InvokeExpr
    // && isRemovable((InvokeExpr) ((DefinitionStmt) unit).getRightOp())) {
    // toRemove.add(unit);
    // } else if (unit instanceof InvokeStmt && isRemovable(((InvokeStmt)
    // unit).getInvokeExpr())) {
    // toRemove.add(unit);
    // }
    // }

    body.getUnits().removeAll(toRemove);
  }

  private static boolean isRemovable(InvokeExpr invoke) {
    return invoke.getMethod().isStatic() && invoke.getMethod().getName().startsWith("access$");
  }
}
