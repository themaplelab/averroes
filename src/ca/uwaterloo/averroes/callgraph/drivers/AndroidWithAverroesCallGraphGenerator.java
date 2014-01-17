package ca.uwaterloo.averroes.callgraph.drivers;

import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;

import probe.CallGraph;
import probe.TextWriter;
import ca.uwaterloo.averroes.callgraph.CallGraphFactory;
import ca.uwaterloo.averroes.exceptions.AverroesException;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.util.TimeUtils;
import ca.uwaterloo.averroes.util.io.FileUtils;

/**
 * A driver class that generates call graph for Spark using the original library.
 * 
 * @author karim
 * 
 */
public class AndroidWithAverroesCallGraphGenerator {

	public static void main(String[] args) {
		try {
			TimeUtils.reset();

			if (args.length != 1) {
				usage();
				throw new AverroesException("AndroidAverroes expects exactly 1 arguments.");
			}

			// Process the arguments
			String benchmark = args[0];

			FileUtils.createDirectory(AverroesProperties.getOutputDir());
			CallGraph android = CallGraphFactory.generateAndroidWithAverroesCallGraph(benchmark);
			System.out.println("Total time to finish: " + TimeUtils.elapsedTime());

			// Write the android-averroes call graph to disk
			TextWriter writer = new TextWriter();
			writer.write(android, new GZIPOutputStream(new FileOutputStream(FileUtils.androidAverroesCallGraphFile())));

			// Print some statistics
			System.out.println("=================================================");
			System.out.println("# edges = " + android.edges().size());
			System.out.println("=================================================");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void usage() {
		System.out.println("");
		System.out.println("Usage: java -jar android-averroes.jar <benchmark_name>");
		System.out.println("");
	}
}
