/**
 * ***************************************************************************** Copyright (c) 2015
 * Karim Ali and Ondřej Lhoták. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Karim Ali - initial API and implementation and/or initial documentation
 * *****************************************************************************
 */
package averroes;

import averroes.options.AverroesOptions;
import averroes.soot.CodeGenerator;
import averroes.soot.Hierarchy;
import averroes.soot.JarFactoryClassProvider;
import averroes.soot.SootSceneUtil;
import averroes.util.MathUtils;
import averroes.util.TimeUtils;
import averroes.util.io.Paths;
import org.apache.commons.io.FileUtils;
import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.SourceLocator;
import soot.options.Options;

import java.io.File;
import java.util.Collections;

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
            // Find the total execution time, instead of depending on the Unix
            // time command
            TimeUtils.splitStart();

            // Process the arguments
            AverroesOptions.processArguments(args);

            // Reset Soot
            G.reset();

            // Create the output directory and clean up any class files in there
            FileUtils.forceMkdir(Paths.libraryClassesOutputDirectory());
            FileUtils.cleanDirectory(Paths.classesOutputDirectory());

            // Organize the input JAR files
            System.out.println();
            System.out.println("Organizing the JAR files...");
            JarOrganizer jarOrganizer = new JarOrganizer();
            jarOrganizer.organizeInputJarFiles();

            // Print some statistics
            System.out.println("# application classes: " + jarOrganizer.applicationClassNames().size());
            System.out.println("# library classes: " + jarOrganizer.libraryClassNames().size());

            // Add the organized archives for the application and its
            // dependencies.
            TimeUtils.reset();

            // Set some specific options if we are using the ASM frontend
            if (AverroesOptions.isUseASM()) {
                Options.v().set_soot_classpath(Paths.organizedApplicationJarFile().getAbsolutePath() +
                        File.pathSeparator +
                        Paths.organizedLibraryJarFile());

                Scene.v().addBasicClass("java.io.UnixFileSystem");
                Scene.v().addBasicClass("java.io.WinNTFileSystem");
                Scene.v().addBasicClass("java.io.Win32FileSystem");

                /* java.net.URL loads handlers dynamically */
                Scene.v().addBasicClass("sun.net.www.protocol.file.Handler");
                Scene.v().addBasicClass("sun.net.www.protocol.ftp.Handler");
                Scene.v().addBasicClass("sun.net.www.protocol.http.Handler");
                Scene.v().addBasicClass("sun.net.www.protocol.https.Handler");
                Scene.v().addBasicClass("sun.net.www.protocol.jar.Handler");
            } else {
                JarFactoryClassProvider provider = new JarFactoryClassProvider();
                provider.prepareJarFactoryClasspath();

                // Set some soot parameters
                SourceLocator.v().setClassProviders(Collections.singletonList(provider));
                SootSceneUtil.addCommonDynamicClasses(provider);
            }

            Options.v().set_main_class(AverroesOptions.getMainClass());
            Options.v().set_validate(true);
            Options.v().set_whole_program(true);

            // If we are using 1.8 or later, need to handle invokedynamic
            if (AverroesOptions.getJreVersion() > 1.7) {
                Options.v().set_allow_phantom_refs(true);
                // Not sure why this required for it to work with 1.8, but not for 1.6:
                Options.v().classes().addAll(SourceLocator.v().getClassesUnder(Paths.organizedApplicationJarFile().getAbsolutePath()));
            }

            // Load the necessary classes
            System.out.println();
            System.out.println("Loading classes...");
            AverroesOptions.getDynamicApplicationClasses().forEach(Scene.v()::addBasicClass);
            Scene.v().loadNecessaryClasses();
            Scene.v().forceResolve(AverroesOptions.getMainClass(), SootClass.BODIES);
            Scene.v().setMainClassFromOptions();
            double soot = TimeUtils.elapsedTime();
            System.out.println("Soot loaded the input classes in " + soot + " seconds.");

            // Now let Averroes do its thing
            // First, create the class hierarchy
            TimeUtils.reset();
            System.out.println();
            System.out.println("Creating the class hierarchy for the placeholder library...");
            Hierarchy.v();

            // Output some initial statistics
            System.out.println(
                    "# initial application classes: " + Hierarchy.v().getApplicationClasses().size());
            System.out.println("# initial library classes: " + Hierarchy.v().getLibraryClasses().size());
            System.out.println("# initial library methods: " + Hierarchy.v().getLibraryMethodCount());
            System.out.println("# initial library fields: " + Hierarchy.v().getLibraryFieldCount());
            System.out.println(
                    "# referenced library methods: " + Hierarchy.v().getReferencedLibraryMethodCount());
            System.out.println(
                    "# referenced library fields: " + Hierarchy.v().getReferencedLibraryFieldCount());

            // Cleanup the hierarchy
            System.out.println();
            System.out.println("Cleaning up the class hierarchy...");
            Hierarchy.v().cleanupLibraryClasses();

            // Output some cleanup statistics
            System.out.println(
                    "# removed library methods: " + Hierarchy.v().getRemovedLibraryMethodCount());
            System.out.println(
                    "# removed library fields: " + Hierarchy.v().getRemovedLibraryFieldCount());
            // The +1 is for Finalizer.register that will be added later
            System.out.println("# final library methods: " + (Hierarchy.v().getLibraryMethodCount() + 1));
            System.out.println("# final library fields: " + Hierarchy.v().getLibraryFieldCount());

            // Output some code generation statistics
            System.out.println();
            System.out.println("Generating extra library classes...");
            System.out.println(
                    "# generated library classes: " + CodeGenerator.v().getGeneratedClassCount());
            System.out.println(
                    "# generated library methods: " + CodeGenerator.v().getGeneratedMethodCount());

            // Create the Averroes library class
            System.out.println();
            System.out.println("Creating the skeleton for Averroes's main library class...");
            CodeGenerator.v().createAverroesLibraryClass();

            // Create method bodies to the library classes
            System.out.println("Generating the method bodies for the placeholder library classes ...");
            CodeGenerator.v().createLibraryMethodBodies();

            // Create empty classes for the basic classes required internally by
            // Soot
            System.out.println("Generating empty basic library classes required by Soot...");
            for (SootClass basicClass :
                    Hierarchy.v().getBasicClassesDatabase().getMissingBasicClasses()) {
                CodeGenerator.writeLibraryClassFile(basicClass);
            }
            double averroes = TimeUtils.elapsedTime();
            System.out.println(
                    "Placeholder library classes created and validated in " + averroes + " seconds.");

            // Create the jar file and add all the generated class files to it.
            TimeUtils.reset();
            JarFile librJarFile = new JarFile(Paths.placeholderLibraryJarFile());
            librJarFile.addGeneratedLibraryClassFiles();
            JarFile aveJarFile = new JarFile(Paths.averroesLibraryClassJarFile());
            aveJarFile.addAverroesLibraryClassFile();
            double bcel = TimeUtils.elapsedTime();
            System.out.println("Placeholder library JAR file verified in " + bcel + " seconds.");
            System.out.println(
                    "Total time (without verification) is " + MathUtils.round(soot + averroes) + " seconds.");
            System.out.println(
                    "Total time (with verification) is "
                            + MathUtils.round(soot + averroes + bcel)
                            + " seconds.");

            double total = TimeUtils.elapsedSplitTime();
            System.out.println("Elapsed time: " + total + " seconds.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void usage() {
        System.out.println();
        System.out.println("Usage: java -jar averroes.jar [options]");
        System.out.println("  -tfx : enable Tamiflex support");
        System.out.println("  -dyn : enable dynamic classes support");
        System.out.println();
        System.exit(1);
    }
}
