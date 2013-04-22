package ca.uwaterloo.averroes.stats;

import java.text.DecimalFormat;

import ca.uwaterloo.averroes.callgraph.CallGraph;
import ca.uwaterloo.averroes.callgraph.CallGraphSource;

/**
 * A class that holds the statistical comparisons of two call graphs. The comparisons are soundness ({@value
 * getMissing()} and precision ({@value #getExtra()}.
 * 
 * @author karim
 * 
 */
public class Statistics {
	private CallGraph sub;
	private CallGraph sup;

	private CallGraph missing;
	private CallGraph extra;

	public static DecimalFormat formatter = new DecimalFormat("#0.00%");

	public Statistics(CallGraph sub, CallGraph sup, CallGraph missing, CallGraph extra) {
		this.sub = sub;
		this.sup = sup;
		this.missing = missing;
		this.extra = extra;
	}

	public void print() {
		CallGraphSource a = sub.getSource();
		CallGraphSource b = sup.getSource();

		System.out.println("Comparisons (" + a + "/" + b + ")");
		System.out.println("----------------------");
		System.out.print("Application call graph edges:\t\t");
		System.out.print(a + " = " + sub.appToAppEdges().size() + ",\t");
		System.out.print(b + " = " + sup.appToAppEdges().size() + ",\t");
		System.out.print("missing = " + missing.appToAppEdges().size() + ",\t");
		System.out.println("extra = " + extra.appToAppEdges().size());
		System.out.print("Library call graph edges:\t");
		System.out.print(a + " = " + sub.appToLibEdges().size() + ",\t");
		System.out.print(b + " = " + sup.appToLibEdges().size() + ",\t");
		System.out.print("missing = " + missing.appToLibEdges().size() + ",\t");
		System.out.println("extra = " + extra.appToLibEdges().size());
		System.out.print("Library call back edges:\t");
		System.out.print(a + " = " + sub.libToAppEdges().size() + ",\t");
		System.out.print(b + " = " + sup.libToAppEdges().size() + ",\t");
		System.out.print("missing = " + missing.libToAppEdges().size() + ",\t");
		System.out.println("extra = " + extra.libToAppEdges().size());

		System.out.println("Library call back edges frequencies:");
		System.out.println("Missing:");
		missing.printFrequencies();
		System.out.println("Extra:");
		extra.printFrequencies();

		System.out.println("");
	}

	public CallGraph getMissing() {
		return missing;
	}

	public CallGraph getExtra() {
		return extra;
	}

	public static double error(double x, double y) {
		return (x - y) / y;
	}

	public static String percentageChange(double x, double y) {
		return formatter.format(error(x, y));
	}

	public static String percent(double x, double y) {
		return formatter.format(x / y);
	}
}