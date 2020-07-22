package soot.coffi;

import averroes.soot.Hierarchy;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import averroes.util.BytecodeUtils;
import soot.SootClass;
import soot.SootField;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.jimple.Stmt;

/**
 * A class that holds the values of library methods and fields found in the constant pool of
 * application classes. The class is in this specific package name because it accesses some
 * package-private classes (e.g., {@link CONSTANT_Fieldref_info}.
 *
 * @author karim
 */
public class CoffiAverroesApplicationConstantPool extends AbstractAverroesApplicationConstantPool {

  /**
   * Initialize this constant pool with all the library methods and fields in the constant pool of
   * any application class.
   *
   * @param hierarchy
   */
  public CoffiAverroesApplicationConstantPool(Hierarchy hierarchy) {
    super(hierarchy);

    initialize();
  }

  /**
   * Get the Coffi class corresponding to the given Soot class. From this coffi class, we can get
   * the constant pool and all the related info.
   *
   * @param cls
   * @return
   */
  private static ClassFile getCoffiClass(SootClass cls) {
    SootMethod anyMethod = cls.methodIterator().next();
    CoffiMethodSource methodSource = (CoffiMethodSource) anyMethod.getSource();
    return methodSource.coffiClass;
  }

  /**
   * Get the referenced library methods in an application class.
   *
   * @param applicationClass
   * @return
   */
  @Override
  protected Set<SootMethod> findLibraryMethodsInConstantPool(SootClass applicationClass) {
    Set<SootMethod> result = new HashSet<SootMethod>();

    /*
     * This is only useful if the application class has any methods. Some
     * classes will not have any methods in them, e.g.,
     * org.jfree.data.xml.DatasetTags which is an interface that has some
     * final constants only.
     */
    if (applicationClass.getMethodCount() > 0) {
      ClassFile coffiClass = getCoffiClass(applicationClass);
      cp_info[] constantPool = coffiClass.constant_pool;

      for (cp_info constantPoolEntry : constantPool) {
        if (constantPoolEntry instanceof ICONSTANT_Methodref_info) {
          ICONSTANT_Methodref_info methodInfo = (ICONSTANT_Methodref_info) constantPoolEntry;

          // Get the method declaring class
          CONSTANT_Class_info c = (CONSTANT_Class_info) constantPool[methodInfo.getClassIndex()];
          String className = ((CONSTANT_Utf8_info) (constantPool[c.name_index])).convert();
          className = className.replace('/', '.');
          // TODO why is that?
          if (className.charAt(0) == '[') {
            className = "java.lang.Object";
          }

          // Get the method name, parameter types, and return type
          CONSTANT_NameAndType_info i =
              (CONSTANT_NameAndType_info) constantPool[methodInfo.getNameAndTypeIndex()];
          String methodName = ((CONSTANT_Utf8_info) (constantPool[i.name_index])).convert();
          String methodDescriptor =
              ((CONSTANT_Utf8_info) (constantPool[i.descriptor_index])).convert();
          SootMethod method = BytecodeUtils.makeSootMethod(className, methodName, methodDescriptor);

          // If the resolved method is in the library, add it to the
          // result
          if (hierarchy.isLibraryMethod(method)) {
            result.add(method);
          }
        }
      }
    }

    return result;
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
      ClassFile coffiClass = getCoffiClass(applicationClass);
      cp_info[] constantPool = coffiClass.constant_pool;

      for (cp_info constantPoolEntry : constantPool) {
        if (constantPoolEntry instanceof CONSTANT_String_info) {
          CONSTANT_String_info stringInfo = (CONSTANT_String_info) constantPoolEntry;

          // Get the class name
          CONSTANT_Utf8_info s = (CONSTANT_Utf8_info) constantPool[stringInfo.string_index];
          String className = s.convert(); // .trim();

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
   * Get the referenced library fields in an application class.
   *
   * @param applicationClass
   * @return
   */
  @Override
  protected Set<SootField> findLibraryFieldsInConstantPool(SootClass applicationClass) {
    Set<SootField> result = new HashSet<SootField>();

    /*
     * This is only useful if the application class has any methods. Some
     * classes will not have any methods in them, e.g.,
     * org.jfree.data.xml.DatasetTags which is an interface that has some
     * final constants only.
     */
//    if (applicationClass.getMethodCount() > 0) {
//      ClassFile coffiClass = getCoffiClass(applicationClass);
//      cp_info[] constantPool = coffiClass.constant_pool;
//
//      for (cp_info constantPoolEntry : constantPool) {
//        if (constantPoolEntry instanceof CONSTANT_Fieldref_info) {
//          CONSTANT_Fieldref_info fieldInfo = (CONSTANT_Fieldref_info) constantPoolEntry;
//
//          // Get the field declaring class
//          CONSTANT_Class_info c = (CONSTANT_Class_info) constantPool[fieldInfo.class_index];
//          String className = ((CONSTANT_Utf8_info) (constantPool[c.name_index])).convert();
//          className = className.replace('/', '.');
//          // TODO why is that?
//          if (className.charAt(0) == '[') {
//            className = "java.lang.Object";
//          }
//          SootClass cls = Scene.v().getSootClass(className);
//
//          // Get the field name, and type
//          CONSTANT_NameAndType_info i =
//              (CONSTANT_NameAndType_info) constantPool[fieldInfo.name_and_type_index];
//          String fieldName = ((CONSTANT_Utf8_info) (constantPool[i.name_index])).convert();
//          String fieldDescriptor =
//              ((CONSTANT_Utf8_info) (constantPool[i.descriptor_index])).convert();
//          Type fieldType = Util.v().jimpleTypeOfFieldDescriptor(fieldDescriptor);
//
//          // Get the field ref and resolve it to a Soot field
//          SootFieldRef fieldRef = Scene.v().makeFieldRef(cls, fieldName, fieldType, false);
//          SootField field;
//
//          /*
//           * We have to do this ugly code. Try first and see if the
//           * field is not static. If it is static, then create a new
//           * fieldRef in the catch and resolve it again with isStatic
//           * = true.
//           */
//          try {
//            field = fieldRef.resolve();
//          } catch (ResolutionFailedException e) {
//            fieldRef = Scene.v().makeFieldRef(cls, fieldName, fieldType, true);
//          }
//          field = fieldRef.resolve();
//
//          // If the resolved field is in the library, add it to the
//          // result
//          if (hierarchy.isLibraryField(field)) {
//            result.add(field);
//          }
//        }
//      }
//    }


    // Adapted to use the Soot ASM frontend instead of the now-outdated coffi
    // Find all (non-static) library field references in the class and add the resolved field
    // to the result set.

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
