package soot.asm;

import averroes.soot.Hierarchy;
import org.objectweb.asm.MissingJarEntryException;
import org.objectweb.asm.SootClassConstantPool;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.coffi.AbstractAverroesApplicationConstantPool;

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
     * Construct an ASM representation of the class constant pool and return referenced library methods.
     *
     * @param applicationClass
     * @return
     */
    @Override
    protected Set<SootMethod> findLibraryMethodsInConstantPool(SootClass applicationClass) {

        // Only look in classes that actually contain methods
        if (applicationClass.getMethodCount() > 0) {
            try {
                Set<SootMethod> result = (new SootClassConstantPool(applicationClass)).getReferencedMethods();
                return result.stream()
                        .filter(sm -> sm.getDeclaringClass().isLibraryClass())
                        .collect(Collectors.toSet());
            } catch (MissingJarEntryException e) {
                System.err.println("Class not found in application Jar: " + applicationClass.toString());
                e.printStackTrace();
            }
        }
        return new HashSet<>();
    }

    /**
     * Adapted to use the Soot ASM frontend instead of the now-outdated coffi
     * Construct an ASM representation of the class constant pool and return referenced library fields.
     *
     * @param applicationClass
     * @return
     */
    @Override
    protected Set<SootField> findLibraryFieldsInConstantPool(SootClass applicationClass) {

        // Only look in classes that actually contain methods
        if (applicationClass.getMethodCount() > 0) {
            try {
                Set<SootField> result = (new SootClassConstantPool(applicationClass)).getReferencedFields();

                return result.stream()
                        .filter(sf -> sf.getDeclaringClass().isLibraryClass())
                        .collect(Collectors.toSet());
            } catch (MissingJarEntryException e) {
                System.err.println("Class not found in application Jar: " + applicationClass.toString());
                e.printStackTrace();
            }
        }
        return new HashSet<>();
    }
}
