package ca.uwaterloo.averroes.dot;

public enum Format {
	PNG("png", "-Tpng"), PS("ps", "-Tps"), DOT("dot", "");

	private String format;
	private String command;

	Format(String format, String command) {
		this.format = format;
		this.command = command;
	}

	public String format() {
		return format;
	}

	public String command() {
		return command;
	}

}
