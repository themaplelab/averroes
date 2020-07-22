package soot.coffi;

import averroes.soot.Hierarchy;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.jimple.Stmt;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A class for accessing library methods and fields that are referenced in application code, using the
 * ASM frontend.
 *
 * @author David Seekatz
 */
public class AsmAverroesApplicationConstantPool extends AbstractAverroesApplicationConstantPool {

    public AsmAverroesApplicationConstantPool(Hierarchy hierarchy) {
        super(hierarchy);

        initialize();
    }

    /**
     * Adapted to use the Soot ASM frontend instead of the now-outdated coffi
     * Find all invoke statements in the class and add their targets to the result set
     * if the target is a library method.
     *
     * While this implementation looks VERY different from the coffi-based implementation, they should both return
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
                                        .map(u -> (Stmt) u)
                                        .flatMap(u -> {
                                            SootMethod tgt = u.getInvokeExpr().getMethodRef().resolve();
                                            if (tgt.isAbstract()) {
                                                // Could have multiple valid targets
                                                Set<SootClass> allPossibleTargets = Scene.v().getClasses().stream()
                                                        .filter(sc -> sc.declaresMethod(tgt.getSubSignature()))
                                                        .collect(Collectors.toSet());
                                                return getPossibleConcreteTargets(tgt, allPossibleTargets).stream();
                                            } else {
                                                return Collections.singleton(tgt).stream();
                                            }
                                        })
                                        .filter(hierarchy::isLibraryMethod)
                                        .collect(Collectors.toSet()));
                            }
                    );
        }
        return result;
    }

    /**
     * We consider the possibility of methods that have a generic return type and are overridden by a method
     * that returns a more restricted type.  In these cases, soot creates two methods, one for each return type,
     * in the class where the overriding method is implemented.  Either could be a valid target.
     *
     * @param tgt
     * @param classesToSearch
     * @return
     */
    private Set<SootMethod> getPossibleConcreteTargets(SootMethod tgt, Set<SootClass> classesToSearch) {
        return classesToSearch.stream()
                .filter(sc -> sc.getMethod(tgt.getSubSignature()).isConcrete())
                .flatMap(sc -> sc.getMethods().stream())
                .filter(sm -> sm.getName().equals(tgt.getName()))
                .collect(Collectors.toSet());
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
