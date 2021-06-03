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

import averroes.frameworks.options.FrameworksOptions;
import soot.*;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A utility class for the {@link Scene} class.
 *
 * @author karim
 */
public class SootSceneUtil {

  /**
   * Add a basic class to the {@link Scene}.
   *
   * @param className
   */
  private static void addCommonDynamicClass(String className) {
    if (SourceLocator.v().getClassSource(className) != null) {
      Scene.v().addBasicClass(className);
    }
  }

  /**
   * Add the common dynamic classes to the {@link Scene} as basic classes.
   *
   */
  public static void addCommonDynamicClasses() {
    /*
     * For simulating the FileSystem class, we need the implementation of
     * the FileSystem, but the classes are not loaded automatically due to
     * the indirection via native code.
     */
    addCommonDynamicClass("java.io.UnixFileSystem");
    addCommonDynamicClass("java.io.WinNTFileSystem");
    addCommonDynamicClass("java.io.Win32FileSystem");

    /* java.net.URL loads handlers dynamically */
    addCommonDynamicClass("sun.net.www.protocol.file.Handler");
    addCommonDynamicClass("sun.net.www.protocol.ftp.Handler");
    addCommonDynamicClass("sun.net.www.protocol.http.Handler");
    addCommonDynamicClass("sun.net.www.protocol.https.Handler");
    addCommonDynamicClass("sun.net.www.protocol.jar.Handler");
  }

  /**
   * Return the set of classes that Averroes processes in the Scene. These include the library
   * classes if {@link FrameworksOptions#isIncludeDependencies()} return true.
   *
   * @return
   */
  public static Set<SootClass> getClasses() {
    Set<SootClass> result = new HashSet<SootClass>(Scene.v().getApplicationClasses());
    if (FrameworksOptions.isIncludeDependencies()) result.addAll(Scene.v().getLibraryClasses());
    return result;
  }

  /**
   * Return the set of classes that Averroes processes in the Scene. These include the library
   * classes if {@link FrameworksOptions#isIncludeDependencies()} return true.
   *
   * @return
   */
  public static SortedSet<SootClass> getSortedClasses() {
    SortedSet<SootClass> result = new TreeSet<SootClass>(new SootClassComparer());
    result.addAll(Scene.v().getApplicationClasses());
    if (FrameworksOptions.isIncludeDependencies()) result.addAll(Scene.v().getLibraryClasses());
    return result;
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
}
