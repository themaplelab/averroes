package ca.uwaterloo.averroes.callgraph.gxl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.gxl.GXLAtomicValue;
import net.sourceforge.gxl.GXLAttr;
import net.sourceforge.gxl.GXLAttributedElement;
import net.sourceforge.gxl.GXLDocument;
import net.sourceforge.gxl.GXLEdge;
import net.sourceforge.gxl.GXLElement;
import net.sourceforge.gxl.GXLGraph;
import net.sourceforge.gxl.GXLNode;
import probe.CallEdge;
import probe.ObjectManager;
import probe.ProbeClass;
import probe.ProbeMethod;
import ca.uwaterloo.averroes.callgraph.CallGraph;
import ca.uwaterloo.averroes.callgraph.CallGraphSource;

/**
 * A reader for application-only call graphs written in GXL format.
 * 
 * @author karim
 * 
 */
public class GXLReader {
	private GXLDocument gxlDocument;
	private GXLGraph graph;

	private Map<GXLNode, GXLNode> declaredIn = new HashMap<GXLNode, GXLNode>();
	private List<GXLNode> entryPoints = new ArrayList<GXLNode>();
	private List<GXLEdge> callEdges = new ArrayList<GXLEdge>();

	private List<GXLEdge> edges = new ArrayList<GXLEdge>();
	private List<GXLNode> nodes = new ArrayList<GXLNode>();

	private Set<GXLNode> classes = new HashSet<GXLNode>();
	private Set<GXLNode> methods = new HashSet<GXLNode>();

	public CallGraph readCallGraph(InputStream file, CallGraphSource source) throws IOException {
		getGraph(file, "callgraph");
		readNodesEdges();
		sortNodes();
		sortEdges();
		createNodeMaps();

		CallGraph ret = new CallGraph(source);

		for (GXLNode node : entryPoints) {
			ret.entryPoints().add((ProbeMethod) nodeToMethod.get(node));
		}
		for (GXLEdge edge : callEdges) {
			if (isAppToAppEdge(edge)) {
				ret.appToAppEdges().add(
						new CallEdge((ProbeMethod) nodeToMethod.get(edge.getSource()), (ProbeMethod) nodeToMethod
								.get(edge.getTarget())));
			} else if (isAppToLibEdge(edge)) {
				ret.appToLibEdges().add((ProbeMethod) nodeToMethod.get(edge.getSource()));
			} else if (isLibToAppEdge(edge)) {
				ret.libToAppEdges().add((ProbeMethod) nodeToMethod.get(edge.getTarget()));
			}
		}

		return ret;
	}

	public boolean isAppToAppEdge(GXLEdge edge) {
		return eqURI(edge.getType().getURI(), URIs.v().calls());
	}

	public boolean isAppToLibEdge(GXLEdge edge) {
		return eqURI(edge.getType().getURI(), URIs.v().callsLibrary());
	}

	public boolean isLibToAppEdge(GXLEdge edge) {
		return eqURI(edge.getType().getURI(), URIs.v().libraryCallsBack());
	}

	private void getGraph(InputStream file, String graphName) {
		gxlDocument = null;
		try {
			gxlDocument = new GXLDocument(file);
		} catch (Exception e) {
			throw new RuntimeException("Caught exception in parsing: " + e);
		}
		graph = (GXLGraph) gxlDocument.getElement(graphName);
	}

	private void readNodesEdges() {
		for (int i = 0; i < graph.getGraphElementCount(); i++) {
			GXLElement elem = graph.getGraphElementAt(i);
			if (elem instanceof GXLNode) {
				nodes.add((GXLNode) elem);
			} else if (elem instanceof GXLEdge) {
				edges.add((GXLEdge) elem);
			} else {
				throw new RuntimeException("unrecognized graph element " + elem);
			}
		}
	}

	private void sortNodes() {
		for (GXLNode node : nodes) {
			if (eqURI(node.getType().getURI(), URIs.v().uRoot()) || eqURI(node.getType().getURI(), URIs.v().uLibrary())) {
				// do nothing
			} else if (eqURI(node.getType().getURI(), URIs.v().uClass())) {
				classes.add(node);
			} else if (eqURI(node.getType().getURI(), URIs.v().uMethod())) {
				methods.add(node);
			} else {
				throw new RuntimeException("unrecognized node " + node + "; its id is " + node.getID());
			}
		}
	}

	private boolean eqURI(java.net.URI u1, java.net.URI u2) {
		return u1.getFragment().equals(u2.getFragment());
	}

	private void sortEdges() {
		for (GXLEdge edge : edges) {
			GXLNode src = (GXLNode) edge.getSource();
			GXLNode dst = (GXLNode) edge.getTarget();
			if (eqURI(edge.getType().getURI(), URIs.v().declaredIn())) {
				declaredIn.put(src, dst);
			} else if (eqURI(edge.getType().getURI(), URIs.v().entryPoint())) {
				entryPoints.add(dst);
			} else if (eqURI(edge.getType().getURI(), URIs.v().calls())
					|| eqURI(edge.getType().getURI(), URIs.v().callsLibrary())
					|| eqURI(edge.getType().getURI(), URIs.v().libraryCallsBack())) {
				callEdges.add(edge);
			} else {
				throw new RuntimeException("unrecognized edge " + edge + "; its id is " + edge.getID());
			}
		}
	}

	private Map<GXLNode, ProbeClass> nodeToClass = new HashMap<GXLNode, ProbeClass>();
	private Map<GXLNode, ProbeMethod> nodeToMethod = new HashMap<GXLNode, ProbeMethod>();

	private void createNodeMaps() {
		for (GXLNode node : classes) {
			nodeToClass.put(node, ObjectManager.v().getClass(getString(node, "package"), getString(node, "name")));
		}

		for (GXLNode node : methods) {
			GXLNode classNode = (GXLNode) declaredIn.get(node);
			nodeToMethod.put(
					node,
					ObjectManager.v().getMethod((ProbeClass) nodeToClass.get(classNode), getString(node, "name"),
							getString(node, "signature")));
		}
	}

	private String getString(GXLAttributedElement elem, String key) {
		GXLAttr attr = elem.getAttr(key);
		GXLAtomicValue value = (GXLAtomicValue) attr.getValue();
		return value.getValue();
	}
}