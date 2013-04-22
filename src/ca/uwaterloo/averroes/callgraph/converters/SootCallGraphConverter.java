package ca.uwaterloo.averroes.callgraph.converters;

import probe.CallEdge;
import probe.ProbeMethod;
import ca.uwaterloo.averroes.callgraph.CallGraph;
import ca.uwaterloo.averroes.callgraph.CallGraphSource;
import ca.uwaterloo.averroes.properties.AverroesProperties;

/**
 * A converter that transforms a probe call graph to an application-only call graph.
 * 
 * @author karim
 * 
 */
public class SootCallGraphConverter {

	/**
	 * Do the conversion
	 * 
	 * @param probeCallGraph
	 * @return
	 */
	public static CallGraph convert(probe.CallGraph probeCallGraph) {
		CallGraph callGraph = new CallGraph(CallGraphSource.SPARK_AVERROES);

		// Getting the entry points
		callGraph.entryPoints().addAll(probeCallGraph.entryPoints());

		// Getting the correct placement for each edge
		// Note: If both src and dst of an edge are in the library, ignore it.
		for (CallEdge edge : probeCallGraph.edges()) {
			ProbeMethod src = edge.src();
			ProbeMethod dst = edge.dst();

			/*
			 * We don't care about the following edges (primarily used for converting dynamic call graphs) 1) edges to
			 * <clinit> methods 2) edges to java.lang.ClassLoader: loadClassInternal(Ljava/lang/String;) 3) edges to
			 * java.lang.ClassLoader: checkPackageAccess(Ljava/lang/Class;Ljava/security /ProtectionDomain;)
			 */
			if (!isClinit(dst) && !isLoadClassInternal(dst) && !isCheckPackageAccess(dst)) {
				boolean isSrcApp = AverroesProperties.isApplicationMethod(src);
				boolean isDstApp = AverroesProperties.isApplicationMethod(dst);

				if (isSrcApp && isDstApp) {
					callGraph.appToAppEdges().add(new CallEdge(src, dst));
				} else if (isSrcApp && !isDstApp) {
					callGraph.appToLibEdges().add(src);
				} else if (!isSrcApp && isDstApp) {
					callGraph.libToAppEdges().add(dst);
				}
			}
		}

		return callGraph;
	}

	public static boolean isClinit(ProbeMethod method) {
		return method.name().equalsIgnoreCase("<clinit>");
	}

	public static boolean isLoadClassInternal(ProbeMethod method) {
		return method.toString().equalsIgnoreCase("java.lang.ClassLoader: loadClassInternal(Ljava/lang/String;)");
	}

	public static boolean isCheckPackageAccess(ProbeMethod method) {
		return method.toString().equalsIgnoreCase(
				"java.lang.ClassLoader: checkPackageAccess(Ljava/lang/Class;Ljava/security/ProtectionDomain;)");
	}
}