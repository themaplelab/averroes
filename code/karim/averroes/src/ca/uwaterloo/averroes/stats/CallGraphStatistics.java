package ca.uwaterloo.averroes.stats;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uwaterloo.averroes.util.ArrayUtils;
import ca.uwaterloo.averroes.util.io.FileUtils;

public class CallGraphStatistics {

	static String year = "2013";
	static String day = "may06-no-reflection";
	static List<String> dacapo = Arrays.asList("antlr", "bloat", "chart", "hsqldb", "luindex", "lusearch", "pmd",
			"xalan");
	static List<String> specjvm = Arrays.asList("compress", "db", "jack", "javac", "jess", "raytrace");
	static List<String> benchmarks = new ArrayList<String>();
	static List<String> lines = new ArrayList<String>();

	static String basedir = FileUtils.composePath("dumps", year, day, "comparisons-call-graphs");
	static String line; // current line

	public static void main(String[] args) {
		try {
			populateAllBenchmarks();

			// Call graph edges count
			CallEdgeCount cgeCount = new CallEdgeCount();
			CallEdgeCount lcgeCount = new CallEdgeCount();
			CallEdgeCount lcbeCount = new CallEdgeCount();

			// Call graph edges comparisons
			CallEdgeComparison cgeComparison = new CallEdgeComparison();
			CallEdgeComparison lcgeComparison = new CallEdgeComparison();
			CallEdgeComparison lcbeComparison = new CallEdgeComparison();

			// Library callbacks frequencies
			LibraryCallBackFrequency doopAverroes_cgc = new LibraryCallBackFrequency();

			for (String benchmark : benchmarks) {
				String dir = FileUtils.composePath(basedir, benchmark);
				String program = benchmark.substring(benchmark.indexOf('/') + 1);

				// The averroes files
				BufferedReader stats = new BufferedReader(new FileReader(FileUtils.composePath(dir, program + "-comparisons.stats")));

				// edge counts
				addEdgesCount(stats, cgeCount);
				addEdgesCount(stats, lcgeCount);
				addEdgesCount(stats, lcbeCount);

				// edge comparisons
				addEdgesComparison(stats, cgeComparison);
				addEdgesComparison(stats, lcgeComparison);
				addEdgesComparison(stats, lcbeComparison);

				// read callbacks frequencies for cgc vs doop only
				readLibraryCallBacksFrequency(stats, program, doopAverroes_cgc);

				stats.close();

			}

			System.out.format("                    " + getFormat(), ArrayUtils.concat(dacapo, specjvm).toArray());
			System.out.format("                    " + getFormat(), lines.toArray());
			System.out.println("Call Graph Counts");
			System.out.println("-----------------");
			System.out.println("CGE");
			System.out.println("---");
			System.out.format("Spark               " + getFormat(), cgeCount.spark().toArray());
			System.out.format("SparkAve            " + getFormat(), cgeCount.sparkAverroes().toArray());
			System.out.format("Doop                " + getFormat(), cgeCount.doop().toArray());
			System.out.format("DoopAve             " + getFormat(), cgeCount.doopAverroes().toArray());
			System.out.println("");

			System.out.println("LCGE");
			System.out.println("----");
			System.out.format("Spark               " + getFormat(), lcgeCount.spark().toArray());
			System.out.format("SparkAve            " + getFormat(), lcgeCount.sparkAverroes().toArray());
			System.out.format("Doop                " + getFormat(), lcgeCount.doop().toArray());
			System.out.format("DoopAve             " + getFormat(), lcgeCount.doopAverroes().toArray());
			System.out.println("");

			System.out.println("LCBE");
			System.out.println("----");
			System.out.format("Spark               " + getFormat(), lcbeCount.spark().toArray());
			System.out.format("SparkAve            " + getFormat(), lcbeCount.sparkAverroes().toArray());
			System.out.format("Doop                " + getFormat(), lcbeCount.doop().toArray());
			System.out.format("DoopAve             " + getFormat(), lcbeCount.doopAverroes().toArray());
			System.out.println("");
			System.out.println("");

			System.out.println("Call Graph Soundness");
			System.out.println("--------------------");
			System.out.println("CGE");
			System.out.println("---");
			System.out.format("Dyn - Spark         " + getFormat(), cgeComparison.dynSpark().toArray());
			System.out.format("Dyn - SparkAve      " + getFormat(), cgeComparison.dyn_SparkAverroes().toArray());
			System.out.format("Dyn - Doop          " + getFormat(), cgeComparison.dynDoop().toArray());
			System.out.format("Dyn - DoopAve       " + getFormat(), cgeComparison.dyn_DoopAverroes().toArray());
			System.out.println("");

			System.out.println("LCGE");
			System.out.println("----");
			System.out.format("Dyn - Spark         " + getFormat(), lcgeComparison.dynSpark().toArray());
			System.out.format("Dyn - SparkAve      " + getFormat(), lcgeComparison.dyn_SparkAverroes().toArray());
			System.out.format("Dyn - Doop          " + getFormat(), lcgeComparison.dynDoop().toArray());
			System.out.format("Dyn - DoopAve       " + getFormat(), lcgeComparison.dyn_DoopAverroes().toArray());
			System.out.println("");

			System.out.println("LCBE");
			System.out.println("----");
			System.out.format("Dyn - Spark         " + getFormat(), lcbeComparison.dynSpark().toArray());
			System.out.format("Dyn - SparkAve      " + getFormat(), lcbeComparison.dyn_SparkAverroes().toArray());
			System.out.format("Dyn - Doop          " + getFormat(), lcbeComparison.dynDoop().toArray());
			System.out.format("Dyn - DoopAve       " + getFormat(), lcbeComparison.dyn_DoopAverroes().toArray());
			System.out.println("");
			System.out.println("");

			System.out.println("Call Graph Preicision");
			System.out.println("---------------------");
			System.out.println("CGE");
			System.out.println("---");
			System.out.format("SparkAve - Spark    " + getFormat(), cgeComparison.sparkAverroes_Spark().toArray());
			System.out.format("DoopAve - Doop      " + getFormat(), cgeComparison.doopAverroes_Doop().toArray());
			System.out.println("");

			System.out.println("LCGE");
			System.out.println("----");
			System.out.format("SparkAve - Spark    " + getFormat(), lcgeComparison.sparkAverroes_Spark().toArray());
			System.out.format("DoopAve - Doop      " + getFormat(), lcgeComparison.doopAverroes_Doop().toArray());
			System.out.println("");

			System.out.println("LCBE");
			System.out.println("----");
			System.out.format("SparkAve - Spark    " + getFormat(), lcbeComparison.sparkAverroes_Spark().toArray());
			System.out.format("DoopAve - Doop      " + getFormat(), lcbeComparison.doopAverroes_Doop().toArray());
			System.out.println("");
			System.out.println("");

			System.out.println("Cgc Vs. Averroes");
			System.out.println("----------------");
			System.out.println("CGE");
			System.out.println("---");
			System.out.format("Cgc - DoopAve       " + getFormat(), cgeComparison.cgc_DoopAverroes().toArray());
			System.out.format("DoopAve - Cgc       " + getFormat(), cgeComparison.doopAverroes_Cgc().toArray());
			System.out.println("");

			System.out.println("LCGE");
			System.out.println("----");
			System.out.format("Cgc - DoopAve       " + getFormat(), lcgeComparison.cgc_DoopAverroes().toArray());
			System.out.format("DoopAve - Cgc       " + getFormat(), lcgeComparison.doopAverroes_Cgc().toArray());
			System.out.println("");

			System.out.println("LCBE");
			System.out.println("----");
			System.out.format("Cgc - DoopAve       " + getFormat(), lcbeComparison.cgc_DoopAverroes().toArray());
			System.out.format("DoopAve - Cgc       " + getFormat(), lcbeComparison.doopAverroes_Cgc().toArray());
			System.out.println("");

			System.out.println("Most Frequent LCBE");
			System.out.println("------------------");
			for (String name : doopAverroes_cgc.frequency().keySet()) {
				System.out.println(name + "\t" + cleanFrequencies(doopAverroes_cgc.frequency().get(name).values()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static String cleanFrequencies(Collection<String> freqs) {
		return freqs.toString().replace("[", "").replace("]", "").replace(',', '\t');
	}

	private static void readLibraryCallBacksFrequency(BufferedReader in, String program,
			LibraryCallBackFrequency doopAverroes_cgc) throws IOException {
		readUntilLine(in, "DoopAverroes - Cgc", true);
		while ((line = in.readLine()) != null && !line.equals("")) {
			// Only read when the set is not empty
			if (!line.equals("<EMPTY>")) {
				// Remove spaces, split on the equal sign. First string is
				// method name, second is count
				String name = line.replace(" ", "").split("=")[0];
				String count = line.replace(" ", "").split("=")[1];
				doopAverroes_cgc.put(name, program, count);
			}
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

	private static void readUntilLine(BufferedReader in, String str, boolean consumeNextLine) throws IOException {
		while ((line = in.readLine()) != null && !line.equals(str))
			;
		if (consumeNextLine) {
			if (in.ready()) {
				line = in.readLine();
			}
		}
	}
	
	private static void readUntilLineStartsWith(BufferedReader in, String str) throws IOException {
		while ((line = in.readLine()) != null && !line.startsWith(str))
			;
	}

	private static void addEdgesCount(BufferedReader in, CallEdgeCount callEdgeount) throws IOException {
		readUntilLineStartsWith(in, "Spark: ");
		callEdgeount.spark().add(extractNumber());
		readUntilLineStartsWith(in, "SparkAverroes: ");
		callEdgeount.sparkAverroes().add(extractNumber());
		readUntilLineStartsWith(in, "Doop: ");
		callEdgeount.doop().add(extractNumber());
		readUntilLineStartsWith(in, "DoopAverroes: ");
		callEdgeount.doopAverroes().add(extractNumber());
	}

	private static void addEdgesComparison(BufferedReader in, CallEdgeComparison callEdgeComparison) throws IOException {
		readUntilLineStartsWith(in, "Dyn - Spark: ");
		callEdgeComparison.dynSpark().add(extractNumber());
		readUntilLineStartsWith(in, "Dyn - SparkAverroes: ");
		callEdgeComparison.dyn_SparkAverroes().add(extractNumber());
		readUntilLineStartsWith(in, "Dyn - Doop: ");
		callEdgeComparison.dynDoop().add(extractNumber());
		readUntilLineStartsWith(in, "Dyn - DoopAverroes: ");
		callEdgeComparison.dyn_DoopAverroes().add(extractNumber());
		readUntilLineStartsWith(in, "Spark - SparkAverroes: ");
		callEdgeComparison.spark_SparkAverroes().add(extractNumber());
		readUntilLineStartsWith(in, "Doop - DoopAverroes: ");
		callEdgeComparison.doop_DoopAverroes().add(extractNumber());
		readUntilLineStartsWith(in, "SparkAverroes - Spark: ");
		callEdgeComparison.sparkAverroes_Spark().add(extractNumber());
		readUntilLineStartsWith(in, "DoopAverroes - Doop: ");
		callEdgeComparison.doopAverroes_Doop().add(extractNumber());
		readUntilLineStartsWith(in, "Cgc - DoopAverroes: ");
		callEdgeComparison.cgc_DoopAverroes().add(extractNumber());
		readUntilLineStartsWith(in, "DoopAverroes - Cgc: ");
		callEdgeComparison.doopAverroes_Cgc().add(extractNumber());
	}

	private static String extractNumber() throws IOException {
		String regex = "\\d+(.\\d+)?";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line);

		// It should only match one number anyways, so no need to loop here
		if (matcher.find()) {
			return matcher.group();
		} else {
			return "";
		}
	}
}
