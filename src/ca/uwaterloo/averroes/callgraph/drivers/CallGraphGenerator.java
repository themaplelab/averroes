package ca.uwaterloo.averroes.callgraph.drivers;

import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;

import probe.CallGraph;
import probe.TextWriter;
import ca.uwaterloo.averroes.callgraph.CallGraphFactory;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.util.ProbeUtils;
import ca.uwaterloo.averroes.util.TimeUtils;
import ca.uwaterloo.averroes.util.io.FileUtils;

/**
 * A driver class that generates a call graph for the given tool.
 * 
 * @author karim
 * 
 */
public class CallGraphGenerator {

	public static void main(String[] args) {
		try {
			TimeUtils.reset();
			if (args.length != 5) {
				usage();
			}

			// Process the arguments
			String doopHome = args[0];
			String tool = args[1];
			String base = args[2];
			String benchmark = args[3];
			boolean isAverroes = Boolean.parseBoolean(args[4]);

			FileUtils.createDirectory(AverroesProperties.getOutputDir());
			CallGraph probecg = null;

			if (tool.equalsIgnoreCase("spark")) {
				probecg = CallGraphFactory.generateSparkCallGraph(base, benchmark, isAverroes);
			} else if (tool.equalsIgnoreCase("doop")) {
				probecg = CallGraphFactory.generateDoopCallGraph(doopHome, base, benchmark, isAverroes);
			} else if (tool.equalsIgnoreCase("wala")) {
				probecg = CallGraphFactory.generateWalaCallGraph(base, benchmark, isAverroes);
			} else {
				throw new IllegalStateException(
						tool
								+ " is unknow. Please provide one of the following tool names: spark, doop, or wala (case-insensitive)");
			}
			System.out.println("Total time to finish: " + TimeUtils.elapsedTime());

			// collapse and write the call graph
			CallGraph cg = ProbeUtils.collapse(probecg);
			new TextWriter().write(cg, new GZIPOutputStream(new FileOutputStream(FileUtils.callGraphGzipFile())));

			// Print some statistics
			System.out.println("=================================================");
			System.out.println("# edges = " + cg.edges().size());
			System.out.println("=================================================");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void usage() {
		System.out.println("");
		System.out.println("Usage: java -jar tool.jar <absolute_path_to_doop_home> <tool_name> <base> <benchmark> <isAverroes>");
		System.out.println("");
		System.exit(1);
	}
}