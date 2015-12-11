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

/**
 * Utility class for printing-related operations.
 * 
 * @author Karim Ali
 * 
 */
public class Printers {

	public enum PrinterType {
		EXPECTED, BEFORE, AFTER;
	}

	public static PrintStream getPrintStream(PrinterType printerType) {
		PrintStream result = null;
		try {
			result = new PrintStream(new FileOutputStream(
					Paths.rtaDebugFile(printerType), true), true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return result;
	}

}
