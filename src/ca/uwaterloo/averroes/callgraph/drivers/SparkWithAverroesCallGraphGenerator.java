package ca.uwaterloo.averroes.callgraph.drivers;

import ca.uwaterloo.averroes.callgraph.CallGraph;
import ca.uwaterloo.averroes.callgraph.CallGraphFactory;
import ca.uwaterloo.averroes.callgraph.gxl.GXLWriter;
import ca.uwaterloo.averroes.exceptions.AverroesException;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.util.TimeUtils;
import ca.uwaterloo.averroes.util.io.FileUtils;

/**
 * A driver class that generates call graph for SparkAverroes, that is Spark using the Averroes placeholder library.
 * 
 * @author karim
 * 
 */
public class SparkWithAverroesCallGraphGenerator {

	public static void main(String[] args) {
		try {
			TimeUtils.reset();
			if (args.length != 1) {
				usage();
				throw new AverroesException("SparkAverroes expects exactly 1 argument.");
			}

			// Process the arguments
			String benchmark = args[0];

			FileUtils.createDirectory(AverroesProperties.getOutputDir());
			CallGraph spark = CallGraphFactory.generateSparkWithAverroesCallGraph(benchmark);
			System.out.println("Total time to finish: " + TimeUtils.elapsedTime());
			new GXLWriter().write(spark, FileUtils.sparkAverroesCallGraphFile());

			// Print some statistics
			System.out.println("=================================================");
			System.out.println("# edges = " + spark.size());
			System.out.println("=================================================");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void usage() {
		System.out.println("");
		System.out.println("Usage: java -jar spark-averroes.jar <benchmark_name>");
		System.out.println("");
	}
}
