package averroes.util;

import averroes.soot.LocalVariableRenamer;
import averroes.soot.LocalVariableSorter;
import averroes.soot.SootSceneUtil;
import soot.Body;
import soot.Modifier;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.FieldRef;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.scalar.NopEliminator;
import soot.toolkits.scalar.UnusedLocalEliminator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Some utility methods for Soot.
 *
 * @author Karim Ali
 */
public class SootUtils {

    /**
     * Perform some code cleanup.
     *
     * <ul>
     *   <li>{@link UnusedLocalEliminator} removes local variables that are defined but never used.
     *   <li>{@link LocalVariableSorter} sorts local variables according to their first use/def in the
     *       method body.
     *   <li>{@link LocalNameStandardizer} standardizes the names of local variables (including "this"
     *       and method parameters. This makes Jimple code comparisons easier.
     *   <li>using {@link NopEliminator} eliminates the NOP statements introduced by guards.
     *   <li>using {@link LocalVariableRenamer} cleans up names of local variables to enable text-base
     *       comparisons of Jimple files.
     * </ul>
     *
     * @param body
     */
    public static void cleanup(Body body) {
        //		UnnecessaryMethodCallEliminator.transform(body);
        //		UnusedLocalEliminator.v().transform(body);
        LocalVariableSorter.transform(body);
        LocalNameStandardizer.v().transform(body);
        NopEliminator.v().transform(body);
        LocalVariableRenamer.transform(body);
    }

    /**
     * Perform cleanup for all classes in the model.
     */
    public static void cleanupClasses() {
//    removeUnusedFields();
        makeFieldsVisible();
    }

    /**
     * Ensure that all fields in the library are protected, if they are not already public or private. This hack helps
     * work around some of the verification that BCEL performs in Pass3b.
     */
    private static void makeFieldsVisible() {
        SootSceneUtil.getClasses().stream()
                .map(c -> c.getFields())
                .flatMap(Collection::stream)
                .filter(f -> !(Modifier.isPublic(f.getModifiers()) || Modifier.isPrivate(f.getModifiers())))
                .forEach(f -> f.setModifiers(f.getModifiers() | Modifier.PROTECTED));
    }

    /**
     * Remove any fields that are not used in the generated model.
     *
     * <p>This will remove, for example, all private fields in the RTA model.
     */
    private static void removeUnusedFields() {
        Set<SootField> usedFields =
                SootSceneUtil.getClasses().stream()
                        .map(c -> c.getMethods())
                        .flatMap(List::stream)
                        .filter(SootMethod::hasActiveBody)
                        .map(m -> m.getActiveBody().getUseAndDefBoxes())
                        .flatMap(List::stream)
                        .map(vb -> vb.getValue())
                        .filter(v -> v instanceof FieldRef)
                        .map(v -> ((FieldRef) v).getField())
                        .collect(Collectors.toSet());

        SootSceneUtil.getClasses().stream()
                .map(c -> new ArrayList<SootField>(c.getFields()))
                .flatMap(List::stream)
                .filter(f -> f.isPrivate() && !usedFields.contains(f))
                .forEach(f -> f.getDeclaringClass().removeField(f));
    }
}
