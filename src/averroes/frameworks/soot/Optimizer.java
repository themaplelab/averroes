package averroes.frameworks.soot;

import averroes.util.io.Printers;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Optimizer {
    public void optimize() {
        for(int i = 0; i < 5; i++) {
            new CHABuilder().run();
            ((Transform) PackManager.v().getPhase("wjtp.si")).apply();
        }

        new CHABuilder().run();
        removeUnreachableMethods();
    }

    /** Return true if it is possible for the application to override this library method. */
    public static boolean isOverridable(SootMethod method) {
        if(method.isFinal()) return false;
        if(method.isPrivate()) return false;

        if(method.isPublic()) return true;
        if(method.isProtected()) return true;

        // TODO: Check whether method is overridden (in library) by any other overridable method.
        // If yes, then return true because if the app overrides the overriding method, then
        // it also overrides this one.

        return false;
    }

    class ReachableMethodsFinder {
        private Set<SootMethod> reachables = new HashSet();
        private void makeReachable(SootMethod method) {
            if(reachables.add(method)) {
                if(method.isConcrete()) {
                    Body body = method.getActiveBody();
                    for(Unit u: body.getUnits()) {
                        Stmt s = (Stmt) u;
                        if(((Stmt) u).containsInvokeExpr()) {
                            InvokeExpr ie = ((Stmt) u).getInvokeExpr();
                            SootMethod target = ie.getMethod();
                            if(target.getDeclaringClass().isApplicationClass()) {
                                logReachable(method, "it is called by "+method);
                                makeReachable(target);
                            }
                        }
                    }
                }
            }
        }
        public Set<SootMethod> apply() {
            for(SootClass cls: Scene.v().getApplicationClasses()) {
                for (SootMethod method : cls.getMethods()) {
                    if (isOverridable(method)) {
                        logReachable(method, "it is overridable");
                        makeReachable(method);
                    }
                }
            }
            return reachables;
        }
        private void logReachable(SootMethod method, String reason) {
            if (!reachables.contains(method)) {
//                System.out.println(method + " is reachable because " + reason);
            }
        }
    }

    public void removeUnreachableMethods() {
        Set<String> signaturesToBeKept = new HashSet();
        signaturesToBeKept.add("void writeObject(java.io.ObjectOutputStream)");
        signaturesToBeKept.add("void readObject(java.io.ObjectInputStream)");
        signaturesToBeKept.add("void readObjectNoData()");

        Set<SootMethod> reachables = new ReachableMethodsFinder().apply();
        for(SootClass cls: Scene.v().getApplicationClasses()) {
//            System.out.println("removing unreachable methods in "+cls);
            for (SootMethod method : cls.getMethods()) {
//                System.out.println(method.getSubSignature());
//                System.out.println(method+" is reachable? "+reachables.contains(method));
                if (!reachables.contains(method) && !signaturesToBeKept.contains(method.getSubSignature())) {
                    Printers.logInliningInfo("removing unreachable method "+method+" from class "+cls, method);
                    cls.removeMethod(method);
                }
            }
        }
    }
}
