package ca.uwaterloo.averroes.callgraph;

import java.io.IOException;

import ca.uwaterloo.averroes.callgraph.converters.SootCallGraphConverter;
import ca.uwaterloo.averroes.callgraph.transformers.SparkCallGraphTransformer;
import ca.uwaterloo.averroes.callgraph.transformers.SparkWithAverroesCallGraphTransformer;

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
	public static probe.CallGraph generateSparkCallGraph() throws IOException {
		return new SparkCallGraphTransformer().getProbeCallGraph();
	}
}