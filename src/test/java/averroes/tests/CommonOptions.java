package averroes.tests;

import averroes.util.io.FileFilters;
import java.io.File;
import java.nio.file.Paths;
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

  public static boolean guard = false;
  public static boolean whole = false;

  public static String base =
      Paths.get("build", "classes", "java", "test", "averroes", "testsuite").toString();

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
    return Paths.get(getOutputDirectory(testCase), "jars", "averroes", "organized-lib.jar")
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
            Paths.get(getOutputDirectory(testCase), "jars", dir).toFile(),
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
    return Paths.get(getOutputDirectory(testCase), "jars", dir, "averroes-lib-class.jar")
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
    return Paths.get(getOutputDirectory(testCase), "jars", dir, "placeholder-lib.jar").toString();
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
              getOutputDirectory(testCase), "jars", "manual", "placeholder-" + analysis + ".jar")
          .toString();
    } else {
      return Paths.get(
              getOutputDirectory(testCase),
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
    return Paths.get(getOutputDirectory(testCase), "jars", "averroes", "organized-app.jar")
        .toString();
  }

  /**
   * Get the path to the input project (i.e., the project for the original library code).
   *
   * @param testCase
   * @return
   */
  public static String getInputProject(String testCase) {
    return Paths.get(base, testCase.toLowerCase(), "input").toString();
  }

  /**
   * GEt the path to the output project (i.e., the project for the handwritten model).
   *
   * @param testCase
   * @param analysis
   * @return
   */
  public static String getOutputProject(String testCase, String analysis) {
    return Paths.get(base, testCase.toLowerCase(), "output", analysis).toString();
  }

  /**
   * Get the output directory for the given test case.
   *
   * @param testCase
   * @return
   */
  public static String getOutputDirectory(String testCase) {
    return Paths.get("output", testCase.toLowerCase()).toString();
  }
}
