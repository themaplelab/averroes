package ca.uwaterloo.averroes.callgraph.converters;

import java.io.IOException;

import org.deri.iris.storage.IRelation;

import probe.CallEdge;
import probe.ProbeMethod;
import ca.uwaterloo.averroes.callgraph.CallGraph;
import ca.uwaterloo.averroes.callgraph.CallGraphSource;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.util.ProbeUtils;
import ca.uwaterloo.averroes.util.ResultImporter;

public class DoopCallGraphConverter {

	/**
	 * Convert the generated Doop call graph to summarized CGC version.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static CallGraph convert(String doopHome) throws IOException {
		CallGraph graph = new CallGraph(CallGraphSource.DOOP_AVERROES);
		IRelation edges = ResultImporter.getDoopCallGraphEdges(doopHome);
		IRelation entryPoints = ResultImporter.getDoopEntryPoints(doopHome);

		// Create the graph entry points
		for (int i = 0; i < entryPoints.size(); i++) {
			String methodSignature = (String) entryPoints.get(i).get(0).getValue();
			ProbeMethod method = ProbeUtils.createProbeMethodBySignature(methodSignature);
			graph.entryPoints().add(method);
		}

		// Create the edges according to the app_includes parameter
		for (int i = 0; i < edges.size(); i++) {
			String srcName = (String) edges.get(i).get(0).getValue();
			String dstName = (String) edges.get(i).get(2).getValue();

			ProbeMethod src = ProbeUtils.createProbeMethodBySignature(srcName);
			ProbeMethod dst = ProbeUtils.createProbeMethodBySignature(dstName);

			boolean isSrcApp = AverroesProperties.isApplicationMethod(src);
			boolean isDstApp = AverroesProperties.isApplicationMethod(dst);

			if (isSrcApp && isDstApp) {
				graph.appToAppEdges().add(new CallEdge(src, dst));
			} else if (isSrcApp && !isDstApp) {
				graph.appToLibEdges().add(src);
			} else if (!isSrcApp && isDstApp) {
				graph.libToAppEdges().add(dst);
			}
		}

		return graph;
	}
}
