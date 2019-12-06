package averroes.tests.junit;

import averroes.exceptions.AssertionError;
import averroes.frameworks.analysis.RtaJimpleBody;
import averroes.frameworks.analysis.XtaJimpleBody;
import averroes.frameworks.options.FrameworksOptions;
import averroes.soot.Names;
import averroes.tests.CommonOptions;
import averroes.util.io.Printers.PrinterType;
import averroes.util.json.JsonUtils;
import averroes.util.json.SootClassJson;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

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
        cleanupFiles(testCase);
        compareJson();
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
        cleanupFiles(testCase);
        compareJson();
        System.out.println("======== Finished testing " + testCase + " XTA ========");
    }

    private static void cleanupFiles(String testCase) {
        cleanupFiles(
                testCase,
                averroes.util.io.Paths.findJsonFiles(PrinterType.GENERATED),
                averroes.util.io.Paths.jsonOutputDirectory(PrinterType.GENERATED));
        cleanupFiles(
                testCase,
                averroes.util.io.Paths.findJimpleFiles(PrinterType.GENERATED),
                averroes.util.io.Paths.jimpleOutputDirectory(PrinterType.GENERATED));
    }

    /**
     * Cleanup the generated Json files. This cleanup includes renaming files to use
     * "output.<analysis>" instead of "input." in the file name, as well in the file content.
     */
    private static void cleanupFiles(String testCase, Collection<File> files, Path dir) {
        files.stream()
                .forEach(
                        file -> {
                            try {
                                String content = FileUtils.readFileToString(file, Charset.defaultCharset());
                                String cleanContent = cleanString(testCase, content);
                                FileUtils.writeStringToFile(file, cleanContent, Charset.defaultCharset());

                                File cleanFile =
                                        Paths.get(dir.toString(), cleanString(testCase, file.getName())).toFile();

                                if (!cleanFile.getCanonicalPath().equalsIgnoreCase(file.getCanonicalPath())) {
                                    FileUtils.moveFile(file, cleanFile);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
    }

    /**
     * Clean the given string to enable successful Json comparisons.
     *
     * @param testCase
     * @param str
     * @return
     */
    private static String cleanString(String testCase, String str) {
        return str
                .replace(
                        "averroes.testsuite." + testCase.toLowerCase() + ".input",
                        "averroes.testsuite."
                                + testCase.toLowerCase()
                                + ".output."
                                + FrameworksOptions.getAnalysis())
                .replace(
                        FrameworksOptions.getAnalysis()
                                + "."
                                + FrameworksOptions.getAnalysis().toUpperCase()
                                + ".",
                        "averroes.testsuite."
                                + testCase.toLowerCase()
                                + ".output."
                                + FrameworksOptions.getAnalysis()
                                + "."
                                + FrameworksOptions.getAnalysis().toUpperCase()
                                + ".")
                .replace(
                        FrameworksOptions.getAnalysis()
                                + "."
                                + FrameworksOptions.getAnalysis().toUpperCase()
                                + ":",
                        "averroes.testsuite."
                                + testCase.toLowerCase()
                                + ".output."
                                + FrameworksOptions.getAnalysis()
                                + "."
                                + FrameworksOptions.getAnalysis().toUpperCase()
                                + ":");
    }

    /**
     * Perform content-based comparison between JSON objects that represent the generated and the
     * expected classes.
     */
    private static void compareJson() {
        Collection<File> generatedFiles = averroes.util.io.Paths.findJsonFiles(PrinterType.GENERATED);
        Collection<File> expectedFiles = averroes.util.io.Paths.findJsonFiles(PrinterType.EXPECTED);

        // // Assert number of generates vs expected classes
        // if (generatedFiles.size() != expectedFiles.size()) {
        // throw new AssertionError("Number of expected classes = " +
        // expectedFiles.size()
        // + " and generated classes = " + generatedFiles.size() + ". They
        // should match!");
        // }

        // Now diff each generated file with its corresponding expected one,
        // ignoring the xta.XTA and rta.RTA files for now
        expectedFiles.stream()
                .filter(f -> !f.getPath().endsWith(xtaJson) && !f.getPath().endsWith(rtaJson))
                .forEach(
                        expectedJsonFile -> {
                            File generatedJsonFile = generatedFiles.stream()
                                    .filter(f -> f.getName().equals(expectedJsonFile.getName()))
                                    .findFirst()
                                    .get();

                            try {
                                SootClassJson expectedSootClass = JsonUtils.fromJson(expectedJsonFile);
                                SootClassJson generatedSootClass = JsonUtils.fromJson(generatedJsonFile);

                                if (!expectedSootClass.isEquivalentTo(generatedSootClass)) {
                                    throw new AssertionError(
                                            "The contents of "
                                                    + expectedJsonFile.getCanonicalPath()
                                                    + " and "
                                                    + generatedJsonFile.getCanonicalPath()
                                                    + " do not match!");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
    }

    /**
     * Perform text-based comparisons between Jimple files.
     */
    private static void performAssertions() {
        Collection<File> generated = averroes.util.io.Paths.findJimpleFiles(PrinterType.GENERATED);
        Collection<File> expected = averroes.util.io.Paths.findJimpleFiles(PrinterType.EXPECTED);

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
                                    expected.stream()
                                            .filter(
                                                    f ->
                                                            f.getName()
                                                                    .equals(
                                                                            g.getName()
                                                                                    .replace(
                                                                                            "input",
                                                                                            "output." + FrameworksOptions.getAnalysis())))
                                            .findFirst()
                                            .get();
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
        ArrayList<String> args =
                new ArrayList<String>(
                        Arrays.asList(
                                "-i",
                                CommonOptions.getOutputProject(testCase, analysis),
                                "-p",
                                "averroes.testsuite." + testCase.toLowerCase() + ".output." + analysis,
                                "-o",
                                CommonOptions.getOutputDirectory(testCase),
                                "-j",
                                CommonOptions.jre,
                                "-a",
                                analysis));

        // Process the arguments. This is necessary because many common
        // options depend on some of those processed arguments.
        FrameworksOptions.processArguments(args.stream().toArray(String[]::new));

        averroes.frameworks.ExpectedOutputPrinter.main(args.stream().toArray(String[]::new));
    }

    private static void runAnalysis(String testCase, String analysis, boolean guard, boolean whole) {
        ArrayList<String> args =
                new ArrayList<String>(
                        Arrays.asList(
                                "-i",
                                CommonOptions.getInputProject(testCase),
                                "-p",
                                "averroes.testsuite." + testCase.toLowerCase() + ".input",
                                "-o",
                                CommonOptions.getOutputDirectory(testCase),
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

        // Process the arguments. This is necessary because many common
        // options depend on some of those processed arguments.
        FrameworksOptions.processArguments(args.stream().toArray(String[]::new));

        averroes.frameworks.Main.main(args.stream().toArray(String[]::new));
        // + File.pathSeparator + CommonOptions.getJreToModel()
    }
}
