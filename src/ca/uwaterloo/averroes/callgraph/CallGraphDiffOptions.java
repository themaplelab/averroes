package ca.uwaterloo.averroes.callgraph;

public class CallGraphDiffOptions {

	private boolean diffEntryPoints;
	private boolean diffAppToAppEdges;
	private boolean diffAppToLibEdges;
	private boolean diffLibToAppEdges;
	
	public static CallGraphDiffOptions defaultOptions = new CallGraphDiffOptions();
	public static CallGraphDiffOptions soundnessOptions = getSoundnessOptions();
	public static CallGraphDiffOptions precisionOptions = getPrecisionOptions();

	public CallGraphDiffOptions() {
		setDiffAll();
	}
	
	private static CallGraphDiffOptions getSoundnessOptions() {
		CallGraphDiffOptions options = new CallGraphDiffOptions();
		options.setSoundnessOptions();
		return options;
	}
	
	private static CallGraphDiffOptions getPrecisionOptions() {
		CallGraphDiffOptions options = new CallGraphDiffOptions();
		options.setPrecisionOptions();
		return options;
	}

	public void setDiffEntryPoints(boolean setting) {
		diffEntryPoints = setting;
	}

	public void setDiffAppToAppEdges(boolean setting) {
		diffAppToAppEdges = setting;
	}

	public void setDiffAppToLibEdges(boolean setting) {
		diffAppToLibEdges = setting;
	}

	public void setDiffLibToAppEdges(boolean setting) {
		diffLibToAppEdges = setting;
	}

	public void setDiffAll() {
		setDiffEntryPoints(true);
		setDiffAppToAppEdges(true);
		setDiffAppToLibEdges(true);
		setDiffLibToAppEdges(true);
	}

	public void setDiffNone() {
		setDiffEntryPoints(false);
		setDiffAppToAppEdges(false);
		setDiffAppToLibEdges(false);
		setDiffLibToAppEdges(false);
	}

	public void reset() {
		setDiffNone();
	}

	public void setSoundnessOptions() {
		setDiffNone();
		setDiffAppToAppEdges(true);
		setDiffAppToLibEdges(true);
	}

	public void setPrecisionOptions() {
		setDiffNone();
		setDiffLibToAppEdges(true);
	}

	public boolean isSetSoundnessOptions() {
		return isDiffAppToAppEdges() && isDiffAppToLibEdges();
	}

	public boolean isSetPrecisionOptions() {
		return isDiffLibToAppEdges();
	}

	public boolean isDiffEntryPoints() {
		return diffEntryPoints;
	}

	public boolean isDiffAppToAppEdges() {
		return diffAppToAppEdges;
	}

	public boolean isDiffAppToLibEdges() {
		return diffAppToLibEdges;
	}

	public boolean isDiffLibToAppEdges() {
		return diffLibToAppEdges;
	}
}