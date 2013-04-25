package ca.uwaterloo.averroes.dot;

public class EntryPointEdge extends Edge {

	public EntryPointEdge(RootNode src, MethodNode tgt) {
		super("entryPoint", src, tgt);
	}
}
