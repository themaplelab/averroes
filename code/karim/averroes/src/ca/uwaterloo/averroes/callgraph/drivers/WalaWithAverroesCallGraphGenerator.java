package ca.uwaterloo.averroes.callgraph.drivers;

import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;

import probe.TextWriter;
import ca.uwaterloo.averroes.callgraph.CallGraph;
import ca.uwaterloo.averroes.callgraph.CallGraphFactory;
import ca.uwaterloo.averroes.callgraph.CallGraphSource;
import ca.uwaterloo.averroes.callgraph.converters.ProbeCallGraphCollapser;
import ca.uwaterloo.averroes.callgraph.gxl.GXLWriter;
import ca.uwaterloo.averroes.exceptions.AverroesException;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.util.TimeUtils;
import ca.uwaterloo.averroes.util.io.FileUtils;

/**
 * A driver class that generates call graph for WALA using the averroes placeholder library.
 * 
 * @author karim
 * 
 */
public class WalaWithAverroesCallGraphGenerator {

	public static void main(String[] args) {
		try {
			// Generate the call graph
			TimeUtils.reset();
			if (args.length != 2) {
				usage();
				throw new AverroesException("WalaAverroes expects exactly 2 arguments.");
			}

			// Process the arguments
			String base = args[0];
			String benchmark = args[1];
			
			FileUtils.createDirectory(AverroesProperties.getOutputDir());
			probe.CallGraph probe = CallGraphFactory.generateWalaCallGraph(base, benchmark, true); 
			CallGraph wala = ProbeCallGraphCollapser.collapse(probe, CallGraphSource.WALA_AVERROES);
			System.out.println("Total time to finish: " + TimeUtils.elapsedTime());
			new GXLWriter().write(wala, FileUtils.walaAverroesCallGraphFile());

			// Output the txt.gzip version for easier comparisons // TODO: change all call graphs to this!
			probe.CallGraph collapsed = ProbeCallGraphCollapser.collapse(probe);
			new TextWriter().write(collapsed, new GZIPOutputStream(new FileOutputStream(FileUtils.walaAverroesCallGraphGzipFile())));
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
		System.out.println("Usage: java -jar wala-averroes.jar");
		System.out.println("");
	}
}
