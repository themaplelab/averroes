package ca.uwaterloo.averroes.callgraph;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xml.sax.SAXException;

import probe.CallEdge;
import probe.ProbeMethod;
import ca.uwaterloo.averroes.callgraph.gxl.GXLWriter;
import ca.uwaterloo.averroes.stats.Statistics;
import ca.uwaterloo.averroes.util.SetUtils;
import ca.uwaterloo.averroes.util.io.FileUtils;

/**
 * A call graph is a set of edges and entry points.
 * 
 * @author karim
 * 
 */
public class CallGraph {

	private Set<ProbeMethod> entryPoints;
	private Set<CallEdge> appToAppEdges;
	private Set<ProbeMethod> appToLibEdges;
	private Set<ProbeMethod> libToAppEdges;
	private Map<String, Integer> frequency;

	private CallGraphSource source;

	public CallGraph(CallGraphSource source) {
		entryPoints = new HashSet<ProbeMethod>();
		appToAppEdges = new HashSet<CallEdge>();
		appToLibEdges = new HashSet<ProbeMethod>();
		libToAppEdges = new HashSet<ProbeMethod>();
		frequency = new HashMap<String, Integer>();
		this.source = source;
	}

	public Set<ProbeMethod> entryPoints() {
		return entryPoints;
	}

	public Set<CallEdge> appToAppEdges() {
		return appToAppEdges;
	}

	public Set<ProbeMethod> appToLibEdges() {
		return appToLibEdges;
	}

	public Set<ProbeMethod> libToAppEdges() {
		return libToAppEdges;
	}

	public CallGraphSource getSource() {
		return source;
	}

	public void setSource(CallGraphSource source) {
		this.source = source;
	}
	
	public int size() {
		return appToAppEdges.size() + appToLibEdges.size() + libToAppEdges.size();
	}

	public Set<ProbeMethod> methods() {
		Set<ProbeMethod> methodSet = new HashSet<ProbeMethod>();

		methodSet.addAll(entryPoints);

		for (CallEdge edge : appToAppEdges) {
			methodSet.add(edge.src());
			methodSet.add(edge.dst());
		}

		methodSet.addAll(appToLibEdges);
		methodSet.addAll(libToAppEdges);

		return Collections.unmodifiableSet(methodSet);
	}

	/**
	 * Get a set of those methods that are transitively reachable in the call graph from its entry points.
	 * 
	 * @return
	 */
	public Set<ProbeMethod> findReachables() {
		// Add entry points, and library call backs
		Set<ProbeMethod> reachables = new HashSet<ProbeMethod>(applicationEntryPoints());
		reachables.addAll(libToAppEdges);

		while (true) {
			Set<ProbeMethod> newReachables = new HashSet<ProbeMethod>();
			for (CallEdge e : appToAppEdges) {
				if (reachables.contains(e.src()) && !reachables.contains(e.dst())) {
					newReachables.add(e.dst());
				}
			}

			if (newReachables.isEmpty()) {
				break;
			}
			reachables.addAll(newReachables);
		}

		return reachables;
	}

	/**
	 * Get the set of application entry points. This is used for dynamic call graphs.
	 * 
	 * @return
	 */
	public Set<ProbeMethod> applicationEntryPoints() {
		Set<ProbeMethod> result = new HashSet<ProbeMethod>();
		
		for (ProbeMethod method : entryPoints) {
			if(!method.cls().pkg().startsWith("java.lang")) {
				result.add(method);
			}
		}
		
		return result;
	}

	public boolean hasLibraryEdges() {
		return appToLibEdges.size() > 0 || libToAppEdges.size() > 0;
	}

	public boolean hasApplicationEdges() {
		return appToAppEdges.size() > 0 || appToLibEdges.size() > 0;
	}

	public boolean hasEdges() {
		return hasApplicationEdges() || hasLibraryEdges();
	}

	public boolean hasEntryPoints() {
		return entryPoints.size() > 0;
	}

	public void print(CallGraphPrintOptions options) {
		if (options.isPrintEntryPoints()) {
			System.out.println("Entry points:");
			for (ProbeMethod method : entryPoints) {
				System.out.println(method);
			}
		}

		if (options.isPrintAppToAppEdges()) {
			System.out.println("Application ===> Application:");
			for (CallEdge edge : appToAppEdges) {
				System.out.println(edge);
			}
		}

		if (options.isPrintAppToLibEdges()) {
			System.out.println("Application ===> Library:");
			for (ProbeMethod method : appToLibEdges) {
				System.out.println(method);
			}
		}

		if (options.isPrintLibToAppEdges()) {
			System.out.println("Library ===> Application:");
			for (ProbeMethod method : libToAppEdges) {
				System.out.println(method);
			}

			System.out.println("");
			printFrequencies();
		}
	}

	public void printFrequencies() {
		populateFrequencies();

		if (frequency.keySet().isEmpty()) {
			System.out.println("<EMPTY>");
		} else {
			SortedMap<String, Integer> map = new TreeMap<String, Integer>(new ValueComparer(frequency));
			map.putAll(frequency);
			for (String method : map.keySet()) {
				System.out.println(method + " = " + map.get(method));
			}
		}
	}

	public void populateFrequencies() {
		for (ProbeMethod method : libToAppEdges) {
			String key = method.name();
			frequency.put(key, frequency.containsKey(key) ? frequency.get(key) + 1 : 1);
		}
	}

	public CallGraph union(CallGraph other) {
		CallGraph result = new CallGraph(source);
		result.entryPoints().addAll(SetUtils.union(entryPoints, other.entryPoints));
		result.appToAppEdges().addAll(SetUtils.union(appToAppEdges, other.appToAppEdges));
		result.appToLibEdges().addAll(SetUtils.union(appToLibEdges, other.appToLibEdges));
		result.libToAppEdges().addAll(SetUtils.union(libToAppEdges, other.libToAppEdges));
		return result;
	}

	/**
	 * Compute the relative complement of two call graphs.
	 */
	public CallGraph minus(CallGraph other, CallGraphDiffOptions options, CallGraphSource source) {
		CallGraph result = new CallGraph(source);

		if (options.isDiffEntryPoints()) {
			result.entryPoints.addAll(SetUtils.minus(entryPoints, other.entryPoints));
		}

		if (options.isDiffAppToAppEdges()) {
			result.appToAppEdges.addAll(SetUtils.minus(appToAppEdges, other.appToAppEdges));
		}

		if (options.isDiffAppToLibEdges()) {
			result.appToLibEdges.addAll(SetUtils.minus(appToLibEdges, other.appToLibEdges));
		}

		if (options.isDiffLibToAppEdges()) {
			result.libToAppEdges.addAll(SetUtils.minus(libToAppEdges, other.libToAppEdges));
		}
		return result;
	}

	/**
	 * Compare to another call graph. The resulting differences in edges are written as call graphs in GXL format.
	 * 
	 * @param other
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 */
	public Statistics diffCallGraphs(CallGraph other) throws IOException, SAXException {
		diffCallGraphsApp(other);
		diffCallGraphsLib(other);
		return diffCallGraphsAll(other);
	}

	/**
	 * Compare all the edges to the another call graph. The resulting set of edges is written as a call graph as well in
	 * GXL format.
	 * 
	 * @param superGraph
	 * @param other
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 */
	public Statistics diffCallGraphsAll(CallGraph other) throws IOException, SAXException {
		CallGraph missing = minus(other, CallGraphDiffOptions.defaultOptions, source);
		CallGraph extra = other.minus(this, CallGraphDiffOptions.defaultOptions, source);

		Statistics stats = new Statistics(other, this, missing, extra);

		String file;
		file = FileUtils.missingCallGraphFile(source, other.source);
		new GXLWriter().write(missing, file);

		file = FileUtils.extraCallGraphFile(source, other.source);
		new GXLWriter().write(extra, file);

		return stats;
	}

	/**
	 * Compare the edges that originate from the application. This includes application-to-application, and
	 * application-to-library edges. The resulting set of edges is written as a call graph as well in GXL format.
	 * 
	 * @param other
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 */
	public Statistics diffCallGraphsApp(CallGraph other) throws IOException, SAXException {
		CallGraph missing = minus(other, CallGraphDiffOptions.soundnessOptions, source);
		CallGraph extra = other.minus(this, CallGraphDiffOptions.soundnessOptions, source);

		Statistics stats = new Statistics(other, this, missing, extra);

		String file;
		file = FileUtils.missingCallGraphFile(source, other.source, CallGraphDiffOptions.soundnessOptions);
		new GXLWriter().write(missing, file);

		file = FileUtils.extraCallGraphFile(source, other.source, CallGraphDiffOptions.soundnessOptions);
		new GXLWriter().write(extra, file);

		return stats;
	}

	/**
	 * Compare the edges that originate from the library. This includes the library-to-application edges only (i.e.,
	 * library callbacks). The resulting set of edges is written as a call graph as well in GXL format.
	 * 
	 * @param other
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 */
	public Statistics diffCallGraphsLib(CallGraph other) throws IOException, SAXException {
		CallGraph missing = minus(other, CallGraphDiffOptions.precisionOptions, source);
		CallGraph extra = other.minus(this, CallGraphDiffOptions.precisionOptions, source);

		Statistics stats = new Statistics(other, this, missing, extra);

		String file;
		file = FileUtils.missingCallGraphFile(source, other.source, CallGraphDiffOptions.precisionOptions);
		new GXLWriter().write(missing, file);

		file = FileUtils.extraCallGraphFile(source, other.source, CallGraphDiffOptions.precisionOptions);
		new GXLWriter().write(extra, file);

		return stats;
	}
}