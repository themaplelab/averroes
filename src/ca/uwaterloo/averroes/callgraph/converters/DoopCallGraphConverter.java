package ca.uwaterloo.averroes.callgraph.converters;

import java.io.IOException;

import org.deri.iris.storage.IRelation;

import probe.CallEdge;
import probe.CallGraph;
import probe.ProbeMethod;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.util.ProbeUtils;
import ca.uwaterloo.averroes.util.ResultImporter;

public class DoopCallGraphConverter {

	/**
	 * Convert the generated Doop call graph to summarized Probe version.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static CallGraph retrieve(String doopHome) throws IOException {
		CallGraph probe = new CallGraph();
		IRelation edges = ResultImporter.getDoopCallGraphEdges(doopHome);
		IRelation entryPoints = ResultImporter.getDoopEntryPoints(doopHome);

		// Create the graph entry points
		for (int i = 0; i < entryPoints.size(); i++) {
			String methodSignature = (String) entryPoints.get(i).get(0).getValue();
			ProbeMethod method = ProbeUtils.createProbeMethodBySignature(methodSignature);
			probe.entryPoints().add(method);
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
				probe.edges().add(new CallEdge(src, dst));
			} else if (isSrcApp && !isDstApp) {
				probe.edges().add(new CallEdge(src, ProbeUtils.LIBRARY_BLOB));
			} else if (!isSrcApp && isDstApp) {
				probe.edges().add(new CallEdge(ProbeUtils.LIBRARY_BLOB, dst));
			}
		}

		return probe;
	}
}
