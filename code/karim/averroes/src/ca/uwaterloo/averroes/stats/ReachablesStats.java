package ca.uwaterloo.averroes.stats;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import probe.ProbeMethod;
import ca.uwaterloo.averroes.callgraph.CallGraph;
import ca.uwaterloo.averroes.callgraph.CallGraphSource;
import ca.uwaterloo.averroes.callgraph.gxl.GXLReader;
import ca.uwaterloo.averroes.util.ArrayUtils;
import ca.uwaterloo.averroes.util.SetUtils;
import ca.uwaterloo.averroes.util.io.FileUtils;

public class ReachablesStats {

	static String year = "2013";
	static String day = "may06-no-reflection";
	static List<String> dacapo = Arrays.asList("antlr", "bloat", "chart", "hsqldb", "luindex", "lusearch", "pmd",
			"xalan");
	static List<String> specjvm = Arrays.asList("compress", "db", "jack", "javac", "jess", "raytrace");
	static List<String> benchmarks = new ArrayList<String>();
	static List<String> lines = new ArrayList<String>();

	static String basedir = FileUtils.composePath("dumps", year, day, "spark-averroes-call-graphs");
	static String line; // current line

	public static void main(String[] args) {
		try {
			populateAllBenchmarks();

			// Call graph edges count
			ReachableMethodsCount reachables = new ReachableMethodsCount();
			ReachableMethodsComparison reachablesComp = new ReachableMethodsComparison();

			for (String benchmark : benchmarks) {
				String dir = FileUtils.composePath(basedir, benchmark);
				String cgNoReflection = FileUtils.composePath(dir, "sparkAverroes.gxl");
				String cgReflection = FileUtils.composePath(dir.replace("may06-no", "april11-with"),
						"sparkAverroes.gxl");
				String cgDynamic = FileUtils.composePath("callgraphs", "dynamic-call-graphs", benchmark, "dynamic.gxl");

				// The averroes files
				CallGraph sparkNoReflection = new GXLReader().readCallGraph(new FileInputStream(cgNoReflection),
						CallGraphSource.SPARK_AVERROES);
				CallGraph sparkReflection = new GXLReader().readCallGraph(new FileInputStream(cgReflection),
						CallGraphSource.SPARK_AVERROES);
				CallGraph dynamic = new GXLReader().readCallGraph(new FileInputStream(cgDynamic),
						CallGraphSource.DYNAMIC);

				Set<ProbeMethod> reachablesDynamic = dynamic.findReachables();
				Set<ProbeMethod> reachablesWithReflection = sparkReflection.findReachables();
				Set<ProbeMethod> reachablesNoReflection = sparkNoReflection.findReachables();

				reachables.dynamic().add(reachablesDynamic.size());
				reachables.sparkAverroes().add(reachablesWithReflection.size());
				reachables.spark().add(reachablesNoReflection.size());

				reachablesComp.dyn_SparkAverroes().add(
						SetUtils.minus(reachablesDynamic, reachablesWithReflection).size());
				reachablesComp.dynSpark().add(
						SetUtils.minus(reachablesDynamic, reachablesNoReflection).size());
			}

			System.out.format("                    " + getFormat(), ArrayUtils.concat(dacapo, specjvm).toArray());
			System.out.format("                    " + getFormat(), lines.toArray());
			System.out.println("Reachables Counts");
			System.out.println("-------------------");
			System.out.format("Dynamic             " + getFormat(), reachables.dynamic().toArray());
			System.out.format("SparkAve            " + getFormat(), reachables.sparkAverroes().toArray());
			System.out.format("SparkAveNR          " + getFormat(), reachables.spark().toArray());
			System.out.println("");
			System.out.println("");
			
			System.out.println("Reachables Comps");
			System.out.println("-------------------");
			System.out.format("Dyn - SparkAve      " + getFormat(), reachablesComp.dyn_SparkAverroes().toArray());
			System.out.format("Dyn - SparkAven     " + getFormat(), reachablesComp.dynSpark().toArray());
			System.out.println("");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void populateAllBenchmarks() {
		for (String d : dacapo) {
			benchmarks.add(FileUtils.composePath("dacapo", d));
			lines.add(getUnderline(d));
		}

		for (String s : specjvm) {
			benchmarks.add(FileUtils.composePath("specjvm", s));
			lines.add(getUnderline(s));
		}
	}

	private static String getFormat() {
		String format = "%10s";
		StringBuilder strBuilder = new StringBuilder();
		for (int i = 0; i < dacapo.size() + specjvm.size(); i++) {
			strBuilder.append(format);
		}
		strBuilder.append("%n");
		return strBuilder.toString();
	}

	private static String getUnderline(String str) {
		char c = '=';
		char[] repeat = new char[str.length()];
		Arrays.fill(repeat, c);
		return String.valueOf(repeat);
	}
}
