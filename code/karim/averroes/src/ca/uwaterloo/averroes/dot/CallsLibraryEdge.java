package ca.uwaterloo.averroes.dot;

public class CallsLibraryEdge extends Edge {

	public CallsLibraryEdge(MethodNode src, LibraryNode tgt) {
		super("calls", src, tgt);
	}
}
