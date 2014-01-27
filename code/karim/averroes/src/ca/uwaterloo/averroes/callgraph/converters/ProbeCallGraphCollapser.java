package ca.uwaterloo.averroes.callgraph.converters;

import probe.CallEdge;
import probe.CallGraph;
import probe.ObjectManager;
import probe.ProbeMethod;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.soot.Names;

/**
 * A converter that transforms a probe call graph to an application-only call graph.
 * 
 * @author karim
 * 
 */
public class ProbeCallGraphCollapser {
	public static final ProbeMethod LIBRARY_BLOB = ObjectManager.v().getMethod(
			ObjectManager.v().getClass(Names.AVERROES_LIBRARY_CLASS), Names.BLOB, "");

	/**
	 * Do the conversion
	 * 
	 * @param probeCallGraph
	 * @return
	 */
	public static CallGraph collapse(CallGraph probeCallGraph) {
		CallGraph result = new CallGraph();

		// Getting the entry points
		result.entryPoints().addAll(probeCallGraph.entryPoints());

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
					result.edges().add(edge);
				} else if (isSrcApp && !isDstApp) {
					result.edges().add(new CallEdge(src, LIBRARY_BLOB));
				} else if (!isSrcApp && isDstApp) {
					result.edges().add(new CallEdge(LIBRARY_BLOB, dst));
				}
			}
		}

		return result;
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