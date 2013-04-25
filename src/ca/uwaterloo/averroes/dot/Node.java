package ca.uwaterloo.averroes.dot;

public class Node {

	protected String id;
	protected String title;
	protected NodeShape shape;

	public Node(String id, NodeShape shape) {
		this.id = id;
		this.shape = shape;
	}

	public Node(String id) {
		this(id, NodeShape.BOX);
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();

		ret.append(id + "[ ");
		ret.append("shape=\"" + shape.value() + "\", ");
		ret.append("fontname=\"Helvetica\", ");
		ret.append("label=\"" + title + "\"");
		ret.append(" ];\n");

		return ret.toString();
	}
}
