package org.objectweb.asm;

import averroes.soot.Hierarchy;
import averroes.util.BytecodeUtils;
import soot.Type;
import soot.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A class that holds the values of library methods and fields found in the constant pool of
 * application classes.
 *
 * @author Karim Ali
 */
public class AsmAverroesApplicationConstantPool {
  private Set<SootClass> applicationClasses;
  private Set<SootMethod> libraryMethods;
  private Set<SootField> libraryFields;

  private averroes.soot.Hierarchy hierarchy;

  /**
   * Initialize this constant pool with all the library methods and fields in the constant pool of
   * any application class.
   *
   * @param hierarchy
   */
  public AsmAverroesApplicationConstantPool(Hierarchy hierarchy) {
    applicationClasses = new HashSet<SootClass>();
    libraryMethods = new HashSet<SootMethod>();
    libraryFields = new HashSet<SootField>();

    this.hierarchy = hierarchy;

    initialize();
  }

  /**
   * Get the constant pool entries for the given Soot class. This method uses the ASM symbol table
   * to do so.
   *
   * @param cls
   * @return
   * @throws IOException
   */
  private static List<Symbol> getConstantPool(SootClass cls) throws IOException {
    String clsFile = cls.getName().replace('.', '/') + ".class";
    FoundFile sourceFile = SourceLocator.v().lookupInClassPath(clsFile);

    InputStream classFileInputStream = sourceFile.inputStream();
    ClassReader reader = new ClassReader(classFileInputStream);
    ClassWriter writer = new ClassWriter(reader, 0);
    AverroesSymbolTable symbolTable = new AverroesSymbolTable(writer, reader);

    return Arrays.stream(symbolTable.getEntries()).collect(Collectors.toList());
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
  private void initialize() {
    findApplicationClassesReferencedByName();
    findLibraryMethodsInApplicationConstantPool();
    findLibraryFieldsInApplicationConstantPool();
  }

  /**
   * Get the referenced library methods in an application class.
   *
   * @param applicationClass
   * @return
   */
  private Set<SootMethod> findLibraryMethodsInConstantPool(SootClass applicationClass) {
    Set<SootMethod> result = new HashSet<SootMethod>();

    /*
     * This is only useful if the application class has any methods. Some
     * classes will not have any methods in them, e.g.,
     * org.jfree.data.xml.DatasetTags which is an interface that has some
     * final constants only.
     */
    if (applicationClass.getMethodCount() > 0) {
      try {
        List<Symbol> constantPool = getConstantPool(applicationClass);
        for(Symbol symbol : constantPool) {
          if(symbol.tag == Symbol.CONSTANT_METHODREF_TAG) {
            // TODO still don't remember why I have this condition for class names (it's 2021 now!)
            String className = symbol.owner.startsWith("[") ? "java.lang.Object" : symbol.owner.replace("/", ".");
            String methodName = symbol.name;
            String methodDescriptor = symbol.value;
            SootMethod method = BytecodeUtils.makeSootMethod(className, methodName, methodDescriptor);

            // If the resolved method is in the library, add it to the result
            if (hierarchy.isLibraryMethod(method)) {
              result.add(method);
            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return result;
  }

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
   * Get the classes referenced by name in the constant pool of an application class.
   *
   * @param applicationClass
   */
  private Set<SootClass> findApplicationClassesReferencedByName(SootClass applicationClass) {
    Set<SootClass> result = new HashSet<SootClass>();
    // Set<SootClass> classes = new HashSet<SootClass>();
    // Set<String> substrings = new HashSet<String>();

    /*
     * This is only useful if the application class has any methods. Some
     * classes will not have any methods in them, e.g.,
     * org.jfree.data.xml.DatasetTags which is an interface that has some
     * final constants only.
     */
    if (applicationClass.getMethodCount() > 0) {
      try {
        List<Symbol> constantPool = getConstantPool(applicationClass);
        for(Symbol symbol : constantPool) {
          if(symbol.tag == Symbol.CONSTANT_STRING_TAG) {
            String className = symbol.value; // .trim();

            // if (className.length() > 2) {
            // Set<SootClass> set =
            // hierarchy.matchSubstrOfApplicationClass(className);
            // if (!set.isEmpty()) {
            // classes.addAll(set);
            // substrings.add(className);
            // }
            // }

            if (hierarchy.isApplicationClass(className)) {
              result.add(hierarchy.getClass(className));
            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

      /*
       * This filtering has to take place here (i.e., for each class
       * separately). Otherwise, we will collect a lot of class names that
       * has substrings spanning multiple class files which is both wrong
       * and imprecise.
       */
      // result.addAll(classes
      // .stream()
      // .filter(c -> {
      // if (substrings.stream().anyMatch(s -> c.getName().startsWith(s))
      // && substrings.stream().anyMatch(s -> c.getName().endsWith(s))) {
      // return true;
      // } else {
      // return false;
      // }
      // }).collect(Collectors.toSet()));

    }

    return result;
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

  /**
   * Get the referenced library fields in an application class.
   *
   * @param applicationClass
   * @return
   */
  private Set<SootField> findLibraryFieldsInConstantPool(SootClass applicationClass) {
    Set<SootField> result = new HashSet<SootField>();

    /*
     * This is only useful if the application class has any methods. Some
     * classes will not have any methods in them, e.g.,
     * org.jfree.data.xml.DatasetTags which is an interface that has some
     * final constants only.
     */
    if (applicationClass.getMethodCount() > 0) {
      try {
        List<Symbol> constantPool = getConstantPool(applicationClass);
        for(Symbol symbol : constantPool) {
          if(symbol.tag == Symbol.CONSTANT_FIELDREF_TAG) {
            // TODO still don't remember why I have this condition for class names (it's 2021 now!)
            String className = symbol.owner.startsWith("[") ? "java.lang.Object" : symbol.owner.replace("/", ".");
            String fieldName = symbol.name;
            String fieldDescriptor = symbol.value;

            SootClass cls = Scene.v().getSootClass(className);
            Type fieldType = BytecodeUtils.getFieldType(fieldDescriptor);

            // Get the field ref and resolve it to a Soot field
            SootFieldRef fieldRef = Scene.v().makeFieldRef(cls, fieldName, fieldType, false);
            SootField field;

            /*
             * We have to do this ugly code. Try first and see if the
             * field is not static. If it is static, then create a new
             * fieldRef in the catch and resolve it again with isStatic
             * = true.
             */
            try {
              field = fieldRef.resolve();
            } catch (ResolutionFailedException e) {
              fieldRef = Scene.v().makeFieldRef(cls, fieldName, fieldType, true);
            }
            field = fieldRef.resolve();

            // If the resolved field is in the library, add it to the
            // result
            if (hierarchy.isLibraryField(field)) {
              result.add(field);
            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return result;
  }

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
