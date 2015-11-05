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

import averroes.options.AverroesOptions;

/**
 * Utility class for file-related operations.
 * 
 * @author Karim Ali
 * 
 */
public class Paths {

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
