/*******************************************************************************
 * Copyright (c) 2015 Karim Ali and Ondřej Lhoták.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Karim Ali - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * 
 */
package averroes.util.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import averroes.frameworks.options.FrameworksOptions;
import averroes.options.AverroesOptions;
import averroes.util.io.Printers.PrinterType;
import soot.SootClass;
import soot.SootMethod;

/**
 * Utility class for file-related operations.
 * 
 * @author Karim Ali
 * 
 */
public class Paths {
	/**
	 * The path to the placeholder library class files.
	 * 
	 * @return
	 */
	public static File framewokrsLibraryClassesOutputDirectory() {
		return java.nio.file.Paths.get(FrameworksOptions.getOutputDirectory(), "classes", "lib").toFile();
	}
	
	/**
	 * The path to the frameworks placeholder library JAR file.
	 * 
	 * @return
	 */
	public static File placeholderFrameworkJarFile() {
		return java.nio.file.Paths.get(FrameworksOptions.getOutputDirectory(), "placeholder-fwk.jar").toFile();
	}

	/**
	 * The path to the output file where we dump Jimple code for before, after,
	 * and expected output.
	 * 
	 * @param printerType
	 * @param method
	 * @return
	 */
	public static File jimpleDumpFile(PrinterType printerType, SootMethod method) {
		String pkg = method.getDeclaringClass().getPackageName().replace(".", File.pathSeparator);
		String file = method.getDeclaringClass().getShortName() + ".jimple";
		String input = FrameworksOptions.getInputs().get(0);
		String project = input.replace(File.separator + "bin", "." + printerType.toString().toLowerCase());

		/*
		 * Separating output based on the project. For example: input + original
		 * => input.original input + generated => output.rta.generated output +
		 * expected => output.rta.expected output + optimized =>
		 * output.rta.optimized
		 */
		if (printerType != PrinterType.ORIGINAL) {
			project = project.replace("input", "output." + FrameworksOptions.getAnalysis());
		}

		Path prefix = java.nio.file.Paths.get(FrameworksOptions.getOutputDirectory()).toAbsolutePath().getParent()
				.getParent().resolve(java.nio.file.Paths.get("averroes.tests", "jimple"));

		Path dir = java.nio.file.Paths.get(project).getFileName();

		try {
			FileUtils.forceMkdir(prefix.resolve(dir).resolve(pkg).toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return prefix.resolve(dir).resolve(pkg).resolve(file).toFile();
	}

	/**
	 * The path to the output file where we dump inlining information.
	 * 
	 * @param cls
	 * @return
	 */
	public static File inlinerDumpFile(SootClass cls) {
		String pkg = cls.getPackageName().replace(".", File.pathSeparator);
		String file = cls.getShortName() + "-inliner.txt";
		String input = FrameworksOptions.getInputs().get(0);
		String project = input.replace(File.separator + "bin", "." + PrinterType.OPTIMIZED.toString().toLowerCase())
				.replace("input", "output." + FrameworksOptions.getAnalysis());

		Path prefix = java.nio.file.Paths.get(FrameworksOptions.getOutputDirectory()).toAbsolutePath().getParent()
				.getParent().resolve(java.nio.file.Paths.get("averroes.tests", "jimple"));

		Path dir = java.nio.file.Paths.get(project).getFileName();

		try {
			FileUtils.forceMkdir(prefix.resolve(dir).resolve(pkg).toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return prefix.resolve(dir).resolve(pkg).resolve(file).toFile();
	}

	/**
	 * The path to the debug file where we dump Jimple code for before, after,
	 * and expected output.
	 * 
	 * @param printerType
	 * @param cls
	 * @return
	 */
	public static File jsonDumpFile(PrinterType printerType, SootClass cls) {
		String pkg = cls.getPackageName().replace(".", File.pathSeparator);
		String file = cls.getShortName() + ".json";
		String input = FrameworksOptions.getInputs().get(0);
		String project = input.replace(File.separator + "bin", "." + printerType.toString().toLowerCase());

		/*
		 * Separating output based on the project. For example: input + original
		 * => input.original input + generated => output.rta.generated output +
		 * expected => output.rta.expected output + optimized =>
		 * output.rta.optimized
		 */
		if (printerType != PrinterType.ORIGINAL) {
			project = project.replace("input", "output." + FrameworksOptions.getAnalysis());
		}

		Path prefix = java.nio.file.Paths.get(FrameworksOptions.getOutputDirectory()).toAbsolutePath().getParent()
				.getParent().resolve(java.nio.file.Paths.get("averroes.tests", "json"));

		Path dir = java.nio.file.Paths.get(project).getFileName();

		try {
			FileUtils.forceMkdir(prefix.resolve(dir).resolve(pkg).toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return prefix.resolve(dir).resolve(pkg).resolve(file).toFile();
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
}
