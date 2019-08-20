package averroes.frameworks.soot;

import averroes.soot.SootSceneUtil;
import java.util.List;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.VirtualCalls;
import soot.util.queue.ChunkedQueue;
import soot.util.queue.QueueReader;

public class CHABuilder {
  CallGraph cg;

  public void run() {
    cg = new CallGraph();
    SootSceneUtil.getClasses().stream()
        .map(c -> c.getMethods())
        .flatMap(List::stream)
        .filter(SootMethod::isConcrete)
        .forEach(m -> processMethod(m));
    Scene.v().setCallGraph(cg);
  }

  void processMethod(SootMethod m) {
    m.getActiveBody().getUnits().stream()
        .filter(u -> ((Stmt) u).containsInvokeExpr())
        .forEach(u -> processUnit(u, m));
  }

  void processUnit(Unit u, SootMethod container) {
    Stmt s = (Stmt) u;
    InvokeExpr ie = s.getInvokeExpr();
    ChunkedQueue<SootMethod> targets = new ChunkedQueue<>();
    QueueReader<SootMethod> reader = targets.reader();
    if (ie instanceof StaticInvokeExpr) {
      targets.add(ie.getMethod());
    } else if (ie instanceof SpecialInvokeExpr) {
      SootMethod target =
          VirtualCalls.v()
              .resolveSpecial(
                  (SpecialInvokeExpr) ie, ie.getMethodRef().getSubSignature(), container);
      // if the call target resides in a phantom class then "target" will be null;
      // simply do not add the target in that case
      if (target != null) {
        targets.add(target);
      }
    } else {
      InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
      for (SootClass clz : SootSceneUtil.getClasses()) {
        Type tpe = RefType.v(clz);
        Type staticType = iie.getBase().getType();
        if (Scene.v().getFastHierarchy().canStoreType(tpe, staticType)) {
          VirtualCalls.v()
              .resolve(tpe, staticType, iie.getMethodRef().getSubSignature(), container, targets);
        }
      }
    }
    while (reader.hasNext()) {
      Edge edge = new Edge(container, s, reader.next());
      cg.addEdge(edge);
    }
  }
}
