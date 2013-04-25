package ca.uwaterloo.averroes.callgraph.drivers;

import ca.uwaterloo.averroes.callgraph.CallGraph;
import ca.uwaterloo.averroes.callgraph.CallGraphFactory;
import ca.uwaterloo.averroes.callgraph.gxl.GXLWriter;
import ca.uwaterloo.averroes.exceptions.AverroesException;
import ca.uwaterloo.averroes.properties.AverroesProperties;
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
			if (args.length != 1) {
				usage();
				throw new AverroesException("SparkAverroes expects exactly 1 argument.");
			}

			String benchmark = args[0];

			FileUtils.createDirectory(AverroesProperties.getOutputDir());
			CallGraph spark = CallGraphFactory.generateSparkWithAverroesCallGraph(benchmark);
			new GXLWriter().write(spark, FileUtils.sparkAverroesCallGraphFile());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void usage() {
		System.out.println("");
		System.out.println("Usage: java -jar sparkaverroes.jar <benchmark_name>");
		System.out.println("");
	}
}