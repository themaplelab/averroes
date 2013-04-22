package ca.uwaterloo.averroes.callgraph;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import ca.uwaterloo.averroes.callgraph.gxl.GXLReader;

/**
 * A class that prints out call graph information on the standard output.
 * 
 * @author karim
 * 
 */
public class CallGraphInfo {

	private static boolean dashE = false;
	private static boolean dashA2A = false;
	private static boolean dashA2L = false;
	private static boolean dashL2A = false;
	private static String filename = null;

	/**
	 * The method expect at least two parameters, one for the graph GXL file. The other parameters represent which print
	 * options should be used.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		parseParams(args);

		CallGraph graph = null;
		try {
			try {
				graph = new GXLReader().readCallGraph(new FileInputStream(filename), CallGraphSource.DUMMY);
			} catch (RuntimeException e) {
				graph = new GXLReader().readCallGraph(new GZIPInputStream(new FileInputStream(filename)),
						CallGraphSource.DUMMY);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		CallGraphPrintOptions options = new CallGraphPrintOptions();
		options.setPrintEntryPoints(dashE);
		options.setPrintAppToAppEdges(dashA2A);
		options.setPrintAppToLibEdges(dashA2L);
		options.setPrintLibToAppEdges(dashL2A);
		graph.print(options);
	}

	private static void parseParams(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-e")) {
				dashE = true;
			} else if (args[i].equals("-a2a")) {
				dashA2A = true;
			} else if (args[i].equals("-a2l")) {
				dashA2L = true;
			} else if (args[i].equals("-l2a")) {
				dashL2A = true;
			} else if (filename == null) {
				filename = args[i];
			} else {
				usage();
			}
		}

		if (filename == null) {
			usage();
		}
	}

	private static void usage() {
		System.out.println("Usage: java -jar cginfo.jar [options] graph.gxl");
		System.out.println("  -e : print entry points");
		System.out.println("  -a2a : print application to application edges");
		System.out.println("  -a2l : print application to library edges");
		System.out.println("  -l2a : print library to application edges");
		System.exit(1);
	}
}