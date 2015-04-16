package ca.uwaterloo.averroes.callgraph;

import java.io.File;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import ca.uwaterloo.averroes.callgraph.converters.DoopCallGraphConverter;
import ca.uwaterloo.averroes.callgraph.converters.ProbeCallGraphCollapser;
import ca.uwaterloo.averroes.callgraph.converters.SootCallGraphConverter;
import ca.uwaterloo.averroes.callgraph.transformers.AndroidCallGraphTransformer;
import ca.uwaterloo.averroes.callgraph.transformers.AndroidWithAverroesCallGraphTransformer;
import ca.uwaterloo.averroes.callgraph.transformers.SparkCallGraphTransformer;
import ca.uwaterloo.averroes.callgraph.transformers.SparkWithAverroesCallGraphTransformer;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.util.CommandExecuter;
import ca.uwaterloo.averroes.util.TimeUtils;
import ca.uwaterloo.averroes.util.io.FileUtils;
import ca.uwaterloo.cgstudy.util.ProbeUtils;

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisOptions.ReflectionOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.BasicCallGraph;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.config.AnalysisScopeReader;

/**
 * A factory that generates call graphs for some tools.
 * 
 * @author karim
 * 
 */
public class CallGraphFactory {

	/**
	 * Generate the call graph for SparkAverroes.
	 * 
	 * @param benchmark
	 * @return
	 * @throws IOException
	 */
	public static CallGraph generateSparkWithAverroesCallGraph(String benchmark) throws IOException {
		probe.CallGraph spark = new SparkWithAverroesCallGraphTransformer(benchmark).run();
		return SootCallGraphConverter.convert(spark);
	}

	/**
	 * Generate the call graph for Spark.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static CallGraph generateSparkCallGraph(String benchmark) throws IOException {
		probe.CallGraph spark = new SparkCallGraphTransformer(benchmark).getProbeCallGraph();
		System.out.println("size of original spark is: " + spark.edges().size());
		return SootCallGraphConverter.convert(spark);
	}

	/**
	 * Generate the call graph for an Android apk.
	 * 
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException 
	 */
	public static probe.CallGraph generateAndroidCallGraph() throws IOException, XmlPullParserException {
		probe.CallGraph android = new AndroidCallGraphTransformer().run();
		return ProbeCallGraphCollapser.collapse(android);
	}

	/**
	 * Generate the call graph for an Android apk using the Averroes placeholder library.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static probe.CallGraph generateAndroidWithAverroesCallGraph(String benchmark) throws IOException {
		probe.CallGraph android = AndroidWithAverroesCallGraphTransformer.run(benchmark);
		return ProbeCallGraphCollapser.collapse(android);
	}

	/**
	 * Generate the call graph for DoopAverroes.
	 * 
	 * @param doopHome
	 * @param benchmark
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static CallGraph generateDoopWithAverroesCallGraph(String doopHome, String benchmark) throws IOException,
			InterruptedException {
		// 1. Run doop's analysis
		CommandExecuter.runDoopAverroes(doopHome, benchmark);

		// 2. Convert the Doop call graph
		return DoopCallGraphConverter.convert(doopHome, CallGraphSource.DOOP_AVERROES);
	}

	/**
	 * Generate the call graph for Doop.
	 * 
	 * @param doopHome
	 * @param benchmark
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static CallGraph generateDoopCallGraph(String doopHome, String benchmark) throws IOException,
			InterruptedException {
		// 1. Run doop's analysis
		CommandExecuter.runDoop(doopHome, benchmark);

		// 2. Convert the Doop call graph
		return DoopCallGraphConverter.convert(doopHome, CallGraphSource.DOOP);
	}
	
	/**
	 * Generate the call graph for WalaAverroes.
	 * 
	 * @param benchmark
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassHierarchyException 
	 * @throws CallGraphBuilderCancelException 
	 * @throws IllegalArgumentException 
	 */
	public static CallGraph generateWalaWithAverroesCallGraph(String benchmark) throws IOException,
			InterruptedException, ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException {
		// 1. build the call graph
		String classpath = FileUtils.composeClassPath(FileUtils.organizedApplicationJarFile(benchmark), FileUtils.placeholderLibraryJarFile(benchmark));
		String exclusionFile = CallGraphFactory.class.getClassLoader().getResource(CallGraphTestUtil.REGRESSION_EXCLUSIONS).getPath();
		
		AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(classpath, new File(exclusionFile));
		ClassHierarchy cha = ClassHierarchy.make(scope);
		Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, cha, "L" + AverroesProperties.getMainClass().replaceAll("\\.", "/"));
		
		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
		options.setReflectionOptions(ReflectionOptions.ONE_FLOW_TO_CASTS_NO_METHOD_INVOKE);
		
		SSAPropagationCallGraphBuilder builder = Util.makeZeroCFABuilder(options, new AnalysisCache(), cha, scope, null, null);
		
		TimeUtils.splitStart();
		BasicCallGraph<?> cg = (BasicCallGraph<?>) builder.makeCallGraph(options, null);
		System.out.println("[Wala] Solution found in " + TimeUtils.elapsedSplitTime() + " seconds.");

		// 2. Convert the WalaAverroes call graph
		probe.CallGraph wala = ProbeUtils.getProbeCallGraph(cg);
	    return ProbeCallGraphCollapser.collapse(wala, CallGraphSource.WALA_AVERROES);
	}

	/**
	 * Generate the call graph for Wala.
	 * 
	 * @param benchmark
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassHierarchyException 
	 * @throws CallGraphBuilderCancelException 
	 * @throws IllegalArgumentException 
	 */
	public static CallGraph generateWalaCallGraph(String benchmark) throws IOException,
			InterruptedException, ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException {
		// 1. build the call graph
		String classpath = FileUtils.composeClassPath(FileUtils.organizedApplicationJarFile(benchmark), FileUtils.organizedLibraryJarFile(benchmark));
		String exclusionFile = CallGraphFactory.class.getClassLoader().getResource(CallGraphTestUtil.REGRESSION_EXCLUSIONS).getPath();
		
		AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(classpath, new File(exclusionFile));
		ClassHierarchy cha = ClassHierarchy.make(scope);
		Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, cha, "L" + AverroesProperties.getMainClass().replaceAll("\\.", "/"));
		
		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
		options.setReflectionOptions(ReflectionOptions.ONE_FLOW_TO_CASTS_NO_METHOD_INVOKE);
		
		SSAPropagationCallGraphBuilder builder = Util.makeZeroCFABuilder(options, new AnalysisCache(), cha, scope, null, null);
		
		TimeUtils.splitStart();
		BasicCallGraph<?> cg = (BasicCallGraph<?>) builder.makeCallGraph(options, null);
		System.out.println("[Wala] Solution found in " + TimeUtils.elapsedSplitTime() + " seconds.");

		// 2. Convert the Wala call graph
		probe.CallGraph wala = ProbeUtils.getProbeCallGraph(cg);
	    return ProbeCallGraphCollapser.collapse(wala, CallGraphSource.WALA);
	}
}