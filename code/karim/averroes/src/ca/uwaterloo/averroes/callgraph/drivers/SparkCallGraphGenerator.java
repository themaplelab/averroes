package ca.uwaterloo.averroes.callgraph.drivers;

import ca.uwaterloo.averroes.callgraph.CallGraph;
import ca.uwaterloo.averroes.callgraph.CallGraphFactory;
import ca.uwaterloo.averroes.callgraph.gxl.GXLConverter;
import ca.uwaterloo.averroes.callgraph.gxl.GXLWriter;
import ca.uwaterloo.averroes.dot.Format;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.util.TimeUtils;
import ca.uwaterloo.averroes.util.io.FileUtils;

/**
 * A driver class that generates call graph for Spark using the original library.
 * 
 * @author karim
 * 
 */
public class SparkCallGraphGenerator {

	public static void main(String[] args) {
		try {
			// Generate the call graph
			TimeUtils.reset();
			FileUtils.createDirectory(AverroesProperties.getOutputDir());
			CallGraph spark = CallGraphFactory.generateSparkCallGraph();
			System.out.println("Total time to finish: " + TimeUtils.elapsedTime());
			new GXLWriter().write(spark, FileUtils.sparkCallGraphFile());
			GXLConverter.probeGxl2Dot(FileUtils.sparkCallGraphFile(), Format.DOT);

			// Print some statistics
			System.out.println("=================================================");
			System.out.println("# edges = " + spark.size());
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
