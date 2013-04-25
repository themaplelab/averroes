package ca.uwaterloo.averroes.dot;

public enum NodeShape {
	BOX("box"), CIRCLE("circle"), DOUBLE_CIRCLE("doublecircle");

	String value;

	NodeShape(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}
}
