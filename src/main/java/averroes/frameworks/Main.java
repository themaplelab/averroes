package averroes.frameworks;

import averroes.JarFile;
import averroes.frameworks.options.FrameworksOptions;
import averroes.frameworks.soot.ClassWriter;
import averroes.frameworks.soot.CodeGenerator;
import averroes.soot.SootSceneUtil;
import averroes.util.MathUtils;
import averroes.util.TimeUtils;
import averroes.util.io.Paths;
import averroes.util.io.Printers;
import org.apache.commons.io.FileUtils;
import soot.G;
import soot.Scene;
import soot.options.Options;

import java.io.IOException;

/**
 * The main Averroes class.
 *
 * @author Karim Ali
 */
public class Main {

    /**
     * The main Averroes method.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            // Start calculating the total execution time
            TimeUtils.splitStart();

            // Process the arguments
            FrameworksOptions.processArguments(args);

            // Reset Soot
            G.reset();

            // Create the output directory and clean up any class files in there
            averroes.util.io.Paths.deleteClassAnalysisDirectories();
            averroes.util.io.Paths.deleteJimpleAnalysisDirectories();
            averroes.util.io.Paths.deleteJsonAnalysisDirectories();
            FileUtils.forceMkdir(Paths.frameworksLibraryClassesOutputDirectory());

            // Set some soot parameters
            Options.v().classes().addAll(FrameworksOptions.getClasses());
            Options.v().set_soot_classpath(FrameworksOptions.getSootClassPath());
            Options.v().set_validate(true);
            if (FrameworksOptions.isIncludeDependencies()) {
                Options.v().set_whole_program(true); // to model lib dependencies
                Options.v().set_allow_phantom_refs(true); // to handle invokedynamic
            }

//            Options.v().setPhaseOption("jb.tr", "use-older-type-assigner:true");
//
//            Options.v().setPhaseOption("wjtp", "enabled");
//            PackManager.v().getPack("wjtp").add(new StaticInlineTransform("wjtp.si"));
//            Options.v().setPhaseOption("wjtp.si", "enabled");

            // Load the necessary classes
            TimeUtils.reset();
            System.out.println("Loading classes...");
            Scene.v().loadNecessaryClasses();
            double soot = TimeUtils.elapsedTime();
            System.out.println("Soot loaded the input classes in " + soot + " seconds.");

            // Add default constructors to all library classes
            SootSceneUtil.getClasses().forEach(CodeGenerator::createEmptyDefaultConstructor);

            // Now let Averroes do its thing
            TimeUtils.reset();
            System.out.println("Creating Jimple bodies for framework methods...");
            CodeGenerator.generateJimple();

            System.out.println("Writing JSON files for framework methods...");
            // Print out JSON files
            Printers.printGeneratedJson();

//            System.out.println("Optimizing generated library methods...");
//            new Optimizer().optimize();
//
//            System.out.println("Creating optimized Jimple bodies for library
//                    methods...");
//            Scene.v().getApplicationClasses().stream().map(c ->
//                    c.getMethods()).flatMap(List::stream)
//                    .filter(SootMethod::isConcrete)
//                    .forEach(m ->
//                            Printers.printJimple(Printers.PrinterType.OPTIMIZED, m));
//
//            Print out JSON files
//            System.out.println("Writing JSON files for optimized library
//                    methods...");
//            Scene.v().getApplicationClasses().forEach(c -> {
//                Printers.printJson(PrinterType.OPTIMIZED, c);
//            });

//            // Perform code cleanup
//            SootUtils.cleanupClasses();

            // Write class files for the generate model
            System.out.println("Writing class files for framework methods...");
            ClassWriter.writeLibraryClassFiles();
            double averroes = TimeUtils.elapsedTime();
            System.out.println("Placeholder framework classes created and Jimple validated in " + averroes + " seconds.");

            // Create the jar file, add all the generated class files to it, and, finally, verify it using BCEL.
            TimeUtils.reset();
            JarFile frameworkJarFile = new JarFile(Paths.placeholderFrameworkJarFile());
            frameworkJarFile.addGeneratedFrameworkClassFiles();
            frameworkJarFile.verify();

            double bcel = TimeUtils.elapsedTime();
            System.out.println("Placeholder framework JAR file verified in " + bcel + " seconds.");
            System.out.println("Total time (without verification) is " + MathUtils.round(soot + averroes) + " seconds.");
            System.out.println("Total time (with verification) is " + MathUtils.round(soot + averroes + bcel) + " seconds.");

            double total = TimeUtils.elapsedSplitTime();
            System.out.println("Elapsed time: " + total + " seconds.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
