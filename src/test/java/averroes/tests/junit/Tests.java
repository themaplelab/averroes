package averroes.tests.junit;

import averroes.exceptions.AssertionError;
import averroes.frameworks.analysis.RtaJimpleBody;
import averroes.frameworks.analysis.XtaJimpleBody;
import averroes.soot.Names;
import averroes.tests.CommonOptions;
import averroes.util.io.Printers.PrinterType;
import averroes.util.json.JsonUtils;
import averroes.util.json.SootClassJson;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.io.FileUtils;

public class Tests {
  private static final String xtaJimple = Names.XTA_CLASS.replace('.', '/') + ".jimple";
  private static final String rtaJimple = Names.RTA_CLASS.replace('.', '/') + ".jimple";
  private static final String xtaJson = Names.XTA_CLASS.replace('.', '/') + ".json";
  private static final String rtaJson = Names.RTA_CLASS.replace('.', '/') + ".json";

  public static void runRta(String testCase) {
    runRta(testCase, false);
  }

  public static void runRta(String testCase, boolean guard) {
    runRta(testCase, guard, false);
  }

  public static void runRta(String testCase, boolean guard, boolean whole) {
    System.out.println("======== Started testing " + testCase + " RTA ========");
    runExpectedOutputPrinter(testCase, RtaJimpleBody.name);
    runAnalysis(testCase, RtaJimpleBody.name, guard, whole);
    compareJson(testCase, RtaJimpleBody.name);
    System.out.println("======== Finished testing " + testCase + " RTA ========");
  }

  public static void runXta(String testCase) {
    runXta(testCase, false);
  }

  public static void runXta(String testCase, boolean guard) {
    runXta(testCase, guard, false);
  }

  public static void runXta(String testCase, boolean guard, boolean whole) {
    System.out.println("======== Started testing " + testCase + " XTA ========");
    runExpectedOutputPrinter(testCase, XtaJimpleBody.name);
    runAnalysis(testCase, XtaJimpleBody.name, guard, whole);
    compareJson(testCase, XtaJimpleBody.name);
    System.out.println("======== Finished testing " + testCase + " XTA ========");
  }

  /**
   * Perform content-based comparison between JSON objects that represent the generated and the
   * expected classes.
   *
   * @param testCase
   * @param analysis
   */
  public static void compareJson(String testCase, String analysis) {
    Collection<File> generatedFiles =
        CommonOptions.findJsonFiles(testCase, analysis, PrinterType.GENERATED);
    Collection<File> expectedFiles =
        CommonOptions.findJsonFiles(testCase, analysis, PrinterType.EXPECTED);

    // // Assert number of generates vs expected classes
    // if (generatedFiles.size() != expectedFiles.size()) {
    // throw new AssertionError("Number of expected classes = " +
    // expectedFiles.size()
    // + " and generated classes = " + generatedFiles.size() + ". They
    // should match!");
    // }

    // Now diff each generated file with its corresponding expected one,
    // ignoring the xta.XTA and rta.RTA files for now
    generatedFiles.stream()
        .filter(f -> !f.getPath().endsWith(xtaJson) && !f.getPath().endsWith(rtaJson))
        .forEach(
            generatedJsonFile -> {
              File expectedJsonFile =
                  expectedFiles.stream()
                      .filter(f -> f.getName().equals(generatedJsonFile.getName()))
                      .findFirst()
                      .get();

              try {
                SootClassJson generatedSootClass = JsonUtils.fromJson(generatedJsonFile);
                SootClassJson expectedSootClass = JsonUtils.fromJson(expectedJsonFile);

                if (!generatedSootClass.isEquivalentTo(expectedSootClass)) {
                  throw new AssertionError(
                      "The contents of "
                          + generatedJsonFile.getName()
                          + " and "
                          + expectedJsonFile.getName()
                          + " do not match!");
                }
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
  }

  /**
   * Perform text-based comparisons between Jimple files.
   *
   * @param testCase
   * @param analysis
   */
  public static void performAssertions(String testCase, String analysis) {
    Collection<File> generated =
        CommonOptions.findJimpleFiles(testCase, analysis, PrinterType.GENERATED);
    Collection<File> expected =
        CommonOptions.findJimpleFiles(testCase, analysis, PrinterType.EXPECTED);

    // Assert number of generates vs expected classes
    if (generated.size() != expected.size()) {
      throw new AssertionError(
          "Number of expected classes = "
              + expected.size()
              + " and generated classes = "
              + generated.size()
              + ". They should match!");
    }

    // Now diff each generated file with its corresponding expected one,
    // ignoring the xta.XTA and rta.RTA files for now
    generated.stream()
        .filter(f -> !f.getPath().endsWith(xtaJimple) && !f.getPath().endsWith(rtaJimple))
        .forEach(
            g -> {
              File e =
                  expected.stream().filter(f -> f.getName().equals(g.getName())).findFirst().get();
              try {
                if (!FileUtils.contentEquals(g, e)) {
                  throw new AssertionError(
                      "The contents of " + g.getName() + " and " + e.getName() + " do not match!");
                }
              } catch (IOException ex) {
                ex.printStackTrace();
              }
            });
  }

  private static void runExpectedOutputPrinter(String testCase, String analysis) {
    CommonOptions.deleteJimpleExpectedDirectory(testCase, analysis);
    CommonOptions.deleteJsonExpectedDirectory(testCase, analysis);
    averroes.frameworks.ExpectedOutputPrinter.main(
        new String[] {
          "-i",
          CommonOptions.getOutputProject(testCase, analysis),
          "-o",
          CommonOptions.output,
          "-j",
          CommonOptions.jre,
          "-a",
          analysis
        });
  }

  private static void runAnalysis(String testCase, String analysis, boolean guard, boolean whole) {
    CommonOptions.deleteJimpleAnalysisDirectories(testCase, analysis);
    CommonOptions.deleteJsonAnalysisDirectories(testCase, analysis);

    ArrayList<String> args =
        new ArrayList<String>(
            Arrays.asList(
                "-i",
                CommonOptions.getInputProject(testCase),
                "-o",
                CommonOptions.output,
                "-j",
                CommonOptions.jre,
                "-a",
                analysis));

    if (guard) {
      args.add("-g");
    }

    if (whole) {
      args.add("-w");
    }

    averroes.frameworks.Main.main(args.toArray(new String[0]));
    // + File.pathSeparator + CommonOptions.getJreToModel()
  }
}
