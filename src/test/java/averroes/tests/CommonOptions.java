package averroes.tests;

import averroes.util.io.FileFilters;
import averroes.util.io.Printers.PrinterType;
import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

/**
 * Class holding common options for testing purposes.
 *
 * @author Karim Ali
 */
public class CommonOptions {

  public static String base = "testsuite";
  public static String output = "output";
  public static String jimple = "jimple";
  public static String json = "json";

  public static IOFileFilter averroesModelFilter =
      FileFilterUtils.or(
          FileFilterUtils.nameFileFilter("averroes-lib-class.jar"),
          FileFilterUtils.nameFileFilter("placeholder-lib.jar"));

  public static String jre = System.getProperty("java.home");

  public static String jreToModel = System.getProperty("java.home");
  //	 Paths.get(new
  // File(".").getAbsolutePath()).getParent().getParent().resolve(Paths.get("averroes-experiments-data", "jre", "1.8.0_60")).toFile();

  /**
   * Get the path to the original library code. This is typically the consolidated library that
   * Averroes conveniently generates.
   *
   * @param testCase
   * @return
   */
  public static String getOriginalLibrary(String testCase) {
    return Paths.get(
            base, testCase, getProjectNamePrefix(testCase), "jars", "averroes", "organized-lib.jar")
        .toString();
  }

  /**
   * Get the path to the Averroes model for the library.
   *
   * @param testCase
   * @param selectHandWrittenModel
   * @return
   */
  public static String getAverroesModel(String testCase, boolean selectHandWrittenModel) {
    String dir = selectHandWrittenModel ? "manual" : "averroes";
    return FileUtils.listFiles(
            Paths.get(base, testCase, getProjectNamePrefix(testCase), "jars", dir).toFile(),
            averroesModelFilter,
            FileFilterUtils.trueFileFilter())
        .stream()
        .map(map -> map.getPath())
        .collect(Collectors.joining(File.pathSeparator));
  }

  /**
   * Get the path to the Averroes model for the library.
   *
   * @param testCase
   * @param selectHandWrittenModel
   * @return
   */
  public static String getAverroesLibraryClass(String testCase, boolean selectHandWrittenModel) {
    String dir = selectHandWrittenModel ? "manual" : "averroes";
    return Paths.get(
            base, testCase, getProjectNamePrefix(testCase), "jars", dir, "averroes-lib-class.jar")
        .toString();
  }

  /**
   * Get the path to the Averroes model for the library.
   *
   * @param testCase
   * @param selectHandWrittenModel
   * @return
   */
  public static String getAverroesPlaceholderLib(String testCase, boolean selectHandWrittenModel) {
    String dir = selectHandWrittenModel ? "manual" : "averroes";
    return Paths.get(
            base, testCase, getProjectNamePrefix(testCase), "jars", dir, "placeholder-lib.jar")
        .toString();
  }

  /**
   * Get the path to the JRE that Averroes will generate a model for (not necessarily the same JRE
   * that the project depends on.
   *
   * @return
   */
  public static String getJreToModel() {
    return FileUtils.listFiles(
            Paths.get(jreToModel).toFile(),
            FileFilters.jreFileFilter,
            FileFilterUtils.trueFileFilter())
        .stream()
        .map(map -> map.getPath())
        .collect(Collectors.joining(File.pathSeparator));
  }

  /**
   * Get the path to the framework model for the library.
   *
   * @param testCase
   * @param analysis
   * @param selectHandWrittenModel
   * @return
   */
  public static String getFrameworkModel(
      String testCase, String analysis, boolean selectHandWrittenModel) {
    if (selectHandWrittenModel) {
      return Paths.get(
              base,
              testCase,
              getProjectNamePrefix(testCase),
              "jars",
              "manual",
              "placeholder-" + analysis + ".jar")
          .toString();
    } else {
      return Paths.get(
              base,
              testCase,
              getProjectNamePrefix(testCase),
              "jars",
              "averroes",
              "placeholder-fwk-" + analysis + ".jar")
          .toString();
    }
  }

  /**
   * Get the path to the application code. Again, this could as well be the convenient consolidated
   * JAR file that Averroes generates.
   *
   * @param testCase
   * @return
   */
  public static String getApplicationCode(String testCase) {
    return Paths.get(
            base, testCase, getProjectNamePrefix(testCase), "jars", "averroes", "organized-app.jar")
        .toString();
  }

  /**
   * Get the path to the input project (i.e., the project for the original library code).
   *
   * @param testCase
   * @return
   */
  public static String getInputProject(String testCase) {
    return Paths.get(base, testCase, getProjectNamePrefix(testCase) + ".input", "bin").toString();
  }

  /**
   * GEt the path to the output project (i.e., the project for the handwritten model).
   *
   * @param testCase
   * @param analysis
   * @return
   */
  public static String getOutputProject(String testCase, String analysis) {
    return Paths.get(base, testCase, getProjectNamePrefix(testCase) + ".output." + analysis, "bin")
        .toString();
  }

  /**
   * Find the Jimple files for the given test case.
   *
   * @param testCase
   * @param analysis
   * @param printerType
   * @return
   */
  public static Collection<File> findJimpleFiles(
      String testCase, String analysis, PrinterType printerType) {
    File root =
        Paths.get(
                jimple,
                getProjectNamePrefix(testCase)
                    + ".output."
                    + analysis
                    + "."
                    + printerType.toString().toLowerCase())
            .toFile();
    return FileUtils.listFiles(root, new String[] {jimple}, true);
  }

  /**
   * Find the JSON files for the given test case.
   *
   * @param testCase
   * @param analysis
   * @param printerType
   * @return
   */
  public static Collection<File> findJsonFiles(
      String testCase, String analysis, PrinterType printerType) {
    File root =
        Paths.get(
                json,
                getProjectNamePrefix(testCase)
                    + ".output."
                    + analysis
                    + "."
                    + printerType.toString().toLowerCase())
            .toFile();
    return FileUtils.listFiles(root, new String[] {json}, true);
  }

  /**
   * Get the common project prefix.
   *
   * @param testCase
   * @return
   */
  public static String getProjectNamePrefix(String testCase) {
    return "averroes.tests." + testCase.toLowerCase();
  }

  /**
   * Delete the directory that contains the Jimple files for the handwritten model.
   *
   * @param testCase
   * @param analysis
   */
  public static void deleteJimpleExpectedDirectory(String testCase, String analysis) {
    deleteExpectedDirectory(testCase, analysis, jimple);
  }

  /**
   * Delete the directory that contains the JSON files for the handwritten model.
   *
   * @param testCase
   * @param analysis
   */
  public static void deleteJsonExpectedDirectory(String testCase, String analysis) {
    deleteExpectedDirectory(testCase, analysis, json);
  }

  /**
   * Delete the directory that contains the expected files for the handwritten model.
   *
   * @param testCase
   * @param analysis
   * @param basedir
   */
  public static void deleteExpectedDirectory(String testCase, String analysis, String basedir) {
    FileUtils.deleteQuietly(
        Paths.get(
                basedir,
                getProjectNamePrefix(testCase)
                    + ".output."
                    + analysis
                    + "."
                    + PrinterType.EXPECTED.toString().toLowerCase())
            .toFile());
  }

  /**
   * Delete the directory that contains the Jimple files for the generated model.
   *
   * @param testCase
   * @param analysis
   */
  public static void deleteJimpleAnalysisDirectories(String testCase, String analysis) {
    deleteAnalysisDirectories(testCase, analysis, jimple);
  }

  /**
   * Delete the directory that contains the Jimple files for the generated model.
   *
   * @param testCase
   * @param analysis
   */
  public static void deleteJsonAnalysisDirectories(String testCase, String analysis) {
    deleteAnalysisDirectories(testCase, analysis, json);
  }

  /**
   * Delete the analysis directories that contain the files for the generated model.
   *
   * @param testCase
   * @param analysis
   * @param basedir
   */
  public static void deleteAnalysisDirectories(String testCase, String analysis, String basedir) {
    FileUtils.deleteQuietly(
        Paths.get(
                basedir,
                getProjectNamePrefix(testCase)
                    + ".input."
                    + PrinterType.ORIGINAL.toString().toLowerCase())
            .toFile());

    FileUtils.deleteQuietly(
        Paths.get(
                basedir,
                getProjectNamePrefix(testCase)
                    + ".output."
                    + analysis
                    + "."
                    + PrinterType.GENERATED.toString().toLowerCase())
            .toFile());

    FileUtils.deleteQuietly(
        Paths.get(
                basedir,
                getProjectNamePrefix(testCase)
                    + ".output."
                    + analysis
                    + "."
                    + PrinterType.OPTIMIZED.toString().toLowerCase())
            .toFile());
  }
}
