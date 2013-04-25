package ca.uwaterloo.averroes.callgraph.gxl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.gxl.GXLAttributedElement;
import net.sourceforge.gxl.GXLDocument;
import net.sourceforge.gxl.GXLEdge;
import net.sourceforge.gxl.GXLElement;
import net.sourceforge.gxl.GXLGraph;
import net.sourceforge.gxl.GXLNode;
import net.sourceforge.gxl.GXLString;
import net.sourceforge.gxl.GXLTypedElement;

import org.xml.sax.SAXException;

import ca.uwaterloo.averroes.dot.CallsEdge;
import ca.uwaterloo.averroes.dot.CallsLibraryEdge;
import ca.uwaterloo.averroes.dot.Edge;
import ca.uwaterloo.averroes.dot.EntryPointEdge;
import ca.uwaterloo.averroes.dot.Format;
import ca.uwaterloo.averroes.dot.LibraryCallsBackEdge;
import ca.uwaterloo.averroes.dot.LibraryNode;
import ca.uwaterloo.averroes.dot.MethodNode;
import ca.uwaterloo.averroes.dot.Node;
import ca.uwaterloo.averroes.dot.RootNode;

/** Converts GXL schema to a graphical representation in dot. */

public class GXLConverter {

	private static Map<String, Node> nodes;
	private static Map<String, Edge> edges;

	public static void dotify(String gxlFileName) throws IOException, SAXException {
		GXLDocument doc = new GXLDocument(new File(gxlFileName));
		GXLGraph graph = doc.getDocumentElement().getGraphAt(0);

		String dotGraphFile = gxlFileName.replace(".gxl", ".dot");

		// Clear all the maps
		initializeMaps();

		// Get nodes information
		for (int i = 0; i < graph.getGraphElementCount(); i++) {
			GXLElement elem = graph.getGraphElementAt(i);
			if (elem instanceof GXLNode) {
				GXLNode node = (GXLNode) elem;
				if (type(node, URIs.v().uCallGraph())) {
					// Nothing
				} else if (type(node, URIs.v().uRoot())) {
					// Add a new root node to the map of nodes
					rootNode(node);
				} else if (type(node, URIs.v().uLibrary())) {
					libraryNode(node);
				} else if (type(node, URIs.v().uClass())) {
					// Ignore
				} else if (type(node, URIs.v().uMethod())) {
					// Add a new method node to the map of nodes
					methodNode(node);
				} else {
					throw new RuntimeException("Unknown type: " + node.getType().getURI());
				}
			}
		}

		// Get edges information
		for (int i = 0; i < graph.getGraphElementCount(); i++) {
			GXLElement elem = graph.getGraphElementAt(i);
			if (elem instanceof GXLEdge) {
				GXLEdge edge = (GXLEdge) elem;
				if (type(edge, URIs.v().declaredIn())) {
					// Ignore
				} else if (type(edge, URIs.v().entryPoint())) {
					entryPointEdge(edge);
				} else if (type(edge, URIs.v().calls())) {
					callsEdge(edge);
				} else if (type(edge, URIs.v().callsLibrary())) {
					callsLibraryEdge(edge);
				} else if (type(edge, URIs.v().libraryCallsBack())) {
					libraryCallsBackEdge(edge);
				} else {
					throw new RuntimeException("Unknown type: " + edge.getType().getURI());
				}
			}
		}

		// Write this to the dot file
		StringBuffer dot = new StringBuffer();
		dot.append("digraph G {" + "\n");
		for (String nodeId : nodes.keySet()) {
			dot.append(nodes.get(nodeId) + "\n");
		}
		for (String edgeId : edges.keySet()) {
			dot.append(edges.get(edgeId) + "\n");
		}
		dot.append("};" + "\n");

		FileWriter writer = new FileWriter(new File(dotGraphFile));
		writer.write(dot.toString());
		writer.flush();
		writer.close();
	}

	public static void convert(String gxlFileName, Format format) throws IOException, SAXException {
		String dotGraphFile = gxlFileName.replace(".gxl", ".dot");
		String psGraphFile = gxlFileName.replace(".gxl", ".ps");
		String pngGraphFile = gxlFileName.replace(".gxl", ".png");

		// Do we need to generate a png or ps ?
		if (format.equals(Format.PNG)) {
			Runtime.getRuntime()
					.exec(new String[] { dotCommand(), format.command(), dotGraphFile, "-o", pngGraphFile });
		} else if (format.equals(Format.PS)) {
			Runtime.getRuntime().exec(new String[] { dotCommand(), format.command(), dotGraphFile, "-o", psGraphFile });
		}
	}

	public static void probeGxl2Dot(String gxlFileName, Format format) throws IOException, SAXException {
		GXLDocument doc = new GXLDocument(new File(gxlFileName));
		GXLGraph graph = doc.getDocumentElement().getGraphAt(0);
		probe.URIs probeURI = new probe.URIs("/~olhotak/probe/schemas/callgraph.gxl");

		String dotGraphFile = gxlFileName.replace(".gxl", ".dot");
		String psGraphFile = gxlFileName.replace(".gxl", ".ps");
		String pngGraphFile = gxlFileName.replace(".gxl", ".png");

		// Clear all the maps
		initializeMaps();

		// Get nodes information
		for (int i = 0; i < graph.getGraphElementCount(); i++) {
			GXLElement elem = graph.getGraphElementAt(i);
			if (elem instanceof GXLNode) {
				GXLNode node = (GXLNode) elem;
				if (type(node, probeURI.uCallGraph())) {
					// Nothing
				} else if (type(node, probeURI.uRoot())) {
					// Add a new root node to the map of nodes
					rootNode(node);
				} else if (type(node, probeURI.uClass())) {
					// Ignore
				} else if (type(node, probeURI.uMethod())) {
					// Add a new method node to the map of nodes
					methodNode(node);
				} else {
					throw new RuntimeException("Unknown type: " + node.getType().getURI());
				}
			}
		}

		// Get edges information
		for (int i = 0; i < graph.getGraphElementCount(); i++) {
			GXLElement elem = graph.getGraphElementAt(i);
			if (elem instanceof GXLEdge) {
				GXLEdge edge = (GXLEdge) elem;
				if (type(edge, probeURI.declaredIn())) {
					// Ignore
				} else if (type(edge, probeURI.entryPoint())) {
					entryPointEdge(edge);
				} else if (type(edge, probeURI.calls())) {
					callsEdge(edge);
				} else {
					throw new RuntimeException("Unknown type: " + edge.getType().getURI());
				}
			}
		}

		// Write this to the dot file
		StringBuffer dot = new StringBuffer();
		dot.append("digraph G {" + "\n");
		for (String nodeId : nodes.keySet()) {
			dot.append(nodes.get(nodeId) + "\n");
		}
		for (String edgeId : edges.keySet()) {
			dot.append(edges.get(edgeId) + "\n");
		}
		dot.append("};" + "\n");
		FileWriter writer = new FileWriter(new File(dotGraphFile));
		writer.write(dot.toString());
		writer.close();

		// Do we need to generate a png or ps ?
		if (!format.equals(Format.PNG)) {
			Runtime.getRuntime().exec(
					new String[] { dotCommand(), "-Ksfpd", format.command(), dotGraphFile, "-o", pngGraphFile });
		} else if (format.equals(Format.PS)) {
			Runtime.getRuntime().exec(
					new String[] { dotCommand(), "-Ksfpd", format.command(), dotGraphFile, "-o", psGraphFile });
		}
	}

	public static String dotCommand() {
		// On Mac, /usr/local/bin is not in the path that Java uses.
		if (System.getProperty("os.name").equalsIgnoreCase("Mac OS X")) {
			return "/usr/local/bin/dot";
		} else {
			return "dot";
		}
	}

	private static void initializeMaps() {
		nodes = new HashMap<String, Node>();
		edges = new HashMap<String, Edge>();
	}

	private static RootNode rootNode(GXLNode node) {
		if (!nodes.containsKey(node.getID())) {
			RootNode rootNode = new RootNode(node.getID());
			nodes.put(node.getID(), rootNode);
		}

		return (RootNode) nodes.get(node.getID());
	}

	private static LibraryNode libraryNode(GXLNode node) {
		if (!nodes.containsKey(node.getID())) {
			LibraryNode libraryNode = new LibraryNode(node.getID());
			nodes.put(node.getID(), libraryNode);
		}

		return (LibraryNode) nodes.get(node.getID());
	}

	private static MethodNode methodNode(GXLNode node) {
		if (!nodes.containsKey(node.getID())) {
			String sig = getString(node, MethodNode.SIGNATURE_ATTR);
			MethodNode methodNode = new MethodNode(node.getID(), sig);
			nodes.put(node.getID(), methodNode);
		}

		return (MethodNode) nodes.get(node.getID());
	}

	private static EntryPointEdge entryPointEdge(GXLEdge edge) {
		String id = edge.getSourceID() + edge.getTargetID();

		if (!edges.containsKey(id)) {
			RootNode src = (RootNode) nodes.get(edge.getSourceID());
			MethodNode tgt = (MethodNode) nodes.get(edge.getTargetID());
			EntryPointEdge entryPointEdge = new EntryPointEdge(src, tgt);
			edges.put(id, entryPointEdge);
		}

		return (EntryPointEdge) edges.get(id);
	}

	private static CallsEdge callsEdge(GXLEdge edge) {
		String id = edge.getSourceID() + edge.getTargetID();

		if (!edges.containsKey(id)) {
			MethodNode src = (MethodNode) nodes.get(edge.getSourceID());
			MethodNode tgt = (MethodNode) nodes.get(edge.getTargetID());
			CallsEdge callsEdge = new CallsEdge(src, tgt);
			edges.put(id, callsEdge);
		}

		return (CallsEdge) edges.get(id);
	}

	private static CallsLibraryEdge callsLibraryEdge(GXLEdge edge) {
		String id = edge.getSourceID() + edge.getTargetID();

		if (!edges.containsKey(id)) {
			MethodNode src = (MethodNode) nodes.get(edge.getSourceID());
			LibraryNode tgt = (LibraryNode) nodes.get(edge.getTargetID());
			CallsLibraryEdge callsLibraryEdge = new CallsLibraryEdge(src, tgt);
			edges.put(id, callsLibraryEdge);
		}

		return (CallsLibraryEdge) edges.get(id);
	}

	private static LibraryCallsBackEdge libraryCallsBackEdge(GXLEdge edge) {
		String id = edge.getSourceID() + edge.getTargetID();

		if (!edges.containsKey(id)) {
			LibraryNode src = (LibraryNode) nodes.get(edge.getSourceID());
			MethodNode tgt = (MethodNode) nodes.get(edge.getTargetID());
			LibraryCallsBackEdge libraryCallsBackEdge = new LibraryCallsBackEdge(src, tgt);
			edges.put(id, libraryCallsBackEdge);
		}

		return (LibraryCallsBackEdge) edges.get(id);
	}

	private static boolean type(GXLTypedElement elem, URI uri) {
		return elem.getType().getURI().compareTo(uri) == 0;
	}

	private static String getString(GXLAttributedElement elem, String desiredAttr) {
		return ((GXLString) elem.getAttr(desiredAttr).getValue()).getValue();
	}
}
