/**
 * ***************************************************************************** Copyright (c) 2015
 * Karim Ali and Ondřej Lhoták. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Karim Ali - initial API and implementation and/or initial documentation
 * *****************************************************************************
 */
/**
 *
 */
package averroes.util.io;

import averroes.soot.SootSceneUtil;
import averroes.util.SootUtils;
import averroes.util.json.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import soot.SootClass;
import soot.SootMethod;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * Utility class for printing-related operations.
 *
 * @author Karim Ali
 */
public class Printers {

    // The main Gson printer
    private static Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

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

        File jimpleFile = Paths.jimpleOutputFile(printerType, method);
        try {
            FileUtils.writeLines(jimpleFile, Collections.singleton(method.getSignature()), true);
            FileUtils.writeLines(
                    jimpleFile, Collections.singleton(method.retrieveActiveBody().toString()), true);
            FileUtils.writeLines(jimpleFile, null, true);
            FileUtils.writeLines(jimpleFile, null, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print out the JSON representation of the given Soot class.
     *
     * @param printerType
     * @param cls
     */
    public static void printJson(PrinterType printerType, SootClass cls) {
        File jsonFile = Paths.jsonOutputFile(printerType, cls);
        String json = gson.toJson(JsonUtils.toJson(cls));
        try {
            FileUtils.writeLines(jsonFile, Collections.singleton(json));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print out the JSON representation of all the generated Soot classes.
     */
    public static void printGeneratedJson() {
        SootSceneUtil.getClasses().forEach(Printers::printGeneratedJson);
    }

    /**
     * Print out the JSON representation of all the given generated Soot classes.
     */

    public static void printGeneratedJson(SootClass cls) {
        printJson(PrinterType.GENERATED, cls);
    }

    /**
     * Log the given information from the inliner.
     *
     * @param message
     * @param method
     */
    public static void logInliningInfo(String message, SootMethod method) {
        logInliningInfo(message, method.getDeclaringClass());
    }

    /**
     * Log the given information from the inliner.
     *
     * @param message
     * @param cls
     */
    public static void logInliningInfo(String message, SootClass cls) {
        File inlinerFile = Paths.inlinerOutputFile(cls);
        try {
            FileUtils.writeLines(inlinerFile, Collections.singleton(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public enum PrinterType {
        EXPECTED,
        ORIGINAL,
        GENERATED,
        OPTIMIZED
    }
}
