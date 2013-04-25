package ca.uwaterloo.averroes.dot;

public class CallsEdge extends Edge {

	public CallsEdge(MethodNode src, MethodNode tgt) {
		super("calls", src, tgt);
	}
}
