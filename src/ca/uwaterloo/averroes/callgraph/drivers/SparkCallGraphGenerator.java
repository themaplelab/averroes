package ca.uwaterloo.averroes.callgraph.drivers;

import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;

import probe.CallGraph;
import probe.TextWriter;
import ca.uwaterloo.averroes.callgraph.CallGraphFactory;
import ca.uwaterloo.averroes.exceptions.AverroesException;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.util.ProbeUtils;
import ca.uwaterloo.averroes.util.TimeUtils;
import ca.uwaterloo.averroes.util.io.FileUtils;

/**
 * A driver class that generates call graph for Spark using the original
 * library.
 * 
 * @author karim
 * 
 */
public class SparkCallGraphGenerator {

	public static void main(String[] args) {
		try {
			// Generate the call graph
			TimeUtils.reset();
			if (args.length != 2) {
				usage();
				throw new AverroesException("SparkAverroes expects exactly 2 arguments.");
			}

			// Process the arguments
			String base = args[0];
			String benchmark = args[1];

			FileUtils.createDirectory(AverroesProperties.getOutputDir());
			CallGraph spark = CallGraphFactory.generateSparkCallGraph(base, benchmark, false);
			System.out.println("Total time to finish: " + TimeUtils.elapsedTime());

			// collapse and write the call graph
			probe.CallGraph collapsed = ProbeUtils.collapse(spark);
			new TextWriter().write(collapsed,
					new GZIPOutputStream(new FileOutputStream(FileUtils.sparkCallGraphGzipFile())));

			// Print some statistics
			System.out.println("=================================================");
			System.out.println("# edges = " + collapsed.edges().size());
			System.out.println("=================================================");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void usage() {
		System.out.println("");
		System.out.println("Usage: java -jar spark.jar");
		System.out.println("");
	}
}
