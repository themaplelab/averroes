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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import averroes.util.SootUtils;
import averroes.util.json.JsonUtils;
import soot.SootClass;
import soot.SootMethod;

/**
 * Utility class for printing-related operations.
 * 
 * @author Karim Ali
 * 
 */
public class Printers {

	public enum PrinterType {
		EXPECTED, ORIGINAL, GENERATED, OPTIMIZED;
	}

	// The main Gson printer
	private static Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

	/**
	 * Get the Jimple output file that corresponds to the given Soot method.
	 * 
	 * @param printerType
	 * @param method
	 * @return
	 */
	private static PrintStream getPrintStream(PrinterType printerType, SootMethod method) {
		PrintStream result = null;

		try {
			File f = Paths.jimpleDumpFile(printerType, method);
			result = new PrintStream(new FileOutputStream(f, true), true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Get the JSON output file that corresponds to the given Soot class.
	 * 
	 * @param printerType
	 * @param cls
	 * @return
	 */
	private static PrintStream getJsonWriter(PrinterType printerType, SootClass cls) {
		PrintStream result = null;

		try {
			result = new PrintStream(new FileOutputStream(Paths.jsonDumpFile(printerType, cls)), true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Get the output stream used to print the inliner info for the given Soot
	 * class.
	 * 
	 * @param cls
	 * @return
	 */
	private static PrintStream getInlinerPrintStream(SootClass cls) {
		PrintStream result = null;

		try {
			result = new PrintStream(new FileOutputStream(Paths.inlinerDumpFile(cls), true), true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Print out the Jimple representation of the given Soot method.
	 * 
	 * @param printerType
	 * @param method
	 */
	public static void printJimple(PrinterType printerType, SootMethod method) {
		if (printerType == PrinterType.EXPECTED) {
			SootUtils.cleanup(method.retrieveActiveBody());
		}

		getPrintStream(printerType, method).println(method.getSignature());
		getPrintStream(printerType, method).println(method.retrieveActiveBody());
		getPrintStream(printerType, method).println();
		getPrintStream(printerType, method).println();
	}

	/**
	 * Print out the JSON representation of the given Soot class.
	 * 
	 * @param printerType
	 * @param cls
	 */
	public static void printJson(PrinterType printerType, SootClass cls) {
		getJsonWriter(printerType, cls).print(gson.toJson(JsonUtils.toJson(cls)));
	}

	/**
	 * Log the given information from the inliner.
	 * 
	 * @param message
	 * @param method
	 */
	public static void logInliningInfo(String message, SootMethod method) {
		getInlinerPrintStream(method.getDeclaringClass()).println(message);
	}

	/**
	 * Log the given information from the inliner.
	 * 
	 * @param message
	 * @param cls
	 */
	public static void logInliningInfo(String message, SootClass cls) {
		getInlinerPrintStream(cls).println(message);
	}
}
