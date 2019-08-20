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
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.VoidType;

/**
 * A database of all the classes required by Soot to run properly. These are the classes from {@link
 * Scene#addSootBasicClasses()}.
 *
 * @author Karim Ali
 */
public class SootBasicClassesDatabase {

  private Hierarchy hierarchy;
  private HashMap<String, String> fakeHierarchy;

  public SootBasicClassesDatabase(Hierarchy hierarchy) {
    this.hierarchy = hierarchy;
    fakeHierarchy = new HashMap<String, String>();

    initialize();
  }

  /**
   * Get all the basic classes that are missing from the hierarchy.
   *
   * @return
   * @throws IOException
   */
  public Set<SootClass> getMissingBasicClasses() throws IOException {
    Set<SootClass> result = new HashSet<SootClass>();
    for (String className : listClasses()) {
      if (!hierarchy.isLibraryClass(className)) {
        SootClass basicClass = new SootClass(className);

        // Add the register method to the finalizer class
        if (className.equals(Names.JAVA_LANG_REF_FINALIZER)) {
          SootMethod register =
              new SootMethod(
                  "register",
                  Arrays.asList(hierarchy.getJavaLangObject().getType()),
                  VoidType.v(),
                  Modifier.STATIC);
          basicClass.addMethod(register);
          AverroesJimpleBody body = new AverroesJimpleBody(register);
          body.insertStandardJimpleBodyFooter();
          body.validate();
        }

        result.add(basicClass);
      }
    }

    // Set the superclass of the basic classes based on the underlying
    // hierarchy
    for (SootClass cls : result) {
      if (fakeHierarchy.containsKey(cls.getName())) {
        cls.setSuperclass(Scene.v().getSootClass(fakeHierarchy.get(cls.getName())));
      }
    }
    return result;
  }

  /**
   * Get a list of all the basic class names.
   *
   * @return
   */
  public Set<String> listClasses() {
    Set<String> classNames = new HashSet<String>();

    // NOTE: This list of classes are the ones in the
    // Scene.addSootBasicClasses method.
    classNames.add("java.lang.Object");
    classNames.add("java.lang.Class");

    classNames.add("java.lang.Void");
    classNames.add("java.lang.Boolean");
    classNames.add("java.lang.Byte");
    classNames.add("java.lang.Character");
    classNames.add("java.lang.Short");
    classNames.add("java.lang.Integer");
    classNames.add("java.lang.Long");
    classNames.add("java.lang.Float");
    classNames.add("java.lang.Double");

    classNames.add("java.lang.String");
    classNames.add("java.lang.StringBuffer");

    classNames.add("java.lang.Error");
    classNames.add("java.lang.AssertionError");
    classNames.add("java.lang.Throwable");
    classNames.add("java.lang.NoClassDefFoundError");
    classNames.add("java.lang.ExceptionInInitializerError");
    classNames.add("java.lang.Exception");
    classNames.add("java.lang.RuntimeException");
    classNames.add("java.lang.ClassNotFoundException");
    classNames.add("java.lang.ArithmeticException");
    classNames.add("java.lang.ArrayStoreException");
    classNames.add("java.lang.ClassCastException");
    classNames.add("java.lang.IllegalMonitorStateException");
    classNames.add("java.lang.IndexOutOfBoundsException");
    classNames.add("java.lang.ArrayIndexOutOfBoundsException");
    classNames.add("java.lang.NegativeArraySizeException");
    classNames.add("java.lang.NullPointerException");
    classNames.add("java.lang.InstantiationError");
    classNames.add("java.lang.InternalError");
    classNames.add("java.lang.OutOfMemoryError");
    classNames.add("java.lang.StackOverflowError");
    classNames.add("java.lang.UnknownError");
    classNames.add("java.lang.ThreadDeath");
    classNames.add("java.lang.ClassCircularityError");
    classNames.add("java.lang.ClassFormatError");
    classNames.add("java.lang.IllegalAccessError");
    classNames.add("java.lang.IncompatibleClassChangeError");
    classNames.add("java.lang.LinkageError");
    classNames.add("java.lang.VerifyError");
    classNames.add("java.lang.NoSuchFieldError");
    classNames.add("java.lang.AbstractMethodError");
    classNames.add("java.lang.NoSuchMethodError");
    classNames.add("java.lang.UnsatisfiedLinkError");

    classNames.add("java.lang.Thread");
    classNames.add("java.lang.Runnable");
    classNames.add("java.lang.Cloneable");

    classNames.add("java.io.Serializable");

    classNames.add("java.lang.ref.Finalizer");

    try {
      classNames.addAll(AverroesOptions.getDynamicLibraryClasses());
    } catch (IOException e) {
      e.printStackTrace();
    }

    return classNames;
  }

  /**
   * Initialize the underlying fake hierarchy. Note: I changed the direct superclass to
   * java.lang.Object wherever it doesn't matter (e.g., java.lang.Byte). I also "round-up" the
   * direct superclass to the nearest existing basic class if the original direct superclass is not
   * in the list (e.g., java.lang.OutOfMemoryError).
   */
  private void initialize() {
    fakeHierarchy.put("java.lang.Class", "java.lang.Object");

    fakeHierarchy.put("java.lang.Void", "java.lang.Object");
    fakeHierarchy.put("java.lang.Boolean", "java.lang.Object");
    fakeHierarchy.put("java.lang.Byte", "java.lang.Object");
    fakeHierarchy.put("java.lang.Character", "java.lang.Object");
    fakeHierarchy.put("java.lang.Short", "java.lang.Object");
    fakeHierarchy.put("java.lang.Integer", "java.lang.Object");
    fakeHierarchy.put("java.lang.Long", "java.lang.Object");
    fakeHierarchy.put("java.lang.Float", "java.lang.Object");
    fakeHierarchy.put("java.lang.Double", "java.lang.Object");

    fakeHierarchy.put("java.lang.String", "java.lang.Object");
    fakeHierarchy.put("java.lang.StringBuffer", "java.lang.Object");

    fakeHierarchy.put("java.lang.Error", "java.lang.Throwable");
    fakeHierarchy.put("java.lang.AssertionError", "java.lang.Error");
    fakeHierarchy.put("java.lang.Throwable", "java.lang.Object");
    fakeHierarchy.put("java.lang.NoClassDefFoundError", "java.lang.LinkageError");
    fakeHierarchy.put("java.lang.ExceptionInInitializerError", "java.lang.LinkageError");
    fakeHierarchy.put("java.lang.RuntimeException", "java.lang.Exception");
    fakeHierarchy.put("java.lang.ClassNotFoundException", "java.lang.Exception");
    fakeHierarchy.put("java.lang.ArithmeticException", "java.lang.RuntimeException");
    fakeHierarchy.put("java.lang.ArrayStoreException", "java.lang.RuntimeException");
    fakeHierarchy.put("java.lang.ClassCastException", "java.lang.RuntimeException");
    fakeHierarchy.put("java.lang.IllegalMonitorStateException", "java.lang.RuntimeException");
    fakeHierarchy.put("java.lang.IndexOutOfBoundsException", "java.lang.RuntimeException");
    fakeHierarchy.put(
        "java.lang.ArrayIndexOutOfBoundsException", "java.lang.IndexOutOfBoundsException");
    fakeHierarchy.put("java.lang.NegativeArraySizeException", "java.lang.RuntimeException");
    fakeHierarchy.put("java.lang.NullPointerException", "java.lang.RuntimeException");
    fakeHierarchy.put("java.lang.InstantiationError", "java.lang.IncompatibleClassChangeError");
    fakeHierarchy.put("java.lang.InternalError", "java.lang.Error");
    fakeHierarchy.put("java.lang.OutOfMemoryError", "java.lang.Error");
    fakeHierarchy.put("java.lang.StackOverflowError", "java.lang.Error");
    fakeHierarchy.put("java.lang.UnknownError", "java.lang.Error");
    fakeHierarchy.put("java.lang.ThreadDeath", "java.lang.Error");
    fakeHierarchy.put("java.lang.ClassCircularityError", "java.lang.LinkageError");
    fakeHierarchy.put("java.lang.ClassFormatError", "java.lang.LinkageError");
    fakeHierarchy.put("java.lang.IllegalAccessError", "java.lang.IncompatibleClassChangeError");
    fakeHierarchy.put("java.lang.IncompatibleClassChangeError", "java.lang.LinkageError");
    fakeHierarchy.put("java.lang.LinkageError", "java.lang.Error");
    fakeHierarchy.put("java.lang.VerifyError", "java.lang.LinkageError");
    fakeHierarchy.put("java.lang.NoSuchFieldError", "java.lang.IncompatibleClassChangeError");
    fakeHierarchy.put("java.lang.AbstractMethodError", "java.lang.IncompatibleClassChangeError");
    fakeHierarchy.put("java.lang.NoSuchMethodError", "java.lang.IncompatibleClassChangeError");
    fakeHierarchy.put("java.lang.UnsatisfiedLinkError", "java.lang.LinkageError");

    fakeHierarchy.put("java.lang.Thread", "java.lang.Object");
    fakeHierarchy.put("java.lang.Runnable", "java.lang.Object");
    fakeHierarchy.put("java.lang.Cloneable", "java.lang.Object");

    fakeHierarchy.put("java.io.Serializable", "java.lang.Object");

    fakeHierarchy.put("java.lang.ref.Finalizer", "java.lang.Object");
  }
}
