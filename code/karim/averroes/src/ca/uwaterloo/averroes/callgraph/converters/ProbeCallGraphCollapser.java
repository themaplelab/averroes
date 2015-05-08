package ca.uwaterloo.averroes.callgraph.converters;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import probe.CallEdge;
import probe.CallGraph;
import probe.ProbeMethod;
import probe.TextWriter;
import ca.uwaterloo.averroes.callgraph.CallGraphSource;
import ca.uwaterloo.averroes.callgraph.gxl.GXLReader;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.util.ProbeUtils;

/**
 * A converter that transforms a probe call graph to an application-only call
 * graph.
 * 
 * @author karim
 * 
 */
public class ProbeCallGraphCollapser {
	/**
	 * Collapse the given probe call graph to an averroes call graph that
	 * summarizes the library in one blob.
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
			 * We don't care about the following edges (primarily used for
			 * converting dynamic call graphs) 1) edges to <clinit> methods 2)
			 * edges to java.lang.ClassLoader:
			 * loadClassInternal(Ljava/lang/String;) 3) edges to
			 * java.lang.ClassLoader:
			 * checkPackageAccess(Ljava/lang/Class;Ljava/security
			 * /ProtectionDomain;)
			 */
			if (!isClinit(dst) && !isLoadClassInternal(dst) && !isCheckPackageAccess(dst)) {
				boolean isSrcApp = AverroesProperties.isApplicationMethod(src);
				boolean isDstApp = AverroesProperties.isApplicationMethod(dst);

				if (isSrcApp && isDstApp) {
					result.edges().add(edge);
				} else if (isSrcApp && !isDstApp) {
					result.edges().add(new CallEdge(src, ProbeUtils.LIBRARY_BLOB));
				} else if (!isSrcApp && isDstApp) {
					result.edges().add(new CallEdge(ProbeUtils.LIBRARY_BLOB, dst));
				}
			}
		}

		return result;
	}

	/**
	 * Convert from the Averroes probe format to the standard probe format.
	 * 
	 * @param aveCallGraph
	 * @return
	 */
	public static CallGraph collapse(ca.uwaterloo.averroes.callgraph.CallGraph aveCallGraph) {
		CallGraph result = new CallGraph();

		// Add the entry points
		aveCallGraph.entryPoints().forEach(entry -> result.entryPoints().add(entry));

		// Add the edges appropriately
		aveCallGraph.appToAppEdges().forEach(edge -> result.edges().add(edge));
		aveCallGraph.appToLibEdges().forEach(src -> result.edges().add(new CallEdge(src, ProbeUtils.LIBRARY_BLOB)));
		aveCallGraph.libToAppEdges().forEach(dst -> result.edges().add(new CallEdge(ProbeUtils.LIBRARY_BLOB, dst)));

		return result;
	}

	public static ca.uwaterloo.averroes.callgraph.CallGraph collapse(CallGraph probeCallGraph, CallGraphSource source) {
		ca.uwaterloo.averroes.callgraph.CallGraph result = new ca.uwaterloo.averroes.callgraph.CallGraph(source);

		// Getting the entry points
		result.entryPoints().addAll(probeCallGraph.entryPoints());

		// Getting the correct placement for each edge
		// Note: If both src and dst of an edge are in the library, ignore it.
		for (CallEdge edge : probeCallGraph.edges()) {
			ProbeMethod src = edge.src();
			ProbeMethod dst = edge.dst();

			/*
			 * We don't care about the following edges (primarily used for
			 * converting dynamic call graphs) 1) edges to <clinit> methods 2)
			 * edges to java.lang.ClassLoader:
			 * loadClassInternal(Ljava/lang/String;) 3) edges to
			 * java.lang.ClassLoader:
			 * checkPackageAccess(Ljava/lang/Class;Ljava/security
			 * /ProtectionDomain;)
			 */
			if (!isClinit(dst) && !isLoadClassInternal(dst) && !isCheckPackageAccess(dst)) {
				boolean isSrcApp = AverroesProperties.isApplicationMethod(src);
				boolean isDstApp = AverroesProperties.isApplicationMethod(dst);

				if (isSrcApp && isDstApp) {
					result.appToAppEdges().add(edge);
				} else if (isSrcApp && !isDstApp) {
					result.appToLibEdges().add(src);
				} else if (!isSrcApp && isDstApp) {
					result.libToAppEdges().add(dst);
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

	public static void main(String[] args) {
		try {
			ca.uwaterloo.averroes.callgraph.CallGraph aveCallGraph = new GXLReader().readCallGraph(new FileInputStream(
					args[0]), CallGraphSource.DUMMY);
			CallGraph probe = collapse(aveCallGraph);
			new TextWriter().write(probe,
					new GZIPOutputStream(new FileOutputStream(args[0].replace(".gxl", ".txt.gzip"))));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}