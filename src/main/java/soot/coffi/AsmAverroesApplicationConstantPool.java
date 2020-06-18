package soot.coffi;

import averroes.soot.Hierarchy;
import soot.SootClass;
import soot.SootField;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AsmAverroesApplicationConstantPool extends AbsAverroesApplicationConstantPool {

    public AsmAverroesApplicationConstantPool(Hierarchy hierarchy) {
        super(hierarchy);

        initialize();
    }

    /**
     * Adapted to use the Soot ASM frontend instead of the now-outdated coffi
     * Find all invoke statements in the class and add their targets to the result set
     * if the target is a library method.
     *
     * While this implementation looks VERY different from the coffi-based implementation, they both return
     * the same set of methods.
     *
     * @param applicationClass
     * @return
     */
    @Override
    protected Set<SootMethod> findLibraryMethodsInConstantPool(SootClass applicationClass) {

        Set<SootMethod> result = new HashSet<SootMethod>();

        if (applicationClass.getMethodCount() > 0) {
            applicationClass.getMethods().stream()
                    .filter(SootMethod::isConcrete)
                    .forEach(sootMethod -> {
                                result.addAll(sootMethod.retrieveActiveBody().getUnits().stream()
                                        .filter(u -> u instanceof Stmt && ((Stmt) u).containsInvokeExpr())
                                        // get the target method
                                        .map(u -> ((Stmt) u).getInvokeExpr().getMethodRef().resolve())
                                        .filter(hierarchy::isLibraryMethod)
                                        .collect(Collectors.toSet()));
                            }
                    );
        }
        return result;
    }

    /**
     * Adapted to use the Soot ASM frontend instead of the now-outdated coffi
     * Find all (non-static) library field references in the class and add the resolved field
     * to the result set.
     *
     * @param applicationClass
     * @return
     */
    @Override
    protected Set<SootField> findLibraryFieldsInConstantPool(SootClass applicationClass) {

        Set<SootField> result = new HashSet<SootField>();

        if (applicationClass.getMethodCount() > 0) {
            applicationClass.getMethods().stream()
                    .filter(SootMethod::isConcrete)
                    .forEach(sootMethod -> {
                                result.addAll(sootMethod.retrieveActiveBody().getUnits().stream()
                                        .filter(u -> u instanceof Stmt &&
                                                ((Stmt) u).containsFieldRef())
                                        .map(u -> ((Stmt) u).getFieldRef().getFieldRef())
                                        .filter(sfr -> !sfr.isStatic())
                                        .map(SootFieldRef::resolve)
                                        .collect(Collectors.toSet())
                                );
                            }
                    );
        }

        return result;
    }
}
