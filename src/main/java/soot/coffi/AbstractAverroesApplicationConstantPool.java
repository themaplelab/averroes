package soot.coffi;

import averroes.soot.Hierarchy;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractAverroesApplicationConstantPool {
    protected Set<SootClass> applicationClasses;
    protected Set<SootMethod> libraryMethods;
    protected Set<SootField> libraryFields;
    protected Hierarchy hierarchy;

    public AbstractAverroesApplicationConstantPool(Hierarchy hierarchy) {
        applicationClasses = new HashSet<SootClass>();
        libraryMethods = new HashSet<SootMethod>();
        libraryFields = new HashSet<SootField>();
        this.hierarchy = hierarchy;
    }

    /**
     * Get the set of library methods that appear in the constant pool of any application class.
     *
     * @return
     */
    public Set<SootMethod> getLibraryMethods() {
      return libraryMethods;
    }

    /**
     * Get the set of library fields that appear in the constant pool of any application class.
     *
     * @return
     */
    public Set<SootField> getLibraryFields() {
      return libraryFields;
    }

    /**
     * Get the set of classes that are referenced by name in the constant pool of any application
     * class.
     *
     * @return
     */
    public Set<SootClass> getApplicationClasses() {
      return applicationClasses;
    }

    /**
     * Check if the given field is a library field referenced by the application.
     *
     * @param field
     * @return
     */
    public boolean isLibraryFieldInApplicationConstantPool(SootField field) {
      return getLibraryFields().contains(field);
    }

    /**
     * Check if the given method is a library method referenced by the application.
     *
     * @param method
     * @return
     */
    public boolean isLibraryMethodInApplicationConstantPool(SootMethod method) {
      return getLibraryMethods().contains(method);
    }

    /** Initialize the application constant pool. */
    protected void initialize() {
      findApplicationClassesReferencedByName();
      findLibraryMethodsInApplicationConstantPool();
      findLibraryFieldsInApplicationConstantPool();
    }

    protected abstract Set<SootMethod> findLibraryMethodsInConstantPool(SootClass applicationClass);

    /**
     * Find all the classes whose name is referenced in the constant pool of application classes.
     *
     * @throws IOException
     */
    private void findApplicationClassesReferencedByName() {
      applicationClasses = new HashSet<SootClass>();

      // If we're processing an android apk, process the global string
      // constant pool
      // if (Options.v().src_prec() == Options.src_prec_apk) {
      // applicationClasses.addAll(findAndroidApplicationClassesReferencedByName());
      // } else {
      // Add the classes whose name appear in the constant pool of application
      // classes
      // TODO
      //		for (SootClass applicationClass : hierarchy.getApplicationClasses()) {
      //			applicationClasses.addAll(findApplicationClassesReferencedByName(applicationClass));
      //		}
      // applicationClasses.forEach(System.out::println);
      // System.out.println("averroes found " + substrings.size() +
      // " possible class name substrings");
      // substrings.forEach(System.out::println);
      // System.out.println("-------------");
      // classes.stream()
      // .filter(c -> {
      // if (substrings.stream().anyMatch(s -> c.getName().startsWith(s))
      // && substrings.stream().anyMatch(s -> c.getName().endsWith(s))) {
      // return true;
      // } else {
      // return false;
      // }
      // }).forEach(System.out::println);
      // applicationClasses.addAll(classes
      // .stream()
      // .filter(c -> {
      // if (substrings.stream().anyMatch(s -> c.getName().startsWith(s))
      // && substrings.stream().anyMatch(s -> c.getName().endsWith(s))) {
      // return true;
      // } else {
      // return false;
      // }
      // }).collect(Collectors.toSet()));
      // }
    }

    /**
     * Find all the library methods referenced from the constant pool of application classes.
     *
     * @return
     */
    private void findLibraryMethodsInApplicationConstantPool() {
      libraryMethods = new HashSet<SootMethod>();

      // If we're processing an android apk, process the global method
      // constant pool
      // if (Options.v().src_prec() == Options.src_prec_apk) {
      // libraryMethods.addAll(findLibraryMethodsInAndroidApplicationConstantPool());
      // } else {
      // Add the library methods that appear in the constant pool of
      // application classes
      for (SootClass applicationClass : hierarchy.getApplicationClasses()) {
        libraryMethods.addAll(findLibraryMethodsInConstantPool(applicationClass));
      }
      // }
    }

    protected abstract Set<SootField> findLibraryFieldsInConstantPool(SootClass applicationClass);

    /**
     * Get all the library fields referenced from the constant pool of application classes.
     *
     * @return
     */
    private void findLibraryFieldsInApplicationConstantPool() {
      libraryFields = new HashSet<SootField>();

      // If we're processing an android apk, process the global field constant
      // pool
      // if (Options.v().src_prec() == Options.src_prec_apk) {
      // libraryFields.addAll(findLibraryFieldsInAndroidApplicationConstantPool());
      // } else {
      // Add the library methods that appear in the constant pool of
      // application classes
      for (SootClass applicationClass : hierarchy.getApplicationClasses()) {
        libraryFields.addAll(findLibraryFieldsInConstantPool(applicationClass));
      }
      // }
    }
}
