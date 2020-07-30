package org.objectweb.asm;

import averroes.options.AverroesOptions;
import averroes.util.BytecodeUtils;
import soot.FoundFile;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * A class representing the constant pool for a Soot class.  This class accesses protected members of org.objectweb.asm
 *
 * This is essentially a utility class for determining fields and methods referenced within a specific constant pool.
 *
 * @author David Seekatz
 */
public class SootClassConstantPool {

    private List<Item> constantPool;

    /**
     * Constructs a representation of the constant pool for a soot class
     *
     * @param applicationClass
     * @throws MissingJarEntryException
     */
    public SootClassConstantPool(SootClass applicationClass) throws MissingJarEntryException {
        try {
            JarFile jarFileContainingClass = getJarFile(applicationClass);
            ZipEntry entry = jarFileContainingClass
                    .getEntry(applicationClass.getName().replace('.', '/') + ".class");

            FoundFile sourceFile = new FoundFile(jarFileContainingClass.getName(), entry.getName());

            InputStream classFileInputStream;
            classFileInputStream = sourceFile.inputStream();
            ClassReader reader = new ClassReader(classFileInputStream);
            ClassWriter writer = new ClassWriter(reader, 0);
            reader.accept(writer, 0);
            Item[] items = writer.items;
            constantPool = Arrays.stream(items)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Could not read constant pool for class: " + applicationClass.toString());
        }
    }

    /**
     * Obtains the Jar file which contains the specific application class.
     *
     * @param applicationClass
     * @return
     * @throws MissingJarEntryException
     */
    private JarFile getJarFile(SootClass applicationClass) throws MissingJarEntryException {
        for (String jar : AverroesOptions.getApplicationJars()) {
            try {
                JarFile jarFile = new JarFile(jar);
                ZipEntry entry = jarFile.getEntry(applicationClass.getName().replace('.', '/') + ".class");
                if (entry != null) {
                    return jarFile;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        throw new MissingJarEntryException("An application class in the scene could not be found in any of the application jar files!");
    }

    /**
     * Gets the methods referenced in this class constant pool
     *
     * @return
     */
    public Set<SootMethod> getReferencedMethods() {
        Set<SootMethod> result = new HashSet<>();
        for (Item i : constantPool) {
            if (i.type == ClassWriter.METH || i.type == ClassWriter.IMETH) {
                result.add(BytecodeUtils.makeSootMethod(
                        // TODO why is that?
                        // Copying over the same CoffiAverroesApplicationConstantPool "to do" note,
                        // originally written circa 2012.  It is still a mystery.
                        i.strVal1.startsWith("[") ? "java.lang.Object" : i.strVal1.replace("/", "."),
                        i.strVal2,
                        i.strVal3
                ));
            }
        }
        return result;

    }

    /**
     * Gets the fields referenced in this class constant pool
     *
     * @return
     */
    public Set<SootField> getReferencedFields() {
        Set<SootField> result = new HashSet<>();
        for (Item i : constantPool) {
            if (i.type == ClassWriter.FIELD) {
                result.add(BytecodeUtils.makeSootField(
                        i.strVal1.startsWith("[") ? "java.lang.Object" : i.strVal1.replace("/", "."),
                        i.strVal2,
                        i.strVal3
                ));
            }
        }
        return result;
    }
}
