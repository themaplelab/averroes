package ca.uwaterloo.averroes.stats;

import java.io.FileInputStream;

import ca.uwaterloo.averroes.callgraph.CallGraph;
import ca.uwaterloo.averroes.callgraph.CallGraphSource;
import ca.uwaterloo.averroes.callgraph.gxl.GXLReader;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.util.io.FileUtils;

public class StatisticsGenerator {

	public static void main(String[] args) {
		String benchmark = args[0];

		String cgcFile = "callgraphs/cgc-call-graphs/" + benchmark + "/cgc.gxl";
		String doopFile = "callgraphs/doop-call-graphs/" + benchmark + "/doop.gxl";
		String doopAverroesFile = "callgraphs/doop-averroes-call-graphs/" + benchmark + "/doopAverroes.gxl";
		String sparkFile = "callgraphs/spark-call-graphs/" + benchmark + "/spark.gxl";
		String sparkAverroesFile = "callgraphs/spark-averroes-call-graphs/" + benchmark + "/sparkAverroes.gxl";
		String dynamicFile = "callgraphs/dynamic-call-graphs/" + benchmark + "/dynamic.gxl";

		try {
			FileUtils.createDirectory(AverroesProperties.getOutputDir());

			CallGraph cgc = new GXLReader().readCallGraph(new FileInputStream(cgcFile), CallGraphSource.CGC);

			CallGraph doop = new GXLReader().readCallGraph(new FileInputStream(doopFile), CallGraphSource.DOOP);
			CallGraph doopAverroes = new GXLReader().readCallGraph(new FileInputStream(doopAverroesFile),
					CallGraphSource.DOOP_AVERROES);

			CallGraph spark = new GXLReader().readCallGraph(new FileInputStream(sparkFile), CallGraphSource.SPARK);
			CallGraph sparkAverroes = new GXLReader().readCallGraph(new FileInputStream(sparkAverroesFile),
					CallGraphSource.SPARK_AVERROES);

			CallGraph dyn = new GXLReader().readCallGraph(new FileInputStream(dynamicFile), CallGraphSource.DYNAMIC);

			// Soundness w.r.t dynamic call graphs
			Statistics dyn_Spark = dyn.diffCallGraphs(spark);
			Statistics dyn_SparkAverroes = dyn.diffCallGraphs(sparkAverroes);
			Statistics dyn_Doop = dyn.diffCallGraphs(doop);
			Statistics dyn_DoopAverroes = dyn.diffCallGraphs(doopAverroes);

			// Adjust the unsoundness in the original call graph for Doop and Spark
			doop = doop.union(dyn);
			spark = spark.union(dyn);

			// Soundness and Precision w.r.t. original call graphs
			Statistics spark_SparkAverroes = spark.diffCallGraphs(sparkAverroes);
			Statistics doop_DoopAverroes = doop.diffCallGraphs(doopAverroes);

			// Soundness and Precision of DoopAve w.r.t CGC call graph
			Statistics cgc_DoopAverroes = cgc.diffCallGraphs(doopAverroes);

			System.out.println("Call Graph Edges");
			System.out.println("================");
			System.out.println("Spark: " + spark.appToAppEdges().size());
			System.out.println("SparkAverroes: " + sparkAverroes.appToAppEdges().size());
			System.out.println("Doop: " + doop.appToAppEdges().size());
			System.out.println("DoopAverroes: " + doopAverroes.appToAppEdges().size());
			System.out.println("");

			System.out.println("Library Call Graph Edges");
			System.out.println("========================");
			System.out.println("Spark: " + spark.appToLibEdges().size());
			System.out.println("SparkAverroes: " + sparkAverroes.appToLibEdges().size());
			System.out.println("Doop: " + doop.appToLibEdges().size());
			System.out.println("DoopAverroes: " + doopAverroes.appToLibEdges().size());
			System.out.println("");

			System.out.println("Library Call Back Edges");
			System.out.println("=======================");
			System.out.println("Spark: " + spark.libToAppEdges().size());
			System.out.println("SparkAverroes: " + sparkAverroes.libToAppEdges().size());
			System.out.println("Doop: " + doop.libToAppEdges().size());
			System.out.println("DoopAverroes: " + doopAverroes.libToAppEdges().size());
			System.out.println("");

			System.out.println("");

			System.out.println("CGE");
			System.out.println("===");
			System.out.println("Dyn - Spark: " + dyn_Spark.getMissing().appToAppEdges().size());
			System.out.println("Dyn - SparkAverroes: " + dyn_SparkAverroes.getMissing().appToAppEdges().size());
			System.out.println("Dyn - Doop: " + dyn_Doop.getMissing().appToAppEdges().size());
			System.out.println("Dyn - DoopAverroes: " + dyn_DoopAverroes.getMissing().appToAppEdges().size());
			System.out.println("Spark - SparkAverroes: " + spark_SparkAverroes.getMissing().appToAppEdges().size());
			System.out.println("Doop - DoopAverroes: " + doop_DoopAverroes.getMissing().appToAppEdges().size());
			System.out.println("SparkAverroes - Spark: " + spark_SparkAverroes.getExtra().appToAppEdges().size());
			System.out.println("DoopAverroes - Doop: " + doop_DoopAverroes.getExtra().appToAppEdges().size());
			System.out.println("Cgc - DoopAverroes: " + cgc_DoopAverroes.getMissing().appToAppEdges().size());
			System.out.println("DoopAverroes - Cgc: " + cgc_DoopAverroes.getExtra().appToAppEdges().size());
			System.out.println("");

			System.out.println("LCGE");
			System.out.println("====");
			System.out.println("Dyn - Spark: " + dyn_Spark.getMissing().appToLibEdges().size());
			System.out.println("Dyn - SparkAverroes: " + dyn_SparkAverroes.getMissing().appToLibEdges().size());
			System.out.println("Dyn - Doop: " + dyn_Doop.getMissing().appToLibEdges().size());
			System.out.println("Dyn - DoopAverroes: " + dyn_DoopAverroes.getMissing().appToLibEdges().size());
			System.out.println("Spark - SparkAverroes: " + spark_SparkAverroes.getMissing().appToLibEdges().size());
			System.out.println("Doop - DoopAverroes: " + doop_DoopAverroes.getMissing().appToLibEdges().size());
			System.out.println("SparkAverroes - Spark: " + spark_SparkAverroes.getExtra().appToLibEdges().size());
			System.out.println("DoopAverroes - Doop: " + doop_DoopAverroes.getExtra().appToLibEdges().size());
			System.out.println("Cgc - DoopAverroes: " + cgc_DoopAverroes.getMissing().appToLibEdges().size());
			System.out.println("DoopAverroes - Cgc: " + cgc_DoopAverroes.getExtra().appToLibEdges().size());
			System.out.println("");

			System.out.println("LCBE");
			System.out.println("====");
			System.out.println("Dyn - Spark: " + dyn_Spark.getMissing().libToAppEdges().size());
			System.out.println("Dyn - SparkAverroes: " + dyn_SparkAverroes.getMissing().libToAppEdges().size());
			System.out.println("Dyn - Doop: " + dyn_Doop.getMissing().libToAppEdges().size());
			System.out.println("Dyn - DoopAverroes: " + dyn_DoopAverroes.getMissing().libToAppEdges().size());
			System.out.println("Spark - SparkAverroes: " + spark_SparkAverroes.getMissing().libToAppEdges().size());
			System.out.println("Doop - DoopAverroes: " + doop_DoopAverroes.getMissing().libToAppEdges().size());
			System.out.println("SparkAverroes - Spark: " + spark_SparkAverroes.getExtra().libToAppEdges().size());
			System.out.println("DoopAverroes - Doop: " + doop_DoopAverroes.getExtra().libToAppEdges().size());
			System.out.println("Cgc - DoopAverroes: " + cgc_DoopAverroes.getMissing().libToAppEdges().size());
			System.out.println("DoopAverroes - Cgc: " + cgc_DoopAverroes.getExtra().libToAppEdges().size());
			System.out.println("");

			System.out.println("");

			System.out.println("Frequencies of extra library call-backs");
			System.out.println("=======================================");
			System.out.println("SparkAverroes - Spark");
			System.out.println("----------");
			spark_SparkAverroes.getExtra().printFrequencies();
			System.out.println("");

			System.out.println("DoopAverroes - Doop");
			System.out.println("-----------");
			doop_DoopAverroes.getExtra().printFrequencies();
			System.out.println("");

			System.out.println("DoopAverroes - Cgc");
			System.out.println("-----------");
			cgc_DoopAverroes.getExtra().printFrequencies();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}