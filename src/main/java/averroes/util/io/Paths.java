/**
 * ***************************************************************************** Copyright (c) 2015
 * Karim Ali and Ondřej Lhoták. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Karim Ali - initial API and implementation and/or initial documentation
 * *****************************************************************************
 */
/** */
package averroes.util.io;

import averroes.frameworks.options.FrameworksOptions;
import averroes.options.AverroesOptions;
import averroes.util.io.Printers.PrinterType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import soot.SootClass;
import soot.SootMethod;

/**
 * Utility class for file-related operations.
 *
 * @author Karim Ali
 */
public class Paths {
  /**
   * The path to the placeholder library class files.
   *
   * @return
   */
  public static File frameworksLibraryClassesOutputDirectory() {
    return java.nio.file.Paths.get(FrameworksOptions.getOutputDirectory(), "classes", "lib")
        .toFile();
  }

  /**
   * The path to the frameworks placeholder library JAR file.
   *
   * @return
   */
  public static File placeholderFrameworkJarFile() {
    return java.nio.file.Paths.get(
            FrameworksOptions.getOutputDirectory(),
            "placeholder-fwk-" + FrameworksOptions.getAnalysis() + ".jar")
        .toFile();
  }

  /**
   * The path to the file where we output Jimple code.
   *
   * @param printerType
   * @param method
   * @return
   */
  public static File jimpleOutputFile(PrinterType printerType, SootMethod method) {
    return dumpFile(jimpleOutputDirectory(printerType), method.getDeclaringClass(), "jimple");
  }

  /**
   * The path to the file where we output the JSON code representation of the given class.
   *
   * @param printerType
   * @param cls
   * @return
   */
  public static File jsonOutputFile(PrinterType printerType, SootClass cls) {
    return dumpFile(jsonOutputDirectory(printerType), cls, "json");
  }

  /**
   * The path to the output file where we output inlining information for the given class.
   *
   * @param cls
   * @return
   */
  public static File inlinerOutputFile(SootClass cls) {
    return dumpFile(inlinerOutputDirectory(PrinterType.OPTIMIZED), cls, "txt");
  }

  /**
   * The directory where we output JSON files.
   *
   * @param printerType
   * @return
   */
  public static Path jsonOutputDirectory(PrinterType printerType) {
    return outputDirectory("json", printerType);
  }

  /**
   * The directory where we output Jimple files.
   *
   * @param printerType
   * @return
   */
  public static Path jimpleOutputDirectory(PrinterType printerType) {
    return outputDirectory("jimple", printerType);
  }

  /**
   * The directory where we output the inliner output files.
   *
   * @param printerType
   * @return
   */
  public static Path inlinerOutputDirectory(PrinterType printerType) {
    return outputDirectory("inliner", printerType);
  }

  /**
   * The path to the output class files.
   *
   * @return
   */
  public static File classesOutputDirectory() {
    return new File(AverroesOptions.getOutputDirectory(), "classes");
  }

  /**
   * The path to the placeholder library class files.
   *
   * @return
   */
  public static File libraryClassesOutputDirectory() {
    return new File(classesOutputDirectory(), "lib");
  }

  /**
   * The path to the placeholder library JAR file.
   *
   * @return
   */
  public static File placeholderLibraryJarFile() {
    return new File(AverroesOptions.getOutputDirectory(), "placeholder-lib.jar");
  }

  /**
   * The path to the JAR file that contains the single file averroes.Library
   *
   * @return
   */
  public static File averroesLibraryClassJarFile() {
    return new File(AverroesOptions.getOutputDirectory(), "averroes-lib-class.jar");
  }

  /**
   * The path to the organized application JAR file.
   *
   * @return
   */
  public static File organizedApplicationJarFile() {
    return new File(AverroesOptions.getOutputDirectory(), "organized-app.jar");
  }

  /**
   * The path to the organized library JAR file.
   *
   * @return
   */
  public static File organizedLibraryJarFile() {
    return new File(AverroesOptions.getOutputDirectory(), "organized-lib.jar");
  }

  /**
   * Find the Jimple files for the given test case.
   *
   * @param printerType
   * @return
   */
  public static Collection<File> findJimpleFiles(PrinterType printerType) {
    return FileUtils.listFiles(
        jimpleOutputDirectory(printerType).toFile(), new String[] {"jimple"}, true);
  }

  /**
   * Find the JSON files for the given test case.
   *
   * @param printerType
   * @return
   */
  public static Collection<File> findJsonFiles(PrinterType printerType) {
    return FileUtils.listFiles(
        jsonOutputDirectory(printerType).toFile(), new String[] {"json"}, true);
  }

  /** Delete the directory that contains the Jimple files for the handwritten model. */
  public static void deleteJimpleExpectedDirectory() {
    deleteDirectory(jimpleOutputDirectory(PrinterType.EXPECTED).toFile());
  }

  /** Delete the directory that contains the JSON files for the handwritten model. */
  public static void deleteJsonExpectedDirectory() {
    deleteDirectory(jsonOutputDirectory(PrinterType.EXPECTED).toFile());
  }

  /** Delete the directory that contains the Jimple files for the generated model. */
  public static void deleteJimpleAnalysisDirectories() {
    deleteDirectory(jimpleOutputDirectory(PrinterType.ORIGINAL).toFile());
    deleteDirectory(jimpleOutputDirectory(PrinterType.GENERATED).toFile());
    deleteDirectory(jimpleOutputDirectory(PrinterType.OPTIMIZED).toFile());
  }

  /** Delete the directory that contains the JSON files for the generated model. */
  public static void deleteJsonAnalysisDirectories() {
    deleteDirectory(jsonOutputDirectory(PrinterType.ORIGINAL).toFile());
    deleteDirectory(jsonOutputDirectory(PrinterType.GENERATED).toFile());
    deleteDirectory(jsonOutputDirectory(PrinterType.OPTIMIZED).toFile());
  }

  public static void deleteClassAnalysisDirectories() {
    deleteDirectory(frameworksLibraryClassesOutputDirectory());
  }

  /**
   * Delete the given directory, recursively.
   *
   * @param directory
   */
  public static void deleteDirectory(String directory) {
    deleteDirectory(java.nio.file.Paths.get(directory).toFile());
  }

  /**
   * Delete the given directory, recursively.
   *
   * @param directory
   */
  private static void deleteDirectory(File directory) {
    try {
      FileUtils.deleteDirectory(directory);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * The path to the output file where we generate some output.
   *
   * @param dir
   * @param cls
   * @param extension
   * @return
   */
  private static File dumpFile(Path dir, SootClass cls, String extension) {
    String file = cls.getName() + "." + extension;

    try {
      FileUtils.forceMkdir(dir.toFile());
    } catch (IOException e) {
      e.printStackTrace();
    }

    return dir.resolve(file).toFile();
  }

  /**
   * The directory where we dump JSON and JIMPLE files.
   *
   * @param prefix
   * @param printerType
   * @return
   */
  private static Path outputDirectory(String prefix, PrinterType printerType) {
    return java.nio.file.Paths.get(
        FrameworksOptions.getOutputDirectory(),
        prefix,
        printerType.name().toLowerCase(),
        FrameworksOptions.getAnalysis());
  }
}
