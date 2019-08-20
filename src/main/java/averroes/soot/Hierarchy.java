/**
 * ***************************************************************************** Copyright (c) 2015
 * Karim Ali and Ondřej Lhoták. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Karim Ali - initial API and implementation and/or initial documentation
 * *****************************************************************************
 */
package averroes.soot;

import averroes.options.AverroesOptions;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import soot.ArrayType;
import soot.Modifier;
import soot.RefLikeType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.VoidType;
import soot.coffi.AverroesApplicationConstantPool;
import soot.tagkit.Tag;

/**
 * A representation of the class hierarchy Averroes uses for the input program.
 *
 * @author Karim Ali
 */
public class Hierarchy {

  private static Hierarchy instance = new Hierarchy();

  private AverroesApplicationConstantPool applicationConstantPool;

  private SootBasicClassesDatabase basicClassesDatabase;

  private HashMap<SootClass, LinkedHashSet<SootClass>> classToSuperclasses;
  private HashMap<SootClass, SootClass> classToDirectSuperclass;
  private HashMap<SootClass, LinkedHashSet<SootClass>> classToSubclasses;
  private HashMap<SootClass, LinkedHashSet<SootClass>> classToConcreteSubclasses;
  private HashMap<SootClass, LinkedHashSet<SootClass>> classToLibraryConcreteSubclasses;
  private HashMap<SootClass, LinkedHashSet<SootClass>> classToDirectSubclasses;

  private HashMap<SootClass, LinkedHashSet<SootClass>> classToSuperinterfaces;
  private HashMap<SootClass, LinkedHashSet<SootClass>> classToDirectSuperinterfaces;
  private HashMap<SootClass, LinkedHashSet<SootClass>> interfaceToDirectImplementers;
  private HashMap<SootClass, LinkedHashSet<SootClass>> interfaceToImplementers;
  private HashMap<SootClass, LinkedHashSet<SootClass>> interfaceToConcreteImplementers;
  private HashMap<SootClass, LinkedHashSet<SootClass>> interfaceToLibraryConcreteImplementers;

  private HashMap<SootMethod, SootMethod> methodToTopmostSuperMethod;
  private HashMap<SootMethod, SootMethod> methodToTopmostSuperclassesSuperMethod;
  private HashMap<SootMethod, SootMethod> methodToTopmostSuperinterfacesSuperMethod;

  private HashMap<SootMethod, Set<SootMethod>> methodToSuperMethods;
  private HashMap<SootMethod, LinkedHashSet<SootMethod>> methodToSuperclassesSuperMethods;
  private HashMap<SootMethod, Set<SootMethod>> methodToSuperinterfacesSuperMethods;

  private HashMap<SootClass, Set<SootMethod>> classToLibrarySuperMethods;
  private Set<SootMethod> librarySuperMethodsOfApplicationMethods;

  private Set<SootClass> applicationClassesReferencedByName;
  private Set<SootMethod> libraryMethodsReferencedInApplication;
  private Set<SootField> libraryFieldsReferencedInApplication;

  private List<SootClass> classes;
  private HashMap<String, SootClass> nameToClass;
  private HashMap<String, SootClass> nameToApplicationClass;
  private HashMap<String, SootClass> nameToLibraryClass;

  private SortedSet<SootClass> applicationClasses;
  private SortedSet<SootClass> libraryClasses;

  private Set<SootClass> abstractLibraryClasses;
  private Set<SootClass> concreteLibraryClasses;
  private Set<SootClass> libraryInterfaces;

  private Set<SootClass> abstractLibraryClassesNotImplementedInLibrary;
  private Set<SootClass> libraryInterfacesNotImplementedInLibrary;

  private Set<ArrayType> libraryArrayTypeReturns;
  private Set<ArrayType> libraryArrayTypeParameters;

  private int applicationMethodCount;
  private int applicationFieldCount;

  private int libraryMethodCount;
  private int libraryFieldCount;

  private int removedLibraryMethodCount;
  private int removedLibraryFieldCount;

  /**
   * Initialize the hierarchy with all the classes resolved at the level {@link
   * ResolvingLevel.#SIGNATURES} from the Soot scene. Averroes is only interested in those classes
   * so it doesn't make sense to include any more classes.
   */
  private Hierarchy() {
    classToSuperclasses = new HashMap<SootClass, LinkedHashSet<SootClass>>();
    classToDirectSuperclass = new HashMap<SootClass, SootClass>();
    classToSubclasses = new HashMap<SootClass, LinkedHashSet<SootClass>>();
    classToConcreteSubclasses = new HashMap<SootClass, LinkedHashSet<SootClass>>();
    classToLibraryConcreteSubclasses = new HashMap<SootClass, LinkedHashSet<SootClass>>();
    classToDirectSubclasses = new HashMap<SootClass, LinkedHashSet<SootClass>>();

    classToSuperinterfaces = new HashMap<SootClass, LinkedHashSet<SootClass>>();
    classToDirectSuperinterfaces = new HashMap<SootClass, LinkedHashSet<SootClass>>();
    interfaceToDirectImplementers = new HashMap<SootClass, LinkedHashSet<SootClass>>();
    interfaceToImplementers = new HashMap<SootClass, LinkedHashSet<SootClass>>();
    interfaceToConcreteImplementers = new HashMap<SootClass, LinkedHashSet<SootClass>>();
    interfaceToLibraryConcreteImplementers = new HashMap<SootClass, LinkedHashSet<SootClass>>();

    methodToTopmostSuperMethod = new HashMap<SootMethod, SootMethod>();
    methodToTopmostSuperclassesSuperMethod = new HashMap<SootMethod, SootMethod>();
    methodToTopmostSuperinterfacesSuperMethod = new HashMap<SootMethod, SootMethod>();

    methodToSuperMethods = new HashMap<SootMethod, Set<SootMethod>>();
    methodToSuperclassesSuperMethods = new HashMap<SootMethod, LinkedHashSet<SootMethod>>();
    methodToSuperinterfacesSuperMethods = new HashMap<SootMethod, Set<SootMethod>>();

    classToLibrarySuperMethods = new HashMap<SootClass, Set<SootMethod>>();
    librarySuperMethodsOfApplicationMethods = new HashSet<SootMethod>();

    applicationClassesReferencedByName = new HashSet<SootClass>();
    libraryMethodsReferencedInApplication = new HashSet<SootMethod>();
    libraryFieldsReferencedInApplication = new HashSet<SootField>();

    classes = Scene.v().getClasses(ResolvingLevel.SIGNATURES.value());
    nameToClass = new HashMap<String, SootClass>();
    nameToApplicationClass = new HashMap<String, SootClass>();
    nameToLibraryClass = new HashMap<String, SootClass>();

    applicationClasses = new TreeSet<SootClass>(new SootClassHierarchyComparer(this));
    libraryClasses = new TreeSet<SootClass>(new SootClassHierarchyComparer(this));

    abstractLibraryClasses = new HashSet<SootClass>();
    concreteLibraryClasses = new HashSet<SootClass>();
    libraryInterfaces = new HashSet<SootClass>();

    abstractLibraryClassesNotImplementedInLibrary = new HashSet<SootClass>();
    libraryInterfacesNotImplementedInLibrary = new HashSet<SootClass>();

    libraryArrayTypeReturns = new HashSet<ArrayType>();
    libraryArrayTypeParameters = new HashSet<ArrayType>();

    applicationMethodCount = 0;
    applicationFieldCount = 0;

    libraryMethodCount = 0;
    libraryFieldCount = 0;

    removedLibraryMethodCount = 0;
    removedLibraryFieldCount = 0;

    initialize();

    basicClassesDatabase = new SootBasicClassesDatabase(this);
  }

  /**
   * Get the Cleanup singleton.
   *
   * @return
   */
  public static Hierarchy v() {
    return instance;
  }

  /** Reset the hierarchy. */
  public static void reset() {
    instance = new Hierarchy();
  }

  /**
   * Check if the given class has a default constructor.
   *
   * @param cls
   * @return
   */
  public static boolean hasDefaultConstructor(SootClass cls) {
    return cls.declaresMethod(Names.DEFAULT_CONSTRUCTOR_SUBSIG);
  }

  /**
   * Get the default constructor of the given class.
   *
   * @param cls
   * @return
   */
  public static SootMethod getDefaultConstructor(SootClass cls) {
    return cls.getMethod(Names.DEFAULT_CONSTRUCTOR_SUBSIG);
  }

  /**
   * Find the first public constructor you can find for the given class.
   *
   * @param cls
   * @return
   */
  public static SootMethod getAnyPublicConstructor(SootClass cls) {
    for (SootMethod method : cls.getMethods()) {
      if (method.isPublic() && method.isConstructor()) {
        return method;
      }
    }

    return null;
  }

  /**
   * Get a new default constructor method.
   *
   * @return
   */
  public static SootMethod getNewDefaultConstructor() {
    return new SootMethod(
        SootMethod.constructorName, Collections.emptyList(), VoidType.v(), Modifier.PUBLIC);
  }

  /**
   * Check if the method is native, then change it to be not native. This way Averroes will create a
   * method body for it. This is important because if such method has a RefLikeType return variable,
   * we need its objects to be propagated to the LPT.
   *
   * @param libraryMethod
   */
  public static void makeNotNative(SootMethod libraryMethod) {
    if (libraryMethod.isNative()) {
      // This is a stupid workaround because Soot doesn't allow changing
      // the modifiers of a library method
      libraryMethod.getDeclaringClass().setApplicationClass();
      libraryMethod.setModifiers(libraryMethod.getModifiers() & ~Modifier.NATIVE);
      libraryMethod.getDeclaringClass().setLibraryClass();
    }
  }

  /**
   * Check if the method is not public, make it public. Useful for Averroes to make public
   * constructors for library classes.
   *
   * @param libraryMethod
   */
  public static void makePublic(SootMethod libraryMethod) {
    if (!libraryMethod.isPublic()) {
      // This is a stupid workaround because Soot doesn't allow changing
      // the modifiers of a library method
      libraryMethod.getDeclaringClass().setApplicationClass();
      libraryMethod.setModifiers(Modifier.PUBLIC);
      libraryMethod.getDeclaringClass().setLibraryClass();
    }
  }

  /**
   * Get all the static fields in a given class.
   *
   * @param cls
   * @return
   */
  public static Set<SootField> getStaticFields(SootClass cls) {
    Set<SootField> result = new HashSet<SootField>();

    for (SootField field : cls.getFields()) {
      if (field.isStatic()) {
        result.add(field);
      }
    }

    return result;
  }

  /**
   * Get all the instance fields in a given class.
   *
   * @param cls
   * @return
   */
  public static Set<SootField> getInstanceFields(SootClass cls) {
    Set<SootField> result = new HashSet<SootField>();

    for (SootField field : cls.getFields()) {
      if (!field.isStatic()) {
        result.add(field);
      }
    }

    return result;
  }

  /**
   * Get subsignature from signature.
   *
   * @param sig
   * @return
   */
  public static String signatureToSubsignature(String sig) {
    if (isValidSignature(sig)) {
      return sig.substring(sig.indexOf(":") + 2, sig.length() - 1);
    }

    throw new RuntimeException("Invalid signature: " + sig);
  }

  /**
   * Check if the given signature is a valid Soot signature.
   *
   * @param sig
   * @return
   */
  public static boolean isValidSignature(String sig) {
    return sig.charAt(0) == '<' && sig.charAt(sig.length() - 1) == '>' && sig.indexOf(":") >= 0;
  }

  /**
   * Check if a Soot class is an abstract class.
   *
   * @param cls
   * @return
   */
  public static boolean isAbstractClass(SootClass cls) {
    return cls.isAbstract() && !cls.isInterface();
  }

  /**
   * Check if the given method is a static initializer.
   *
   * @param method
   * @return
   */
  public static boolean isStaticInitializer(SootMethod method) {
    return method.isStatic() && method.getName().equals(SootMethod.staticInitializerName);
  }

  /**
   * Get the base type of Soot type. If the Soot type is an array type, it will return its base
   * type. Otherwise, it will return the type itself.
   *
   * @param type
   * @return
   */
  public static Type getBaseType(Type type) {
    return type instanceof ArrayType ? ((ArrayType) type).baseType : type;
  }

  /**
   * Get the base type of Soot type name.
   *
   * @param type
   * @return
   */
  public static String getBaseType(String type) {
    return type.replaceAll("\\[\\]", "");
  }

  /**
   * Get the database of basic classes required by Averroes to run.
   *
   * @return
   */
  public SootBasicClassesDatabase getBasicClassesDatabase() {
    return basicClassesDatabase;
  }

  /**
   * Get the array types that appear as return types of library methods.
   *
   * @return
   */
  public Set<ArrayType> getLibraryArrayTypeReturns() {
    return libraryArrayTypeReturns;
  }

  /**
   * Get the array types that appear as parameter types in library supermethods (i.e., overridden by
   * the application).
   *
   * @return
   */
  public Set<ArrayType> getLibraryArrayTypeParameters() {
    return libraryArrayTypeParameters;
  }

  /**
   * Get the number of application methods.
   *
   * @return
   */
  public int getApplicationMethodCount() {
    return applicationMethodCount;
  }

  /**
   * Get the number of application fields.
   *
   * @return
   */
  public int getApplicationFieldCount() {
    return applicationFieldCount;
  }

  /**
   * Get the number of library methods.
   *
   * @return
   */
  public int getLibraryMethodCount() {
    return libraryMethodCount;
  }

  /**
   * Get the number of library fields.
   *
   * @return
   */
  public int getLibraryFieldCount() {
    return libraryFieldCount;
  }

  /**
   * Get the number of referenced library methods.
   *
   * @return
   */
  public int getReferencedLibraryMethodCount() {
    return libraryMethodsReferencedInApplication.size();
  }

  /**
   * Get the number of referenced library fields.
   *
   * @return
   */
  public int getReferencedLibraryFieldCount() {
    return libraryFieldsReferencedInApplication.size();
  }

  /**
   * Get the number of removed library methods.
   *
   * @return
   */
  public int getRemovedLibraryMethodCount() {
    return removedLibraryMethodCount;
  }

  /**
   * Get the number of removed library fields.
   *
   * @return
   */
  public int getRemovedLibraryFieldCount() {
    return removedLibraryFieldCount;
  }

  /**
   * Get the number of methods.
   *
   * @return
   */
  public int getMethodCount() {
    return applicationMethodCount + libraryMethodCount;
  }

  /**
   * Get the number of fields.
   *
   * @return
   */
  public int getFieldCount() {
    return applicationFieldCount + libraryFieldCount;
  }

  /**
   * Clean up the hierarchy from methods and fields not referenced by the application. In addition,
   * add default constructors to classes that don't have one. They will be used by Averroes to
   * create objects in the doItAll method.
   */
  public void cleanupLibraryClasses() {
    for (SootClass libraryClass : libraryClasses) {
      addDefaultConstructorToLibraryClass(libraryClass);
      cleanupLibraryClassTags(libraryClass);
      cleanupMethodsInLibraryClass(libraryClass);
      cleanupFieldsInLibraryClass(libraryClass);
    }
  }

  /**
   * Get a list of all the classes that make up this hierarchy.
   *
   * @return
   */
  public List<SootClass> getClasses() {
    return classes;
  }

  /**
   * Get all the application classes.
   *
   * @return
   */
  public SortedSet<SootClass> getApplicationClasses() {
    return applicationClasses;
  }

  /**
   * Get all the library classes.
   *
   * @return
   */
  public SortedSet<SootClass> getLibraryClasses() {
    return libraryClasses;
  }

  /**
   * Check if the given method is a basic method for Averroes.
   *
   * @param method
   * @return
   */
  public boolean isBasicLibraryMethod(SootMethod method) {
    String sig = method.getSignature();
    return method.isConstructor()
        || isStaticInitializer(method)
        || sig.equals(Names.FOR_NAME_SIG)
        || sig.equals(Names.NEW_INSTANCE_SIG)
        || sig.equals(Names.FINALIZE_SIG);
  }

  /**
   * Get the default constructor of the superclass.
   *
   * @param method
   * @return
   */
  public SootMethod getDirectSuperclassDefaultConstructor(SootMethod method) {
    return getDirectSuperclassOf(method.getDeclaringClass())
        .getMethod(Names.DEFAULT_CONSTRUCTOR_SUBSIG);
  }

  /**
   * Add a default constructor to the given library class.
   *
   * @param libraryClass
   */
  public void addDefaultConstructorToLibraryClass(SootClass libraryClass) {
    if (!libraryClass.isInterface()) {
      if (hasDefaultConstructor(libraryClass)) {
        makePublic(libraryClass.getMethod(Names.DEFAULT_CONSTRUCTOR_SUBSIG));
      } else {
        addMethodToLibraryClass(libraryClass, getNewDefaultConstructor());
      }
    }
  }

  /**
   * Add a method to a library class and update the library method count.
   *
   * @param libraryClass
   * @param method
   */
  private void addMethodToLibraryClass(SootClass libraryClass, SootMethod method) {
    libraryClass.addMethod(method);

    // Update the library method count
    libraryMethodCount++;
  }

  /**
   * Get class name from signature.
   *
   * @param sig
   * @return
   */
  public String signatureToClass(String sig) {
    if (isValidSignature(sig)) {
      return sig.substring(1, sig.indexOf(":"));
    }

    throw new RuntimeException("Invalid signature: " + sig);
  }

  /**
   * Get the Soot array type corresponding to the given type name.
   *
   * @param type
   * @return
   */
  public ArrayType getArrayType(String type) {
    String className = getBaseType(type);
    int numberOfDimensions = (type.length() - className.length()) / 2;
    return ArrayType.v(nameToClass.get(className).getType(), numberOfDimensions);
  }

  /**
   * Get the java.lang.Object Soot class.
   *
   * @return
   */
  public SootClass getJavaLangObject() {
    return nameToLibraryClass.get(Names.JAVA_LANG_OBJECT);
  }

  /**
   * Get the java.lang.Class Soot class.
   *
   * @return
   */
  public SootClass getJavaLangClass() {
    return nameToLibraryClass.get(Names.JAVA_LANG_CLASS);
  }

  /**
   * Get the java.lang.Throwable Soot class.
   *
   * @return
   */
  public SootClass getJavaLangThrowable() {
    return nameToLibraryClass.get(Names.JAVA_LANG_THROWABLE);
  }

  /**
   * Get the String[] type.
   *
   * @return
   */
  public Type getStringArrayType() {
    return ArrayType.v(nameToClass.get(Names.JAVA_LANG_STRING).getType(), 1);
  }

  /**
   * Get the parameters of the standard main method.
   *
   * @return
   */
  public List<Type> getMainParams() {
    return Arrays.asList(getStringArrayType());
  }

  /**
   * Check if the given method is declared in java.lang.Object.
   *
   * @param method
   * @return
   */
  public boolean isDeclaredInJavaLangObject(SootMethod method) {
    return method.getDeclaringClass().getName().equals(Names.JAVA_LANG_OBJECT);
  }

  /**
   * Get a class given its name. If not found, the method returns null.
   *
   * @param className
   * @return
   */
  public SootClass getClass(String className) {
    return nameToClass.get(className);
  }

  /**
   * Get a method given its signature.
   *
   * @param methodSignature
   * @return
   */
  public SootMethod getMethod(String methodSignature) {
    return getClass(signatureToClass(methodSignature))
        .getMethod(signatureToSubsignature(methodSignature));
  }

  //	/**
  //	 * Check if the given string is a valid substring of an application class
  //	 * name.
  //	 *
  //	 * @param str
  //	 * @return list of application classes it matches
  //	 */
  //	public Set<SootClass> matchSubstrOfApplicationClass(String str) {
  //		return nameToApplicationClass.keySet().stream().filter(k -> k.contains(str))
  //				.map(k -> nameToApplicationClass.get(k)).collect(Collectors.toSet());
  //	}

  /**
   * Check if the given class is an application class.
   *
   * @param className
   * @return
   */
  public boolean isApplicationClass(String className) {
    return nameToApplicationClass.containsKey(className);
  }

  /**
   * Check if the given class is an application class.
   *
   * @param cls
   * @return
   */
  public boolean isApplicationClass(SootClass cls) {
    return isApplicationClass(cls.getName());
  }

  /**
   * Check if the given type is for a application class.
   *
   * @param type
   * @return
   */
  public boolean isApplicationClass(Type type) {
    return isApplicationClass(getBaseType(type).toString());
  }

  /**
   * Check if the given class is a library class.
   *
   * @param className
   * @return
   */
  public boolean isLibraryClass(String className) {
    return nameToLibraryClass.containsKey(className);
  }

  /**
   * Check if the given class is a library class.
   *
   * @param cls
   * @return
   */
  public boolean isLibraryClass(SootClass cls) {
    return isLibraryClass(cls.getName());
  }

  /**
   * Check if the given type is for a library class.
   *
   * @param type
   * @return
   */
  public boolean isLibraryClass(Type type) {
    return isLibraryClass(getBaseType(type).toString());
  }

  /**
   * Check if the given method is an application method.
   *
   * @param method
   * @return
   */
  public boolean isApplicationMethod(SootMethod method) {
    return isApplicationClass(method.getDeclaringClass());
  }

  /**
   * Check if the given method is an application method.
   *
   * @param methodSignature
   * @return
   */
  public boolean isApplicationMethod(String methodSignature) {
    return isApplicationClass(signatureToClass(methodSignature));
  }

  /**
   * Check if the given method is a library method.
   *
   * @param method
   * @return
   */
  public boolean isLibraryMethod(SootMethod method) {
    return isLibraryClass(method.getDeclaringClass());
  }

  /**
   * Check if the given method is a library method.
   *
   * @param methodSignature
   * @return
   */
  public boolean isLibraryMethod(String methodSignature) {
    return isLibraryClass(signatureToClass(methodSignature));
  }

  /**
   * Check if the given field is an application field.
   *
   * @param field
   * @return
   */
  public boolean isApplicationField(SootField field) {
    return isApplicationClass(field.getDeclaringClass());
  }

  /**
   * Check if the given field is a library field.
   *
   * @param field
   * @return
   */
  public boolean isLibraryField(SootField field) {
    return isLibraryClass(field.getDeclaringClass());
  }

  /**
   * Find all the superclasses of the given class recursively. This also calculates the subclasses
   * relation.
   *
   * @param cls
   * @return
   */
  public LinkedHashSet<SootClass> getSuperclassesOf(SootClass cls) {
    checkLevel(cls);

    if (!classToSuperclasses.containsKey(cls)) {
      LinkedHashSet<SootClass> result = new LinkedHashSet<SootClass>();

      if (cls.hasSuperclass()) {
        // Direct superclass
        result.add(getDirectSuperclassOf(cls));

        // Direct subclasses
        getDirectSubclassesOf(cls.getSuperclass()).add(cls);

        // Superclasses of direct superclass
        result.addAll(getSuperclassesOf(cls.getSuperclass()));
      }
      classToSuperclasses.put(cls, result);

      // Calculate the subclasses relation
      for (SootClass superClass : result) {
        getSubclassesOf(superClass).add(cls);

        // Calculate the concrete subclasses relation if cls is
        // concrete.
        if (cls.isConcrete()) {
          getConcreteSubclassesOf(superClass).add(cls);

          if (isLibraryClass(cls)) {
            getLibraryConcreteSubclassesOf(superClass).add(cls);
          }
        }
      }
    }

    return classToSuperclasses.get(cls);
  }

  /**
   * Get all the subclasses of the given class.
   *
   * @param cls
   * @return
   */
  public LinkedHashSet<SootClass> getSubclassesOf(SootClass cls) {
    checkLevel(cls);

    if (!classToSubclasses.containsKey(cls)) {
      classToSubclasses.put(cls, new LinkedHashSet<SootClass>());
    }

    return classToSubclasses.get(cls);
  }

  /**
   * Get all the concrete subclasses of the given class.
   *
   * @param cls
   * @return
   */
  public LinkedHashSet<SootClass> getConcreteSubclassesOf(SootClass cls) {
    checkLevel(cls);

    if (!classToConcreteSubclasses.containsKey(cls)) {
      classToConcreteSubclasses.put(cls, new LinkedHashSet<SootClass>());
    }

    return classToConcreteSubclasses.get(cls);
  }

  /**
   * Get all the library concrete subclasses of the given class.
   *
   * @param cls
   * @return
   */
  public LinkedHashSet<SootClass> getLibraryConcreteSubclassesOf(SootClass cls) {
    checkLevel(cls);

    if (!classToLibraryConcreteSubclasses.containsKey(cls)) {
      classToLibraryConcreteSubclasses.put(cls, new LinkedHashSet<SootClass>());
    }

    return classToLibraryConcreteSubclasses.get(cls);
  }

  /**
   * Get the direct superclass of the given class.
   *
   * @param cls
   * @return
   */
  public SootClass getDirectSuperclassOf(SootClass cls) {
    checkLevel(cls);

    if (!classToDirectSuperclass.containsKey(cls)) {
      classToDirectSuperclass.put(cls, cls.getSuperclass());
    }

    return classToDirectSuperclass.get(cls);
  }

  /**
   * Get all the direct subclasses of the given class.
   *
   * @param cls
   * @return
   */
  public LinkedHashSet<SootClass> getDirectSubclassesOf(SootClass cls) {
    checkLevel(cls);

    if (!classToDirectSubclasses.containsKey(cls)) {
      classToDirectSubclasses.put(cls, new LinkedHashSet<SootClass>());
    }

    return classToDirectSubclasses.get(cls);
  }

  /**
   * Check if class A is a possible child of class B.
   *
   * @param possibleChild
   * @param cls
   * @return
   */
  public boolean isSubclassOf(SootClass possibleChild, SootClass cls) {
    return getSuperclassesOf(possibleChild).contains(cls);
  }

  /**
   * Check if class A is a possible concrete child of class B.
   *
   * @param possibleChild
   * @param cls
   * @return
   */
  public boolean isConcreteSubclassOf(SootClass possibleChild, SootClass cls) {
    return getConcreteSubclassesOf(cls).contains(possibleChild);
  }

  /**
   * Check if class A is a possible library concrete child of class B.
   *
   * @param possibleChild
   * @param cls
   * @return
   */
  public boolean isLibraryConcreteSubclassOf(SootClass possibleChild, SootClass cls) {
    return getLibraryConcreteSubclassesOf(cls).contains(possibleChild);
  }

  /**
   * Check if class A is a possible parent of class B.
   *
   * @param possibleParent
   * @param cls
   * @return
   */
  public boolean isSuperclassOf(SootClass possibleParent, SootClass cls) {
    return getSuperclassesOf(cls).contains(possibleParent);
  }

  /**
   * Find all the super interfaces of a given Soot class recursively.
   *
   * @param cls
   * @return
   */
  public LinkedHashSet<SootClass> getSuperinterfacesOf(SootClass cls) {
    checkLevel(cls);

    if (!classToSuperinterfaces.containsKey(cls)) {
      LinkedHashSet<SootClass> result = new LinkedHashSet<SootClass>();

      // Direct superinterfaces
      result.addAll(getDirectSuperinterfacesOf(cls));

      // Direct implementers
      for (SootClass iface : getDirectSuperinterfacesOf(cls)) {
        getDirectImplementersOf(iface).add(cls);
      }

      // Superinterface of a direct superinterface
      for (SootClass iface : getDirectSuperinterfacesOf(cls)) {
        result.addAll(getSuperinterfacesOf(iface));
      }

      // Superinterface of the direct superclass
      if (cls.hasSuperclass()) {
        result.addAll(getSuperinterfacesOf(cls.getSuperclass()));
      }
      classToSuperinterfaces.put(cls, result);

      // Calculate the implementersOf relation
      for (SootClass iface : result) {
        getImplementersOf(iface).add(cls);

        // Calculate the concrete implementersOf relation if cls is
        // concrete
        if (cls.isConcrete()) {
          getConcreteImplementersOf(iface).add(cls);

          if (isLibraryClass(cls)) {
            getLibraryConcreteImplementersOf(iface).add(cls);
          }
        }
      }
    }

    return classToSuperinterfaces.get(cls);
  }

  /**
   * Get all the superinterfaces including the given interface.
   *
   * @param iface
   * @return
   */
  public LinkedHashSet<SootClass> getSuperinterfacesOfIncluding(SootClass iface) {
    LinkedHashSet<SootClass> result = new LinkedHashSet<SootClass>();
    result.add(iface);
    result.addAll(getSuperinterfacesOf(iface));
    return result;
  }

  /**
   * Get all the direct superinterfaces of the given class.
   *
   * @param cls
   * @return
   */
  public LinkedHashSet<SootClass> getDirectSuperinterfacesOf(SootClass cls) {
    checkLevel(cls);

    if (!classToDirectSuperinterfaces.containsKey(cls)) {
      LinkedHashSet<SootClass> ifaces = new LinkedHashSet<SootClass>(cls.getInterfaces());
      classToDirectSuperinterfaces.put(cls, ifaces);
    }

    return classToDirectSuperinterfaces.get(cls);
  }

  /**
   * Get all the direct implementers of the given interface.
   *
   * @param iface
   * @return
   */
  public LinkedHashSet<SootClass> getDirectImplementersOf(SootClass iface) {
    checkLevel(iface);

    if (!interfaceToDirectImplementers.containsKey(iface)) {
      interfaceToDirectImplementers.put(iface, new LinkedHashSet<SootClass>());
    }

    return interfaceToDirectImplementers.get(iface);
  }

  /**
   * Get all the implementers of the given interface.
   *
   * @param iface
   * @return
   */
  public LinkedHashSet<SootClass> getImplementersOf(SootClass iface) {
    checkLevel(iface);

    if (!interfaceToImplementers.containsKey(iface)) {
      interfaceToImplementers.put(iface, new LinkedHashSet<SootClass>());
    }

    return interfaceToImplementers.get(iface);
  }

  /**
   * Get all the concrete implementers of the given interface.
   *
   * @param iface
   * @return
   */
  public LinkedHashSet<SootClass> getConcreteImplementersOf(SootClass iface) {
    checkLevel(iface);

    if (!interfaceToConcreteImplementers.containsKey(iface)) {
      interfaceToConcreteImplementers.put(iface, new LinkedHashSet<SootClass>());
    }

    return interfaceToConcreteImplementers.get(iface);
  }

  /**
   * Get all the library concrete implementers of the given interface.
   *
   * @param iface
   * @return
   */
  public LinkedHashSet<SootClass> getLibraryConcreteImplementersOf(SootClass iface) {
    checkLevel(iface);

    if (!interfaceToLibraryConcreteImplementers.containsKey(iface)) {
      interfaceToLibraryConcreteImplementers.put(iface, new LinkedHashSet<SootClass>());
    }

    return interfaceToLibraryConcreteImplementers.get(iface);
  }

  /**
   * Check if class A implements interface B.
   *
   * @param possibleChild
   * @param iface
   * @return
   */
  public boolean isSubinterfaceOf(SootClass possibleChild, SootClass iface) {
    return getSuperinterfacesOf(possibleChild).contains(iface);
  }

  /**
   * Check if class A is a superinterface of class B.
   *
   * @param possibleParent
   * @param cls
   * @return
   */
  public boolean isSuperinterfaceOf(SootClass possibleParent, SootClass cls) {
    return getSuperinterfacesOf(cls).contains(possibleParent);
  }

  /**
   * Get the set of all supermethods of the given method.
   *
   * @param method
   * @return
   */
  public Set<SootMethod> getSuperMethodsOf(SootMethod method) {
    if (canOverride(method)) {
      if (!methodToSuperMethods.containsKey(method)) {
        Set<SootMethod> result = new HashSet<SootMethod>();

        if (hasSuperclassesSuperMethods(method)) {
          result.addAll(getSuperclassesSuperMethodsOf(method));
        }

        if (hasSuperinterfacesSuperMethods(method)) {
          result.addAll(getSuperinterfacesSuperMethodsOf(method));
        }

        methodToSuperMethods.put(method, result);
      }

      return methodToSuperMethods.get(method);
    }

    return null;
  }

  /**
   * Check if the given methods has any supermethods.
   *
   * @param method
   * @return
   */
  public boolean hasSuperMethods(SootMethod method) {
    Set<SootMethod> superMethods = getSuperMethodsOf(method);
    return superMethods != null && !superMethods.isEmpty();
  }

  /**
   * Check if this method has a concrete supermethod.
   *
   * @param method
   * @return
   */
  public boolean hasConcreteSuperMethod(SootMethod method) {
    if (hasSuperMethods(method)) {
      for (SootMethod superMethod : getSuperMethodsOf(method)) {
        if (superMethod.isConcrete()) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Check if this method has an abstract supermethod.
   *
   * @param method
   * @return
   */
  public boolean hasAbstractSuperMethod(SootMethod method) {
    if (hasSuperMethods(method)) {
      for (SootMethod superMethod : getSuperMethodsOf(method)) {
        if (superMethod.isAbstract()) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Get the set of methods overridden by the given method in its superclasses.
   *
   * @param method
   * @return
   */
  public LinkedHashSet<SootMethod> getSuperclassesSuperMethodsOf(SootMethod method) {
    if (canOverride(method)) {
      if (!methodToSuperclassesSuperMethods.containsKey(method)) {
        LinkedHashSet<SootMethod> result = new LinkedHashSet<SootMethod>();

        for (SootClass superClass : getSuperclassesOf(method.getDeclaringClass())) {
          if (superClass.declaresMethod(method.getNumberedSubSignature())) {
            // NOTE: Private methods are ignored in calculating
            // supermethods from superclasses
            SootMethod m = superClass.getMethod(method.getNumberedSubSignature());
            if (!m.isPrivate()) {
              result.add(m);
            }
          }
        }

        methodToSuperclassesSuperMethods.put(method, result);
      }

      return methodToSuperclassesSuperMethods.get(method);
    }

    return null;
  }

  /**
   * Check if the given method can override methods in superclasses.
   *
   * @param method
   * @return
   */
  public boolean hasSuperclassesSuperMethods(SootMethod method) {
    LinkedHashSet<SootMethod> superMethods = getSuperclassesSuperMethodsOf(method);
    return superMethods != null && !superMethods.isEmpty();
  }

  /**
   * Get the set of methods overridden by this method in its superinterfaces.
   *
   * @param method
   * @return
   */
  public Set<SootMethod> getSuperinterfacesSuperMethodsOf(SootMethod method) {
    if (canOverride(method)) {
      if (!methodToSuperinterfacesSuperMethods.containsKey(method)) {
        Set<SootMethod> result = new HashSet<SootMethod>();

        for (SootClass superInterface : getSuperinterfacesOf(method.getDeclaringClass())) {
          if (superInterface.declaresMethod(method.getNumberedSubSignature())) {
            result.add(superInterface.getMethod(method.getNumberedSubSignature()));
          }
        }

        methodToSuperinterfacesSuperMethods.put(method, result);
      }

      return methodToSuperinterfacesSuperMethods.get(method);
    }

    return null;
  }

  /**
   * Check if the given method can override methods in its superinterfaces.
   *
   * @param method
   * @return
   */
  public boolean hasSuperinterfacesSuperMethods(SootMethod method) {
    Set<SootMethod> superMethods = getSuperinterfacesSuperMethodsOf(method);
    return superMethods != null && !superMethods.isEmpty();
  }

  /**
   * Get the topmost supermethod of the given method.
   *
   * @param method
   * @return
   */
  public SootMethod getTopmostSuperMethodOf(SootMethod method) {
    if (canOverride(method)) {
      if (!methodToTopmostSuperMethod.containsKey(method)) {
        SootMethod superMethod = null;

        if (hasTopmostSuperclassesSuperMethod(method)) {
          superMethod = getTopmostSuperclassesSuperMethodOf(method);
        } else if (hasTopmostSuperinterfacesSuperMethod(method)) {
          superMethod = getTopmostSuperinterfacesSuperMethodOf(method);
        }

        methodToTopmostSuperMethod.put(method, superMethod);
      }

      return methodToTopmostSuperMethod.get(method);
    }

    return null;
  }

  /**
   * Check if the given method has a topmost supermethod.
   *
   * @param method
   * @return
   */
  public boolean hasTopmostSuperMethod(SootMethod method) {
    return getTopmostSuperMethodOf(method) != null;
  }

  /**
   * Get the topmost supermethod in the overridden methods of superclasses. This method would be the
   * one highest in the hierarchy.
   *
   * @param method
   * @return
   */
  public SootMethod getTopmostSuperclassesSuperMethodOf(SootMethod method) {
    if (canOverride(method)) {
      if (!methodToTopmostSuperclassesSuperMethod.containsKey(method)) {
        SootMethod result = null;

        if (hasSuperclassesSuperMethods(method)) {
          LinkedHashSet<SootMethod> s = getSuperclassesSuperMethodsOf(method);
          result = s.stream().skip(s.size() - 1).findFirst().get();
          // result =
          // SetUtils.getLastElement(getSuperclassesSuperMethodsOf(method));
        }

        methodToTopmostSuperclassesSuperMethod.put(method, result);
      }

      return methodToTopmostSuperclassesSuperMethod.get(method);
    }

    return null;
  }

  /**
   * Check if a method has a topmost supermethod in the overridden methods of superclasses. This
   * method would be the one highest in the hierarchy.
   *
   * @param method
   * @return
   */
  public boolean hasTopmostSuperclassesSuperMethod(SootMethod method) {
    return getTopmostSuperclassesSuperMethodOf(method) != null;
  }

  /**
   * Get the topmost supermethod in the overridden methods of superinterfaces. Get the overridden
   * methods of superinterfaces, remove the ones that have supermethods in this set, sort them
   * alphabetically and return the first one.
   *
   * @param method
   * @return
   */
  public SootMethod getTopmostSuperinterfacesSuperMethodOf(SootMethod method) {
    if (canOverride(method)) {
      if (!methodToTopmostSuperinterfacesSuperMethod.containsKey(method)) {
        SootMethod result = null;

        if (hasSuperinterfacesSuperMethods(method)) {
          Set<SootMethod> impureResult = getSuperinterfacesSuperMethodsOf(method);

          if (!impureResult.isEmpty()) {
            result = purifySuperinterfacesSuperMethodsSet(impureResult);
          }
        }

        methodToTopmostSuperinterfacesSuperMethod.put(method, result);
      }

      return methodToTopmostSuperinterfacesSuperMethod.get(method);
    }

    return null;
  }

  /**
   * Check if a method has a topmost supermethod in the overridden methods of superinterfaces.
   *
   * @param method
   * @return
   */
  public boolean hasTopmostSuperinterfacesSuperMethod(SootMethod method) {
    return getTopmostSuperinterfacesSuperMethodOf(method) != null;
  }

  /**
   * Check if method A overrides (i.e., sub method of) method B.
   *
   * @param possibleChild
   * @param method
   * @return
   */
  public boolean isOverrideOf(SootMethod possibleChild, SootMethod method) {
    return hasSuperMethods(possibleChild) && getSuperMethodsOf(possibleChild).contains(method);
  }

  /**
   * Check if method A is a supermethod for method B.
   *
   * @param possibleParent
   * @param method
   * @return
   */
  public boolean isSuperMethodOf(SootMethod possibleParent, SootMethod method) {
    return hasSuperMethods(method) && getSuperMethodsOf(method).contains(possibleParent);
  }

  /**
   * Get the set of all library supermethods for the methods in the given class.
   *
   * @param cls
   * @return
   */
  public Set<SootMethod> getLibrarySuperMethodsOf(SootClass cls) {
    if (!classToLibrarySuperMethods.containsKey(cls)) {
      Set<SootMethod> result = new HashSet<SootMethod>();

      for (SootMethod method : cls.getMethods()) {
        if (hasTopmostSuperMethod(method)) {
          SootMethod topmostSuperMethod = getTopmostSuperMethodOf(method);

          if (isLibraryMethod(topmostSuperMethod)) {
            result.add(topmostSuperMethod);

            // Get the array parameters in this topmost library
            // super method.
            libraryArrayTypeParameters.addAll(getArrayTypeParameters(topmostSuperMethod));
          }
        }
      }

      classToLibrarySuperMethods.put(cls, result);
    }

    return classToLibrarySuperMethods.get(cls);
  }

  /**
   * Get the set of all library supermethods of all the application methods.
   *
   * @return
   */
  public Set<SootMethod> getLibrarySuperMethodsOfApplicationMethods() {
    return librarySuperMethodsOfApplicationMethods;
  }

  /**
   * Check if the given method is a library method referenced by the application.
   *
   * @param libraryMethod
   * @return
   */
  public boolean isLibraryMethodReferencedInApplication(SootMethod libraryMethod) {
    return libraryMethodsReferencedInApplication.contains(libraryMethod);
  }

  /**
   * Get the set of all library methods referenced in the application.
   *
   * @return
   */
  public Set<SootMethod> getLibraryMethodsReferencedInApplication() {
    return libraryMethodsReferencedInApplication;
  }

  /**
   * Get the application constant pool.
   *
   * @return
   */
  public AverroesApplicationConstantPool getApplicationConstantPool() {
    return applicationConstantPool;
  }

  /**
   * Get the set of all library fields referenced in the application.
   *
   * @return
   */
  public Set<SootField> getLibraryFieldsReferencedInApplication() {
    return libraryFieldsReferencedInApplication;
  }

  /**
   * Get the set of all abstract library classes;
   *
   * @return
   */
  public Set<SootClass> getAbstractLibraryClasses() {
    return abstractLibraryClasses;
  }

  /**
   * Get the set of all concrete library classes;
   *
   * @return
   */
  public Set<SootClass> getConcreteLibraryClasses() {
    return concreteLibraryClasses;
  }

  /**
   * Get the set of all library interfaces;
   *
   * @return
   */
  public Set<SootClass> getLibraryInterfaces() {
    return libraryInterfaces;
  }

  /**
   * Get the set of all abstract library classes not implemented in the library (i.e., library
   * classes that have no concrete subclasses in the library).
   *
   * @return
   */
  public Set<SootClass> getAbstractLibraryClassesNotImplementedInLibrary() {
    return abstractLibraryClassesNotImplementedInLibrary;
  }

  /**
   * Get the set of all library interfaces not implemented in the library (i.e., library interfaces
   * that have no concrete implementers in the library).
   *
   * @return
   */
  public Set<SootClass> getLibraryInterfacesNotImplementedInLibrary() {
    return libraryInterfacesNotImplementedInLibrary;
  }

  /** Initialize some stuff. */
  private void initialize() {
    collectClassNames();
    calculateBaseRelations();
    createClassTrees();
    findLibrarySuperMethodsOfApplicationMethods();
    findLibraryEntitiesReferencedInApplication();
    findUnimplementedLibraryClasses();
  }

  /** Collect the class names and some class properties (e.g., abstract, interface). */
  private void collectClassNames() {
    for (SootClass cls : classes) {
      // Get the class names
      nameToClass.put(cls.getName(), cls);
      if (AverroesOptions.isApplicationClass(cls)) {
        nameToApplicationClass.put(cls.getName(), cls);
        applicationMethodCount += cls.getMethodCount();
        applicationFieldCount += cls.getFieldCount();
      } else {
        nameToLibraryClass.put(cls.getName(), cls);
        libraryMethodCount += cls.getMethodCount();
        libraryFieldCount += cls.getFieldCount();

        // Get the return array types of library methods
        libraryArrayTypeReturns.addAll(getArrayTypeReturns(cls));

        // Get the abstract library classes, interfaces, and concrete
        // library classes
        if (isAbstractClass(cls)) {
          abstractLibraryClasses.add(cls);
        } else if (cls.isInterface()) {
          libraryInterfaces.add(cls);
        } else {
          concreteLibraryClasses.add(cls);
        }
      }
    }

    cleanupLibraryArrayTypeReturns();
  }

  /**
   * Cleanup the library array type returns set from all those classes that will not be in the
   * hierarchy when it's built.
   */
  private void cleanupLibraryArrayTypeReturns() {
    Set<ArrayType> toRemove = new HashSet<ArrayType>();

    for (ArrayType type : libraryArrayTypeReturns) {
      if (!isLibraryClass(type)) {
        toRemove.add(type);
      }
    }

    libraryArrayTypeReturns.removeAll(toRemove);
  }

  /**
   * Get the array types from the returns of the methods of this class.
   *
   * @param cls
   * @return
   */
  private Set<ArrayType> getArrayTypeReturns(SootClass cls) {
    Set<ArrayType> result = new HashSet<ArrayType>();

    for (SootMethod method : cls.getMethods()) {
      if (method.getReturnType() instanceof ArrayType) {
        result.add((ArrayType) method.getReturnType());
      }
    }

    return result;
  }

  /**
   * Get the array types from the parameters of this method.
   *
   * @param method
   * @return
   */
  private Set<ArrayType> getArrayTypeParameters(SootMethod method) {
    Set<ArrayType> result = new HashSet<ArrayType>();

    for (Object obj : method.getParameterTypes()) {
      Type type = (Type) obj;

      if (type instanceof ArrayType) {
        result.add((ArrayType) type);
      }
    }

    return result;
  }

  /** Calculate the base relations (i.e., class hierarchy). */
  private void calculateBaseRelations() {
    for (SootClass cls : classes) {
      getSuperclassesOf(cls);
      getSuperinterfacesOf(cls);
    }
  }

  /** Create the class trees for the application and the library. */
  private void createClassTrees() {
    for (SootClass cls : classes) {
      if (isApplicationClass(cls)) {
        applicationClasses.add(cls);
      } else {
        libraryClasses.add(cls);
      }
    }
  }

  /** Find all the unimplemented library classes. */
  private void findUnimplementedLibraryClasses() {
    findUnimplementedAbstractLibraryClasses();
    findUnimplementedLibraryInterfaces();
  }

  /** Find all the unimplemented abstract library classes. */
  private void findUnimplementedAbstractLibraryClasses() {
    for (SootClass cls : abstractLibraryClasses) {
      if (getLibraryConcreteSubclassesOf(cls).isEmpty()) {
        abstractLibraryClassesNotImplementedInLibrary.add(cls);
      }
    }
  }

  /** Find all the unimplemented library interfaces. */
  private void findUnimplementedLibraryInterfaces() {
    for (SootClass iface : libraryInterfaces) {
      if (getLibraryConcreteImplementersOf(iface).isEmpty()) {
        libraryInterfacesNotImplementedInLibrary.add(iface);
      }
    }
  }

  /**
   * Find the set of all library supermethods in all the application classes.
   *
   * @return
   */
  private void findLibrarySuperMethodsOfApplicationMethods() {
    for (SootClass cls : getApplicationClasses()) {
      librarySuperMethodsOfApplicationMethods.addAll(getLibrarySuperMethodsOf(cls));
    }
  }

  /**
   * Find all the library entities (methods and fields) that are referenced in the application
   * constant pool.
   */
  private void findLibraryEntitiesReferencedInApplication() {
    applicationConstantPool = new AverroesApplicationConstantPool(this);

    findApplicationClassesReferencedByName();
    findLibraryMethodsReferencedInApplication();
    findLibraryFieldsReferencedInApplication();
  }

  private void findApplicationClassesReferencedByName() {
    applicationClassesReferencedByName.addAll(applicationConstantPool.getApplicationClasses());
  }

  /** Find all the library methods referenced by the application. */
  private void findLibraryMethodsReferencedInApplication() {
    libraryMethodsReferencedInApplication.addAll(librarySuperMethodsOfApplicationMethods);
    libraryMethodsReferencedInApplication.addAll(applicationConstantPool.getLibraryMethods());
  }

  /** Find all the library fields referenced by the application. */
  private void findLibraryFieldsReferencedInApplication() {
    libraryFieldsReferencedInApplication.addAll(applicationConstantPool.getLibraryFields());
  }

  /**
   * Purify the set of superinterfaces supermethods. That's remove methods whose declaring class has
   * a superinterface within this same set, then sort the set of methods alphabetically according to
   * the name of the declaring class.
   *
   * @param superMethods
   * @return
   */
  private SootMethod purifySuperinterfacesSuperMethodsSet(Set<SootMethod> superMethods) {
    SortedSet<SootMethod> sorted = new TreeSet<SootMethod>(new SootMethodComparer());

    for (SootMethod method : superMethods) {
      if (!containsSuperinterface(superMethods, method.getDeclaringClass())) {
        sorted.add(method);
      }
    }

    return sorted.first(); // we are sure that the input set is not empty
  }

  /**
   * Check if the given set of methods contain a method whose declaring class is a superinterface of
   * the given class.
   *
   * @param methods
   * @param cls
   * @return
   */
  private boolean containsSuperinterface(Set<SootMethod> methods, SootClass cls) {
    for (SootMethod method : methods) {
      if (isSuperinterfaceOf(method.getDeclaringClass(), cls)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Check if the given class is resolved at least at the level {@link ResolvingLevel.#SIGNATURES}.
   *
   * @param cls
   */
  private void checkLevel(SootClass cls) {
    if (cls.resolvingLevel() < ResolvingLevel.SIGNATURES.value()) {
      throw new RuntimeException(
          "Trying to process class " + cls + ", and it is not resolved at level SIGNATURES.");
    }
  }

  /**
   * Check that the given method can override other methods or not (i.e., it is not a constructor,
   * not a static method (including the static initializer), nor is it declared in
   * java.lang.Object).
   *
   * @param method
   * @return
   */
  private boolean canOverride(SootMethod method) {
    return !method.isConstructor() && !method.isStatic();
  }

  /**
   * Cleanup a library class from any removable methods. A method is removable if it is not
   * referenced by the application and removing it will not mess up the class hierarchy. In
   * addition, remove any exceptions that are not referenced by the application. Finally, change any
   * native method to be non-native.
   *
   * @param libraryClass
   */
  private void cleanupMethodsInLibraryClass(SootClass libraryClass) {
    Set<SootMethod> toRemove = new HashSet<SootMethod>();

    for (SootMethod method : libraryClass.getMethods()) {
      if (isLibraryMethodRemovable(method)) {
        toRemove.add(method);
      } else {
        cleanupLibraryMethodExceptions(method);
        cleanupLibraryMethodTags(method);
        makeNotNative(method);
      }
    }

    // Remove the methods the proper way.
    for (SootMethod method : toRemove) {
      libraryClass.removeMethod(method);

      // Update the method counts
      removedLibraryMethodCount++;
      libraryMethodCount--;
    }
  }

  /**
   * Cleanup the exceptions list of a library method.
   *
   * @param libraryMethod
   */
  private void cleanupLibraryMethodExceptions(SootMethod libraryMethod) {
    Set<SootClass> toRemove = new HashSet<SootClass>();
    for (SootClass exception : libraryMethod.getExceptions()) {
      if (isLibraryMethodExceptionRemovable(exception)) {
        toRemove.add(exception);
      }
    }

    // Remove the exceptions the proper way.
    for (SootClass exception : toRemove) {
      libraryMethod.removeException(exception);
    }
  }

  /**
   * Cleanup the tag list of a library method. There's no need for tags/annotations in a placeholder
   * library.
   *
   * @param libraryMethod
   */
  private void cleanupLibraryMethodTags(SootMethod libraryMethod) {
    Set<String> toRemove = new HashSet<String>();
    for (Tag tag : libraryMethod.getTags()) {
      toRemove.add(tag.getName());
    }

    // Remove the exceptions the proper way.
    for (String tag : toRemove) {
      libraryMethod.removeTag(tag);
    }
  }

  /**
   * Cleanup the tag list of a library class. There's no need for tags/annotations in a placeholder
   * library.
   *
   * @param libraryMethod
   */
  private void cleanupLibraryClassTags(SootClass libraryClass) {
    Set<String> toRemove = new HashSet<String>();
    for (Tag tag : libraryClass.getTags()) {
      toRemove.add(tag.getName());
    }

    // Remove the exceptions the proper way.
    for (String tag : toRemove) {
      libraryClass.removeTag(tag);
    }
  }

  /**
   * Cleanup a library class from any removable fields. A field is removable if it is not referenced
   * by the application.
   *
   * @param libraryClass
   */
  private void cleanupFieldsInLibraryClass(SootClass libraryClass) {
    Set<SootField> toRemove = new HashSet<SootField>();

    for (SootField field : libraryClass.getFields()) {
      if (isLibraryFieldRemovable(field)) {
        toRemove.add(field);
      } else {
        cleanupLibraryFieldTags(field);
      }
    }

    // Remove the fields the proper way.
    for (SootField field : toRemove) {
      libraryClass.removeField(field);

      // Update the field counts
      removedLibraryFieldCount++;
      libraryFieldCount--;
    }
  }

  /**
   * Cleanup the tag list of a library field. There's no need for tags/annotations in a placeholder
   * library.
   *
   * @param libraryfield
   */
  private void cleanupLibraryFieldTags(SootField libraryfield) {
    Set<String> toRemove = new HashSet<String>();
    for (Tag tag : libraryfield.getTags()) {
      toRemove.add(tag.getName());
    }

    // Remove the exceptions the proper way.
    for (String tag : toRemove) {
      libraryfield.removeTag(tag);
    }
  }

  /**
   * Check if it is safe to remove the given library method from its class.
   *
   * @param libraryField
   * @return
   */
  private boolean isLibraryMethodRemovable(SootMethod libraryMethod) {
    if (libraryMethod.isPrivate()
        || isLibraryMethodReturnTypeRemovable(libraryMethod)
        || isLibraryMethodParameterTypesRemovable(libraryMethod)) {
      return true;
    } else if (isBasicLibraryMethod(libraryMethod)
        || isLibraryMethodReferencedInApplication(libraryMethod)) {
      return false;
    } else
      return !libraryMethod.isConcrete()
          || hasConcreteSuperMethod(libraryMethod)
          || !hasAbstractSuperMethod(libraryMethod);
  }

  /**
   * Check if the it is safe to remove the given library method exception (i.e., if it is in the set
   * of library classes or not).
   *
   * @param exception
   * @return
   */
  private boolean isLibraryMethodExceptionRemovable(SootClass exception) {
    return !isLibraryClass(exception);
  }

  /**
   * Check if it is safe to remove the given library field from its class. A field is safe to remove
   * if its class is not a library class in this hierarchy (depending on the resolution level
   * SIGNATURES), or if it is a private field or if it is not referenced in the application constant
   * pool.
   *
   * @param libraryField
   * @return
   */
  private boolean isLibraryFieldRemovable(SootField libraryField) {
    return !isLibraryField(libraryField)
        || libraryField.isPrivate()
        || !libraryFieldsReferencedInApplication.contains(libraryField);
  }

  /**
   * Check if any parameter of this methods is removable (i.e., resolved at level less than
   * SIGNATURES).
   *
   * @param method
   * @return
   */
  private boolean isLibraryMethodParameterTypesRemovable(SootMethod method) {
    for (Object obj : method.getParameterTypes()) {
      Type type = getBaseType((Type) obj);
      if (type instanceof RefLikeType && !isLibraryClass(type)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Check if the return type of a method is removable (i.e., resolved at level less than
   * SIGNATURES).
   *
   * @param method
   * @return
   */
  private boolean isLibraryMethodReturnTypeRemovable(SootMethod method) {
    Type type = getBaseType(method.getReturnType());
    return type instanceof RefLikeType && !isLibraryClass(type);
  }
}
