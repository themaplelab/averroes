package ca.uwaterloo.averroes.callgraph;

import java.io.IOException;

import ca.uwaterloo.averroes.callgraph.converters.DoopCallGraphConverter;
import ca.uwaterloo.averroes.callgraph.converters.SootCallGraphConverter;
import ca.uwaterloo.averroes.callgraph.transformers.AndroidCallGraphTransformer;
import ca.uwaterloo.averroes.callgraph.transformers.SparkCallGraphTransformer;
import ca.uwaterloo.averroes.callgraph.transformers.SparkWithAverroesCallGraphTransformer;
import ca.uwaterloo.averroes.util.CommandExecuter;

/**
 * A factory that generates call graphs for some tools.
 * 
 * @author karim
 * 
 */
public class CallGraphFactory {

	/**
	 * Generate the call graph for SparkAverroes.
	 * 
	 * @param benchmark
	 * @return
	 * @throws IOException
	 */
	public static CallGraph generateSparkWithAverroesCallGraph(String benchmark) throws IOException {
		probe.CallGraph spark = new SparkWithAverroesCallGraphTransformer(benchmark).getProbeCallGraph();
		return SootCallGraphConverter.convert(spark);
	}

	/**
	 * Generate the call graph for Spark.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static CallGraph generateSparkCallGraph() throws IOException {
		probe.CallGraph spark = new SparkCallGraphTransformer().getProbeCallGraph();
		return SootCallGraphConverter.convert(spark);
	}
	
	/**
	 * Generate the call graph for an Android apk.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static probe.CallGraph generateAndroidCallGraph() throws IOException {
		probe.CallGraph android = new AndroidCallGraphTransformer().getProbeCallGraph();
		return android;
	}
	
	/**
	 * Generate the call graph for DoopAverroes.
	 * 
	 * @param doopHome
	 * @param benchmark
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static CallGraph generateDoopWithAverroesCallGraph(String doopHome, String benchmark) throws IOException,
			InterruptedException {
		// 1. Run doop's analysis
		CommandExecuter.runDoopAverroes(doopHome, benchmark);

		// 2. Convert the Doop call graph
		return DoopCallGraphConverter.convert(doopHome);
	}
}