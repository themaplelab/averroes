package ca.uwaterloo.averroes.callgraph.drivers;

import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;

import probe.CallGraph;
import probe.TextWriter;
import ca.uwaterloo.averroes.callgraph.CallGraphFactory;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.util.TimeUtils;
import ca.uwaterloo.averroes.util.io.FileUtils;

/**
 * A driver class that generates call graph for Spark using the original library.
 * 
 * @author karim
 * 
 */
public class AndroidCallGraphGenerator {

	public static void main(String[] args) {
		try {
			// Find the total execution time, instead of depending on the Unix time command
			TimeUtils.splitStart();

			// Generate the call graph
			TimeUtils.reset();
			FileUtils.createDirectory(AverroesProperties.getOutputDir());
			CallGraph android = CallGraphFactory.generateAndroidCallGraph();
			System.out.println("Total time to finish: " + TimeUtils.elapsedTime());

			// Write the cal graph to disk
			TextWriter writer = new TextWriter();
			writer.write(android, new GZIPOutputStream(new FileOutputStream(FileUtils.androidCallGraphFile())));

			// Print some statistics
			System.out.println("=================================================");
			System.out.println("# edges = " + android.edges().size());
			System.out.println("=================================================");

			double total = TimeUtils.elapsedSplitTime();
			System.out.println("Elapsed time: " + total + " seconds.");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void usage() {
		System.out.println("");
		System.out.println("Usage: java -jar android.jar");
		System.out.println("");
	}
}
