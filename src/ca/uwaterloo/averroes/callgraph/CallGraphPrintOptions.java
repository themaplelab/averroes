package ca.uwaterloo.averroes.callgraph;

/**
 * Parts of the call graph can be printed individually. This class sets the options for a call graph to be printed out
 * when {@link CallGraph#print(CallGraphPrintOptions) is called.}
 * 
 * @author karim
 * 
 */
public class CallGraphPrintOptions {

	private boolean printEntryPoints;
	private boolean printAppToAppEdges;
	private boolean printAppToLibEdges;
	private boolean printLibToAppEdges;
	private boolean printConfinedLibraryMethods;

	public CallGraphPrintOptions() {
		setPrintAll();
	}

	public void setPrintEntryPoints(boolean setting) {
		printEntryPoints = setting;
	}

	public void setPrintAppToAppEdges(boolean setting) {
		printAppToAppEdges = setting;
	}

	public void setPrintAppToLibEdges(boolean setting) {
		printAppToLibEdges = setting;
	}

	public void setPrintLibToAppEdges(boolean setting) {
		printLibToAppEdges = setting;
	}
	
	public void setPrintAll() {
		setPrintEntryPoints(true);
		setPrintAppToAppEdges(true);
		setPrintAppToLibEdges(true);
		setPrintLibToAppEdges(true);
	}

	public void setPrintNone() {
		setPrintEntryPoints(false);
		setPrintAppToAppEdges(false);
		setPrintAppToLibEdges(false);
		setPrintLibToAppEdges(false);
	}

	public void reset() {
		setPrintNone();
	}

	public boolean isPrintEntryPoints() {
		return printEntryPoints;
	}

	public boolean isPrintAppToAppEdges() {
		return printAppToAppEdges;
	}

	public boolean isPrintAppToLibEdges() {
		return printAppToLibEdges;
	}

	public boolean isPrintLibToAppEdges() {
		return printLibToAppEdges;
	}

	public boolean isPrintConfinedLibraryMethods() {
		return printConfinedLibraryMethods;
	}
}