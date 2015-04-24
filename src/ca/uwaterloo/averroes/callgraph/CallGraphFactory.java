package ca.uwaterloo.averroes.callgraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;

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
import ca.uwaterloo.averroes.util.ProbeUtils;
import ca.uwaterloo.averroes.util.TimeUtils;
import ca.uwaterloo.averroes.util.io.FileUtils;

import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisOptions.ReflectionOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.BasicCallGraph;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXCFABuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.io.FileProvider;

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
	 * Generate the call graph for an Android apk using the Averroes placeholder
	 * library.
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
	 * @throws InvalidClassFileException 
	 */
	public static probe.CallGraph generateWalaCallGraph(String benchmark, boolean isAve) throws IOException,
			InterruptedException, ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException, InvalidClassFileException {
		// 1. build the call graph
		String classpath = FileUtils.composeClassPath(FileUtils.organizedApplicationJarFile(benchmark),
				isAve ? FileUtils.placeholderLibraryJarFile(benchmark) : FileUtils.organizedLibraryJarFile(benchmark));
		String exclusionFile = CallGraphFactory.class.getClassLoader()
				.getResource(CallGraphTestUtil.REGRESSION_EXCLUSIONS).getPath();

		AnalysisScope scope = isAve ? makeAverroesAnalysisScope(FileUtils.organizedApplicationJarFile(benchmark),
				FileUtils.placeholderLibraryJarFile(benchmark), exclusionFile) : AnalysisScopeReader
				.makeJavaBinaryAnalysisScope(classpath, new File(exclusionFile));
		ClassHierarchy cha = ClassHierarchy.make(scope);
		Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, cha, "L"
				+ AverroesProperties.getMainClass().replaceAll("\\.", "/"));

		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
		options.setReflectionOptions(isAve ? ReflectionOptions.NONE
				: ReflectionOptions.MULTI_FLOW_TO_CASTS_APPLICATION_GET_METHOD);

		SSAPropagationCallGraphBuilder builder = isAve ? makeZeroOneCFABuilder(options, new AnalysisCache(), cha,
				scope, null, null) : Util.makeZeroOneCFABuilder(options, new AnalysisCache(), cha, scope, null, null);
		
		TimeUtils.splitStart();
		BasicCallGraph<?> cg = (BasicCallGraph<?>) builder.makeCallGraph(options, null);
		System.out.println("[Wala] Solution found in " + TimeUtils.elapsedSplitTime() + " seconds.");

		// 2. Convert the Wala call graph to probe and collapse it
		return ProbeUtils.getProbeCallGraph(cg);
		// probe.CallGraph wala = ProbeUtils.getProbeCallGraph(cg);
		// return ProbeCallGraphCollapser.collapse(wala, isAve ?
		// CallGraphSource.WALA_AVERROES : CallGraphSource.WALA);
	}

	public static SSAPropagationCallGraphBuilder makeZeroOneCFABuilder(AnalysisOptions options, AnalysisCache cache,
			IClassHierarchy cha, AnalysisScope scope, ContextSelector customSelector,
			SSAContextInterpreter customInterpreter) {

		if (options == null) {
			throw new IllegalArgumentException("options is null");
		}
		Util.addDefaultSelectors(options, cha);

		return ZeroXCFABuilder.make(cha, options, cache, customSelector, customInterpreter,
				ZeroXInstanceKeys.ALLOCATIONS | ZeroXInstanceKeys.SMUSH_MANY
						| ZeroXInstanceKeys.SMUSH_PRIMITIVE_HOLDERS | ZeroXInstanceKeys.SMUSH_STRINGS
						| ZeroXInstanceKeys.SMUSH_THROWABLES);
	}

	public static AnalysisScope makeAverroesAnalysisScope(String app, String averroesLib, String exclusions)
			throws IOException, IllegalArgumentException, InvalidClassFileException {
		AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();

		// Exclusion file
		File exclusionsFile = new File(exclusions);
		InputStream fs = exclusionsFile.exists() ? new FileInputStream(exclusionsFile) : FileProvider.class
				.getClassLoader().getResourceAsStream(exclusionsFile.getName());
		scope.setExclusions(new FileOfClasses(fs));

		// Averroes library
		// Try adding all class files in AveLib to Primordial except for AverroesLibraryClass, add it to Application
//		new JarFileModule(new JarFile(new File(averroesLib))).getEntries().forEachRemaining(
//				m -> {
//					try {
//						if (m.isClassFile()
//								&& m.getClassName().equals(Names.AVERROES_LIBRARY_CLASS.replaceAll("\\.", "/"))) {
//							scope.addClassFileToScope(ClassLoaderReference.Application,
//									((ClassFileModule) m.asModule()).getFile());
//							System.out.println("Application: " + Names.AVERROES_LIBRARY_CLASS);
//						} else if (m.isClassFile()) {
//							scope.addClassFileToScope(ClassLoaderReference.Primordial,
//									((ClassFileModule) m.asModule()).getFile());
//							System.out.println("Primordial: " + m.getClassName());
//						}
//
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				});
//		scope.addToScope(ClassLoaderReference.Application, new JarFileModule(new JarFile(new File(averroesLib.replace(".jar", "-bkp.jar")))));
		scope.addToScope(ClassLoaderReference.Primordial, new JarFileModule(new JarFile(new File(averroesLib.replace(".jar", "-bkp.jar")))));
//		scope.addToScope(ClassLoaderReference.Primordial, new JarFileModule(new JarFile(new File(averroesLib))));		
//		scope.addClassFileToScope(ClassLoaderReference.Application, new File("benchmarks-averroes/dacapo/lusearch-placeholder-lib/ca/uwaterloo/averroes/Library.class"));

		// Application JAR
		scope.addToScope(ClassLoaderReference.Application, new JarFileModule(new JarFile(new File(app))));

		return scope;
	}
}