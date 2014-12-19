package ca.uwaterloo.averroes.callgraph.drivers;

import ca.uwaterloo.averroes.callgraph.CallGraph;
import ca.uwaterloo.averroes.callgraph.CallGraphFactory;
import ca.uwaterloo.averroes.callgraph.gxl.GXLWriter;
import ca.uwaterloo.averroes.exceptions.AverroesException;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.util.TimeUtils;
import ca.uwaterloo.averroes.util.io.FileUtils;

/**
 * A driver class that generates call graph for WALA using the original library.
 * 
 * @author karim
 * 
 */
public class WalaCallGraphGenerator {

	public static void main(String[] args) {
		try {
			// Generate the call graph
			TimeUtils.reset();
			if (args.length != 1) {
				usage();
				throw new AverroesException("Wala expects exactly 1 argument.");
			}

			// Process the arguments
			String benchmark = args[0];
			
			FileUtils.createDirectory(AverroesProperties.getOutputDir());
			CallGraph wala = CallGraphFactory.generateWalaCallGraph(benchmark);
			System.out.println("Total time to finish: " + TimeUtils.elapsedTime());
			new GXLWriter().write(wala, FileUtils.walaCallGraphFile());

			// Print some statistics
			System.out.println("=================================================");
			System.out.println("# edges = " + wala.size());
			System.out.println("=================================================");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void usage() {
		System.out.println("");
		System.out.println("Usage: java -jar wala.jar");
		System.out.println("");
	}
}
