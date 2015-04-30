package ca.uwaterloo.averroes.callgraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.jar.JarFile;

import org.xmlpull.v1.XmlPullParserException;

import ca.uwaterloo.averroes.callgraph.converters.DoopCallGraphConverter;
import ca.uwaterloo.averroes.callgraph.converters.ProbeCallGraphCollapser;
import ca.uwaterloo.averroes.callgraph.converters.SootCallGraphConverter;
import ca.uwaterloo.averroes.callgraph.transformers.AndroidCallGraphTransformer;
import ca.uwaterloo.averroes.callgraph.transformers.AndroidWithAverroesCallGraphTransformer;
import ca.uwaterloo.averroes.callgraph.transformers.SparkCallGraphTransformer;
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
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
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
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.strings.Atom;

/**
 * A factory that generates call graphs for some tools.
 * 
 * @author karim
 * 
 */
public class CallGraphFactory {

	/**
	 * Generate the call graph for Spark.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static CallGraph generateSparkCallGraph(String benchmark, boolean isAverroes) throws IOException {
		probe.CallGraph spark = new SparkCallGraphTransformer(benchmark, isAverroes).getProbeCallGraph();
		System.out.println("size of original spark is: " + spark.edges().size());
		return SootCallGraphConverter.convert(spark, isAverroes ? CallGraphSource.SPARK_AVERROES : CallGraphSource.SPARK);
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
			InterruptedException, ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException,
			InvalidClassFileException {
		// 1. build the call graph
		String classpath = FileUtils.composeClassPath(FileUtils.organizedApplicationJarFile(benchmark),
				FileUtils.organizedLibraryJarFile(benchmark));

		String exclusionFile = CallGraphFactory.class.getClassLoader()
				.getResource(CallGraphTestUtil.REGRESSION_EXCLUSIONS).getPath();

		AnalysisScope scope = isAve ? makeAverroesAnalysisScope(benchmark, exclusionFile) : AnalysisScopeReader
				.makeJavaBinaryAnalysisScope(classpath, new File(exclusionFile));

		ClassHierarchy cha = ClassHierarchy.make(scope);

		Iterable<Entrypoint> entrypoints = makeMainEntrypoints(scope.getApplicationLoader(), cha, new String[] { "L"
				+ AverroesProperties.getMainClass().replaceAll("\\.", "/") }, isAve);

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

	public static AnalysisScope makeAverroesAnalysisScope(String benchmark, String exclusions) throws IOException,
			IllegalArgumentException, InvalidClassFileException {
		AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();

		// Exclusion file
		File exclusionsFile = new File(exclusions);
		InputStream fs = exclusionsFile.exists() ? new FileInputStream(exclusionsFile) : FileProvider.class
				.getClassLoader().getResourceAsStream(exclusionsFile.getName());
		scope.setExclusions(new FileOfClasses(fs));

		// Library stuff
		scope.addToScope(ClassLoaderReference.Application,
				new JarFileModule(new JarFile(new File(FileUtils.averroesLibraryClassJarFile(benchmark)))));
		scope.addToScope(ClassLoaderReference.Primordial,
				new JarFileModule(new JarFile(new File(FileUtils.placeholderLibraryJarFile(benchmark)))));

		// Application JAR
		scope.addToScope(ClassLoaderReference.Application,
				new JarFileModule(new JarFile(new File(FileUtils.organizedApplicationJarFile(benchmark)))));

		return scope;
	}

	public static Iterable<Entrypoint> makeMainEntrypoints(final ClassLoaderReference loaderRef,
			final IClassHierarchy cha, final String[] classNames, boolean isAve) throws IllegalArgumentException,
			IllegalArgumentException, IllegalArgumentException {
		return new Iterable<Entrypoint>() {
			@Override
			public Iterator<Entrypoint> iterator() {
				final Atom mainMethod = Atom.findOrCreateAsciiAtom("main");

				return new Iterator<Entrypoint>() {
					private int index = 0;
					private boolean clinitTaken = false;

					@Override
					public void remove() {
						Assertions.UNREACHABLE();
					}

					@Override
					public boolean hasNext() {
						return index < classNames.length || (isAve && !clinitTaken);
					}

					@Override
					public Entrypoint next() {
						if (index < classNames.length) {
							TypeReference T = TypeReference.findOrCreate(loaderRef,
									TypeName.string2TypeName(classNames[index++]));
							MethodReference mainRef = MethodReference.findOrCreate(T, mainMethod,
									Descriptor.findOrCreateUTF8("([Ljava/lang/String;)V"));
							return new DefaultEntrypoint(mainRef, cha);
						} else if(isAve && !clinitTaken){
							clinitTaken = true;
							TypeReference T = TypeReference.findOrCreate(loaderRef,
									TypeName.string2TypeName("Lca/uwaterloo/averroes/Library"));
							MethodReference clinitRef = MethodReference.findOrCreate(T, MethodReference.clinitName,
									MethodReference.clinitSelector.getDescriptor());
							return new DefaultEntrypoint(clinitRef, cha);
						} else {
							throw new IllegalStateException("No more entry points. This should never happen!");
						}
					}
				};
			}
		};

	}
}