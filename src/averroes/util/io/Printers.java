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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import soot.Body;
import soot.SootMethod;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.scalar.NopEliminator;

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

	private static PrintStream getPrintStream(PrinterType printerType,
			SootMethod method) {
		PrintStream result = null;

		try {
			result = new PrintStream(new FileOutputStream(Paths.jimpleDumpFile(
					printerType, method), true), true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return result;
	}

	public static void print(PrinterType printerType, SootMethod method) {
		if (printerType == PrinterType.EXPECTED) {
			cleanup(method.retrieveActiveBody());
		}

		getPrintStream(printerType, method).println(method.getSignature());
		getPrintStream(printerType, method)
				.println(method.retrieveActiveBody());
		getPrintStream(printerType, method).println();
		getPrintStream(printerType, method).println();
	}

	// Apply the same cleanups we do in AbstractJimpleBody
	private static void cleanup(Body body) {
//		UnusedLocalEliminator.v().transform(body);
		LocalNameStandardizer.v().transform(body);
		NopEliminator.v().transform(body);
	}

}
