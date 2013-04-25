package ca.uwaterloo.averroes.dot;

public class LibraryCallsBackEdge extends Edge {

	public LibraryCallsBackEdge(LibraryNode src, MethodNode tgt) {
		super("calls", src, tgt);
	}
}
