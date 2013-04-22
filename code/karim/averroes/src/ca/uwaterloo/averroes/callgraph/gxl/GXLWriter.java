package ca.uwaterloo.averroes.callgraph.gxl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.gxl.GXLDocument;
import net.sourceforge.gxl.GXLEdge;
import net.sourceforge.gxl.GXLGraph;
import net.sourceforge.gxl.GXLNode;
import net.sourceforge.gxl.GXLString;
import probe.CallEdge;
import probe.ProbeClass;
import probe.ProbeMethod;
import ca.uwaterloo.averroes.callgraph.CallGraph;

/**
 * A write for application-only call graphs that writes them in GXL format.
 * 
 * @author karim
 * 
 */
public class GXLWriter {

	private Set<ProbeMethod> methods;
	private Set<ProbeClass> classes;
	private Map<Object, Integer> idMap;

	public void write(CallGraph callGraph, String fileName) throws IOException {
		OutputStream file = new FileOutputStream(new File(fileName));
		initializeMaps();

		// Collect up all the methods and classes appearing in the call graph.
		for (ProbeMethod method : callGraph.methods()) {
			methods.add(method);
			classes.add(method.cls());
		}

		// Assign ids to all method and class nodes.
		assignIDs();

		// Create the GXL nodes in the graph.
		GXLDocument gxlDocument = new GXLDocument();
		GXLGraph graph = new GXLGraph("callgraph");
		graph.setType(URIs.v().uCallGraph());

		if (callGraph.hasEntryPoints()) {
			addRoot(graph);
		}

		if (callGraph.hasLibraryEdges()) {
			addLibrary(graph);
		}

		addClasses(graph);
		addMethods(graph);

		// Add the call edges to the GXL graph
		for (ProbeMethod method : callGraph.entryPoints()) {
			GXLEdge edge = new GXLEdge(GXLNodeFactory.ROOT_NODE_ID, getId(method));
			edge.setType(URIs.v().entryPoint());
			graph.add(edge);
		}

		for (CallEdge edge : callGraph.appToAppEdges()) {
			GXLEdge gxlEdge = new GXLEdge(getId(edge.src()), getId(edge.dst()));
			gxlEdge.setType(URIs.v().calls());
			graph.add(gxlEdge);
		}

		for (ProbeMethod src : callGraph.appToLibEdges()) {
			GXLEdge edge = new GXLEdge(getId(src), GXLNodeFactory.LIBRARY_NODE_ID);
			edge.setType(URIs.v().callsLibrary());
			graph.add(edge);
		}

		for (ProbeMethod dst : callGraph.libToAppEdges()) {
			GXLEdge edge = new GXLEdge(GXLNodeFactory.LIBRARY_NODE_ID, getId(dst));
			edge.setType(URIs.v().libraryCallsBack());
			graph.add(edge);
		}

		// Write out the GXL graph.
		gxlDocument.getDocumentElement().add(graph);
		gxlDocument.write(file);
	}

	private void initializeMaps() {
		methods = new HashSet<ProbeMethod>();
		classes = new HashSet<ProbeClass>();
	}

	/** Assign ids to all method and class nodes. */
	private void assignIDs() {
		int id = 1;
		idMap = new HashMap<Object, Integer>();

		for (ProbeMethod method : methods) {
			idMap.put(method, new Integer(id++));
		}

		for (ProbeClass klass : classes) {
			idMap.put(klass, new Integer(id++));
		}
	}

	private void addClasses(GXLGraph graph) {
		for (ProbeClass klass : classes) {
			addClass(graph, klass);
		}
	}

	private void addMethods(GXLGraph graph) {
		for (ProbeMethod method : methods) {
			addMethod(graph, method);
		}
	}

	private void addClass(GXLGraph graph, ProbeClass klass) {
		GXLNode node = new GXLNode(getId(klass));
		node.setType(URIs.v().uClass());
		node.setAttr("package", new GXLString(klass.pkg()));
		node.setAttr("name", new GXLString(klass.name()));
		graph.add(node);
	}

	private void addMethod(GXLGraph graph, ProbeMethod method) {
		GXLNode node = new GXLNode(getId(method));
		node.setType(URIs.v().uMethod());
		node.setAttr("name", new GXLString(method.name()));
		node.setAttr("signature", new GXLString(method.signature()));
		graph.add(node);

		GXLEdge classEdge = new GXLEdge(getId(method), getId(method.cls()));
		classEdge.setType(URIs.v().declaredIn());
		graph.add(classEdge);
	}

	private void addRoot(GXLGraph graph) {
		graph.add(GXLNodeFactory.rootNode());
	}

	private void addLibrary(GXLGraph graph) {
		graph.add(GXLNodeFactory.libraryNode());
	}

	private String getId(ProbeClass klass) {
		Integer id = idMap.get(klass);
		return "id" + id.toString();
	}

	private String getId(ProbeMethod method) {
		Integer id = idMap.get(method);
		return "id" + id.toString();
	}
}