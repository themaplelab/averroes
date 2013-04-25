package ca.uwaterloo.averroes.dot;

public class MethodNode extends Node {

	public static final String SIGNATURE_ATTR = "signature";

	public MethodNode(String id, String signature) {
		super(id);
		title = signature;
	}
}
