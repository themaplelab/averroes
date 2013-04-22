package ca.uwaterloo.averroes.callgraph.gxl;

import net.sourceforge.gxl.GXLNode;

/**
 * A factor for GXL nodes.
 * 
 * @author karim
 * 
 */
public class GXLNodeFactory {

	public static String ROOT_NODE_ID = "root";
	public static String LIBRARY_NODE_ID = "library";

	public static GXLNode rootNode() {
		GXLNode root = new GXLNode(ROOT_NODE_ID);
		root.setType(URIs.v().uRoot());
		return root;
	}

	public static GXLNode libraryNode() {
		GXLNode library = new GXLNode(LIBRARY_NODE_ID);
		library.setType(URIs.v().uLibrary());
		return library;
	}
}
