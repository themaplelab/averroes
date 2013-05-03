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
import ca.uwaterloo.averroes.util.MathUtils;
import ca.uwaterloo.averroes.util.io.FileUtils;

public class AverroesStatisticsGenerator {

	static String year = "2013";
	static String day = "may03-no-reflection";
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

			// Execution time
			ExecutionTime time = new ExecutionTime();

			// Disk usage
			DiskUsage disk = new DiskUsage();

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

			BufferedReader factory = new BufferedReader(new FileReader(FileUtils.composePath("averroes", "factory.stats")));
			BufferedReader organizer = new BufferedReader(new FileReader(FileUtils.composePath("averroes",
					"organizer.stats")));

			for (String benchmark : benchmarks) {
				String dir = FileUtils.composePath(basedir, benchmark);
				String sparkDir = FileUtils.composePath("dumps", "original-spark", benchmark);
				String mar22Dir = FileUtils.composePath("dumps", "2012", "mar22", benchmark);
				String program = benchmark.substring(benchmark.indexOf('/') + 1);

				// The stuff form march 22nd (i.e., original doop and original spark
				BufferedReader spark = new BufferedReader(new FileReader(FileUtils.composePath(sparkDir,
						"original-spark.stats")));
				BufferedReader sparkGc = new BufferedReader(new FileReader(FileUtils.composePath(sparkDir,
						"original-spark-gc.stats")));
				BufferedReader doop = new BufferedReader(
						new FileReader(FileUtils.composePath(mar22Dir, program + ".stats")));

				// The averroes files
				BufferedReader stats = new BufferedReader(new FileReader(FileUtils.composePath(dir, program + ".stats")));
				BufferedReader sparkAverroesGc = new BufferedReader(new FileReader(FileUtils.composePath(dir, program
						+ "-gc.stats")));

				// jar organizer
				readOrganizerStats(organizer, time, disk, benchmark);

				// jar creation
				readJarFactoryStats(factory, disk, time, benchmark);

				// verbose:gc memory consumption for SparkAverroes
				readSparkAverroesGcStats(sparkAverroesGc, disk);

				// verbose:gc memory consumption for Spark
				readSparkGcStats(sparkGc, disk);

				// SparkAverroes execution time
				readSparkAverroesTime(stats, time);

				// spark execution time
				readSparkTime(spark, time);

				// DoopAverroes execution time
				readDoopAverroesTime(stats, time);

				// Doop execution time
				readDoopTime(doop, time);

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

				// Statistics
				readDiskStats(stats, disk);
				readDoopDiskStats(doop, disk);

				stats.close();
				doop.close();
				sparkAverroesGc.close();
				spark.close();
				sparkGc.close();
			}
			factory.close();
			organizer.close();

			System.out.format("                    " + getFormat(), ArrayUtils.concat(dacapo, specjvm).toArray());
			System.out.format("                    " + getFormat(), lines.toArray());
			System.out.println("Analysis Time");
			System.out.println("-------------");
			System.out.format("Spark               " + getFormat(), time.spark.toArray());
			System.out.format("SparkAve            " + getFormat(), time.sparkAverroes.toArray());
			System.out.format("Doop                " + getFormat(), time.doop.toArray());
			System.out.format("DoopAve             " + getFormat(), time.doopAverroes.toArray());
			System.out.println("");
			
			System.out.println("Total Time");
			System.out.println("----------");
			System.out.format("Spark               " + getFormat(), time.sparkTotal.toArray());
			System.out.format("SparkAve            " + getFormat(), time.sparkAverroesTotal.toArray());
			System.out.format("Doop                " + getFormat(), time.doopTotal.toArray());
			System.out.format("DoopAve             " + getFormat(), time.doopAverroesTotal.toArray());
			System.out.println("");
			

			System.out.println("JarFactory");
			System.out.println("----------");
			System.out.format("Loading             " + getFormat(), time.sootLoading.toArray());
			System.out.format("Generating          " + getFormat(), time.jarFactory.toArray());
			System.out.format("Verification        " + getFormat(), time.jarVerification.toArray());
			System.out.format("Total               " + getFormat(), time.jarFactoryTotal.toArray());
			System.out.println("");

			System.out.println("Doop Disk Usage");
			System.out.println("---------------");
			System.out.format("Doop                " + getFormat(), disk.doop.toArray());
			System.out.format("DoopAve             " + getFormat(), disk.doopAverroes.toArray());
			System.out.println("");

			System.out.println("Spark Memory Usage");
			System.out.println("------------------");
			System.out.format("Spark               " + getFormat(), disk.spark.toArray());
			System.out.format("SparkAve            " + getFormat(), disk.sparkAverroes.toArray());
			System.out.println("");

			System.out.println("JAR Files");
			System.out.println("---------");
			System.out.format("Input App           " + getFormat(), disk.inputApplicationClassesSize.toArray());
			System.out.format("Input Lib           " + getFormat(), disk.inputLibraryClassesSize.toArray());
			System.out.format("Averroes            " + getFormat(), disk.finalLibraryClassesSize.toArray());
			System.out.println("");

			System.out.println("Classes");
			System.out.println("-------");
			System.out.format("Input App           " + getFormat(), disk.inputApplicationClasses.toArray());
			System.out.format("Original Lib        " + getFormat(), disk.originalLibraryClasses.toArray());
			System.out.format("Input Lib           " + getFormat(), disk.inputLibraryClasses.toArray());
			System.out.format("Generated Lib       " + getFormat(), disk.generatedLibraryClasses.toArray());
			System.out.println("");

			System.out.println("Library Methods");
			System.out.println("---------------");
			System.out.format("Input               " + getFormat(), disk.inputLibraryMethods.toArray());
			System.out.format("Referenced          " + getFormat(), disk.referencedLibraryMethods.toArray());
			System.out.format("Removed             " + getFormat(), disk.removedLibraryMethods.toArray());
			System.out.format("Processed           " + getFormat(), disk.libraryMethods.toArray());
			System.out.format("Generated           " + getFormat(), disk.generatedLibraryMethods.toArray());
			System.out.println("");

			System.out.println("Library Fields");
			System.out.println("--------------");
			System.out.format("Input               " + getFormat(), disk.inputLibraryFields.toArray());
			System.out.format("Referenced          " + getFormat(), disk.referencedLibraryFields.toArray());
			System.out.format("Removed             " + getFormat(), disk.removedLibraryFields.toArray());
			System.out.println("");

			System.out.println("Call Graph Counts");
			System.out.println("-----------------");
			System.out.println("CGE");
			System.out.println("---");
			System.out.format("SparkAve            " + getFormat(), cgeCount.sparkAverroes().toArray());
			System.out.format("DoopAve             " + getFormat(), cgeCount.doopAverroes().toArray());
			System.out.println("");

			System.out.println("LCGE");
			System.out.println("----");
			System.out.format("SparkAve            " + getFormat(), lcgeCount.sparkAverroes().toArray());
			System.out.format("DoopAve             " + getFormat(), lcgeCount.doopAverroes().toArray());
			System.out.println("");

			System.out.println("LCBE");
			System.out.println("----");
			System.out.format("SparkAve            " + getFormat(), lcbeCount.sparkAverroes().toArray());
			System.out.format("DoopAve             " + getFormat(), lcbeCount.doopAverroes().toArray());
			System.out.println("");
			System.out.println("");

			System.out.println("Call Graph Soundness");
			System.out.println("--------------------");
			System.out.println("CGE");
			System.out.println("---");
			System.out.format("Dyn - SparkAve      " + getFormat(), cgeComparison.dyn_SparkAverroes().toArray());
			System.out.format("Dyn - DoopAve       " + getFormat(), cgeComparison.dyn_DoopAverroes().toArray());
			System.out.println("");

			System.out.println("LCGE");
			System.out.println("----");
			System.out.format("Dyn - SparkAve      " + getFormat(), lcgeComparison.dyn_SparkAverroes().toArray());
			System.out.format("Dyn - DoopAve       " + getFormat(), lcgeComparison.dyn_DoopAverroes().toArray());
			System.out.println("");

			System.out.println("LCBE");
			System.out.println("----");
			System.out.format("Dyn - SparkAve      " + getFormat(), lcbeComparison.dyn_SparkAverroes().toArray());
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

			// System.out.format("Spark - SparkAve =  " + getFormat(), cgeComparison.spark_SparkAverroes().toArray());
			// System.out.format("Doop - DoopAve =    " + getFormat(), cgeComparison.doop_DoopAverroes().toArray());
			// System.out.format("Spark - SparkAve =  " + getFormat(), lcgeComparison.spark_SparkAverroes().toArray());
			// System.out.format("Doop - DoopAve =    " + getFormat(), lcgeComparison.doop_DoopAverroes().toArray());
			// System.out.format("Spark - SparkAve =  " + getFormat(), lcbeComparison.spark_SparkAverroes().toArray());
			// System.out.format("Doop - DoopAve =    " + getFormat(), lcbeComparison.doop_DoopAverroes().toArray());
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
		while ((line = in.readLine()) != null && !line.equals("Statistics ...")) {
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

	private static void readUntilLine(BufferedReader in, String str) throws IOException {
		readUntilLine(in, str, false);
	}

	private static void readUntilLineStartsWith(BufferedReader in, String str) throws IOException {
		while ((line = in.readLine()) != null && !line.startsWith(str))
			;
	}

	private static void addEdgesCount(BufferedReader in, CallEdgeCount callEdgeount) throws IOException {
		readUntilLineStartsWith(in, "SparkAverroes: ");
		callEdgeount.sparkAverroes().add(extractNumber());
		readUntilLineStartsWith(in, "DoopAverroes: ");
		callEdgeount.doopAverroes().add(extractNumber());
	}

	private static void addEdgesComparison(BufferedReader in, CallEdgeComparison callEdgeComparison) throws IOException {
		readUntilLineStartsWith(in, "Dyn - SparkAverroes: ");
		callEdgeComparison.dyn_SparkAverroes().add(extractNumber());
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

	private static void readDiskStats(BufferedReader in, DiskUsage disk) throws IOException {
		readUntilLine(in, "application: ", true);
		disk.inputApplicationClassesSize.add(extractSize());
		readUntilLine(in, "original library: ", true);
		disk.inputLibraryClassesSize.add(extractSize());
		readUntilLine(in, "averroes library: ", true);
		disk.finalLibraryClassesSize.add(extractSize());
		readUntilLine(in, "doop disk usage: ", true);
		disk.doopAverroes.add(extractNumber());
	}

	private static void readDoopDiskStats(BufferedReader doop, DiskUsage disk) throws IOException {
		readUntilLine(doop, "Disk usage stats ...");
		readUntilLineStartsWith(doop, "doop:");
		disk.doop.add(extractNumber());
	}

	private static void readOrganizerStats(BufferedReader organizer, ExecutionTime time, DiskUsage disk, String benchmark)
			throws IOException {
		readUntilLineStartsWith(organizer, "running " + benchmark);
		// we could get the number of application classes from here as well but we get it from the JarFactory output instead
		readUntilLineStartsWith(organizer, "Library classes:");
		disk.originalLibraryClasses.add(extractNumber());
		readUntilLineStartsWith(organizer, "elapsed time:");
		time.organizer.add(extractNumber());
	}

	private static void readJarFactoryStats(BufferedReader factory, DiskUsage disk, ExecutionTime time, String benchmark)
			throws IOException {
		readUntilLineStartsWith(factory, "running " + benchmark);
		readUntilLineStartsWith(factory, "Soot loaded the input classes in");
		time.sootLoading.add(extractNumber());
		readUntilLineStartsWith(factory, "Number of input application classes:");
		disk.inputApplicationClasses.add(extractNumber());
		readUntilLineStartsWith(factory, "Number of input library classes:");
		disk.inputLibraryClasses.add(extractNumber());
		readUntilLineStartsWith(factory, "Number of input library methods:");
		disk.inputLibraryMethods.add(extractNumber());
		readUntilLineStartsWith(factory, "Number of input library fields:");
		disk.inputLibraryFields.add(extractNumber());

		readUntilLineStartsWith(factory, "Number of referenced library methods:");
		disk.referencedLibraryMethods.add(extractNumber());
		readUntilLineStartsWith(factory, "Number of referenced library fields:");
		disk.referencedLibraryFields.add(extractNumber());
		readUntilLineStartsWith(factory, "Number of removed library methods:");
		disk.removedLibraryMethods.add(extractNumber());
		readUntilLineStartsWith(factory, "Number of removed library fields:");
		disk.removedLibraryFields.add(extractNumber());

		readUntilLineStartsWith(factory, "Number of library methods:");
		disk.libraryMethods.add(extractNumber());
		readUntilLineStartsWith(factory, "Number of library fields:");
		disk.libraryFields.add(extractNumber());

		readUntilLineStartsWith(factory, "Number of generated library classes:");
		disk.generatedLibraryClasses.add(extractNumber());
		readUntilLineStartsWith(factory, "Number of generated library methods:");
		disk.generatedLibraryMethods.add(extractNumber());

		readUntilLineStartsWith(factory, "Library classes created and validated in");
		time.jarFactory.add(extractNumber());
		readUntilLineStartsWith(factory, "Library JAR file verified in");
		time.jarVerification.add(extractNumber());
		readUntilLineStartsWith(factory, "elapsed time:");
		time.jarFactoryTotal.add(extractNumber());
	}

	private static void readSparkAverroesTime(BufferedReader stats, ExecutionTime time) throws IOException {
		readUntilLineStartsWith(stats, "[Spark] Solution found in");
		time.sparkAverroes.add(extractNumber());
		readUntilLineStartsWith(stats, "elapsed time:");
		time.sparkAverroesTotal.add(extractNumber());
	}

	private static void readSparkTime(BufferedReader spark, ExecutionTime time) throws IOException {
		readUntilLineStartsWith(spark, "[Spark] Solution found in");
		time.spark.add(extractNumber());
		readUntilLineStartsWith(spark, "elapsed time:");
		time.sparkTotal.add(extractNumber());
	}

	private static void readDoopAverroesTime(BufferedReader stats, ExecutionTime time) throws IOException {
		double total = 0, analysis = 0;
		readUntilLine(stats, "clearing all cached results ...");
		readUntilLineStartsWith(stats, "elapsed time:");
		total += Double.valueOf(extractNumber());

		readUntilLineStartsWith(stats, "creating database in");
		readUntilLineStartsWith(stats, "elapsed time:");
		total += Double.valueOf(extractNumber());

		readUntilLine(stats, "loading fact declarations ...");
		readUntilLineStartsWith(stats, "elapsed time:");
		total += Double.valueOf(extractNumber());

		readUntilLine(stats, "loading facts ...");
		readUntilLineStartsWith(stats, "elapsed time:");
		total += Double.valueOf(extractNumber());

		readUntilLine(stats, "loading context-insensitive declarations...");
		readUntilLineStartsWith(stats, "elapsed time:");
		total += Double.valueOf(extractNumber());

		readUntilLine(stats, "loading context-insensitive delta rules...");
		readUntilLineStartsWith(stats, "elapsed time:");
		total += Double.valueOf(extractNumber());

		readUntilLine(stats, "loading reflection delta rules...");
		readUntilLineStartsWith(stats, "elapsed time:");
		total += Double.valueOf(extractNumber());

		readUntilLine(stats, "loading client delta rules...");
		readUntilLineStartsWith(stats, "elapsed time:");
		total += Double.valueOf(extractNumber());

		readUntilLine(stats, "MBBENCH logicblox START");
		readUntilLineStartsWith(stats, "elapsed time:");
		analysis = Double.valueOf(extractNumber());
		total += analysis;

		time.doopAverroes.add(String.valueOf(MathUtils.round(analysis, 2)));
		time.doopAverroesTotal.add(String.valueOf(MathUtils.round(total, 2)));
	}

	private static void readDoopTime(BufferedReader doop, ExecutionTime time) throws IOException {
		double total = 0, analysis = 0;
		readUntilLine(doop, "clearing all cached results ...");
		readUntilLineStartsWith(doop, "elapsed time:");
		total += Double.valueOf(extractNumber());

		readUntilLineStartsWith(doop, "creating database in");
		readUntilLineStartsWith(doop, "elapsed time:");
		total += Double.valueOf(extractNumber());

		readUntilLine(doop, "loading fact declarations ...");
		readUntilLineStartsWith(doop, "elapsed time:");
		total += Double.valueOf(extractNumber());

		readUntilLine(doop, "loading facts ...");
		readUntilLineStartsWith(doop, "elapsed time:");
		total += Double.valueOf(extractNumber());

		readUntilLine(doop, "loading context-insensitive declarations...");
		readUntilLineStartsWith(doop, "elapsed time:");
		total += Double.valueOf(extractNumber());

		readUntilLine(doop, "loading context-insensitive delta rules...");
		readUntilLineStartsWith(doop, "elapsed time:");
		total += Double.valueOf(extractNumber());

		readUntilLine(doop, "loading reflection delta rules...");
		readUntilLineStartsWith(doop, "elapsed time:");
		total += Double.valueOf(extractNumber());

		readUntilLine(doop, "loading client delta rules...");
		readUntilLineStartsWith(doop, "elapsed time:");
		total += Double.valueOf(extractNumber());

		readUntilLine(doop, "MBBENCH logicblox START");
		readUntilLineStartsWith(doop, "elapsed time:");
		analysis = Double.valueOf(extractNumber());
		total += analysis;

		time.doop.add(String.valueOf(MathUtils.round(analysis, 2)));
		time.doopTotal.add(String.valueOf(MathUtils.round(total, 2)));
	}

	private static String extractSize() {
		String size = line.trim().split(" +")[0]; // in bytes, need to convert it to MB
		return DiskUsage.sizeInMB(size);
	}

	private static double readGcStats(BufferedReader in) throws IOException {
		String line;
		double memory = 0;

		while ((line = in.readLine()) != null) {
			if (line.indexOf("Full GC") > -1) {
				double next = extractMemory(line);
				memory = next > memory ? next : memory;
			}
		}

		return memory;
	}

	private static void readSparkAverroesGcStats(BufferedReader in, DiskUsage disk) throws IOException {
		double memory = readGcStats(in);
		disk.sparkAverroes.add(DiskUsage.sizeInMB(memory));
	}

	private static void readSparkGcStats(BufferedReader in, DiskUsage disk) throws IOException {
		double memory = readGcStats(in);
		disk.spark.add(DiskUsage.sizeInMB(memory));
	}

	public static double extractMemory(String line) {
		String regex = "\\d+(.\\d+)?";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line);

		// Skip the first two value, they are the timestamp and the memory size before doing GC. The third value is the
		// heap after in KB
		matcher.find();
		matcher.find();
		Double.parseDouble(matcher.group());

		matcher.find();
		return Double.parseDouble(matcher.group());
	}
}
