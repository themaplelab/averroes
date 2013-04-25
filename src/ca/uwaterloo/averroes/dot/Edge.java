package ca.uwaterloo.averroes.dot;

public class Edge {

	protected String name;
	protected Node src;
	protected Node tgt;

	public Edge(String name, Node src, Node tgt) {
		this.name = name;
		this.src = src;
		this.tgt = tgt;
	}

	public String getId() {
		return src.getId() + tgt.getId();
	}

	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();

		ret.append(src.getId() + " -> " + tgt.getId());
		ret.append("[ fontname=\"Helvetica\", ");
		ret.append("label=\"" + name + "\" ");
		ret.append(", arrowhead=open, ];\n");

		return ret.toString();
	}
}
