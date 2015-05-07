package ca.uwaterloo.averroes.callgraph.transformers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import probe.CallGraph;
import probe.ObjectManager;
import probe.ProbeClass;
import probe.ProbeMethod;
import soot.ClassProvider;
import soot.G;
import soot.Kind;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.jimple.spark.SparkTransformer;
import soot.options.Options;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.soot.Names;

public class SparkCallGraphTransformer {
	private AverroesClassProvider provider;
	private boolean isAverroes;

	public SparkCallGraphTransformer(String base, String benchmark, boolean isAverroes) {
		this.isAverroes = isAverroes;
		provider = new AverroesClassProvider(base, benchmark, this.isAverroes);
	}

	public CallGraph getProbeCallGraph() throws IOException {
		System.out.println("Generating the call graph from Spark" + (isAverroes ? "Averroes." : "."));

		// Reset Soot
		G.reset();
		provider.prepare();

		// Set some soot parameters
		SourceLocator.v().setClassProviders(Collections.singletonList((ClassProvider) provider));
		Options.v().classes().addAll(provider.getApplicationClassNames());
		Options.v().set_main_class(AverroesProperties.getMainClass());
		Options.v().set_whole_program(true);

		// Dynamic classes are only relevant for the vanilla Spark
		if (!isAverroes) {
			addCommonDynamicClasses(provider);
			Options.v().set_dynamic_class(AverroesProperties.getDynamicClasses());
		} else {
			// required since this class is not related to any other class now
			Options.v().classes().add(Names.AVERROES_LIBRARY_CLASS);
		}

		// Load the necessary classes
		Scene.v().loadNecessaryClasses();
		Scene.v().setMainClassFromOptions();

		// Setting entry points (i.e., main method of the main class)
		Scene.v().setEntryPoints(entryPoints());

		// Run the Spark transformer
		SparkTransformer.v().transform("", Transformer.sparkOptions(isAverroes));

		// Retrieve the call graph edges
		CallGraph probecg = new CallGraph();
		soot.jimple.toolkits.callgraph.CallGraph cg = Scene.v().getCallGraph();

		Iterator<soot.jimple.toolkits.callgraph.Edge> it = cg.listener();
		while (it.hasNext()) {
			soot.jimple.toolkits.callgraph.Edge e = it.next();
			if (e.isExplicit() || e.kind().equals(Kind.NEWINSTANCE)) {
				probecg.edges().add(new probe.CallEdge(probeMethod(e.src()), probeMethod(e.tgt())));
			}
		}

		// Retrieve the call graph entry points
		for (SootMethod method : Scene.v().getEntryPoints()) {
			probecg.entryPoints().add(probeMethod(method));
		}

		return probecg;
	}

	/**
	 * The main method of the main class set in the Soot scene is the only entry
	 * point to the call graph.
	 * 
	 * @return
	 */
	private List<SootMethod> entryPoints() {
		List<SootMethod> result = new ArrayList<SootMethod>();
		result.add(Scene.v().getMainMethod());
		if (isAverroes) {
			result.add(Scene.v().getMethod(Names.AVERROES_LIBRARY_CLINIT_METHOD_SIGNATURE));
		}
		return result;
	}

	/**
	 * Convert a soot method to a probe method.
	 * 
	 * @param sootMethod
	 * @return
	 */
	private ProbeMethod probeMethod(SootMethod sootMethod) {
		SootClass sootClass = sootMethod.getDeclaringClass();
		ProbeClass cls = ObjectManager.v().getClass(sootClass.toString());
		return ObjectManager.v().getMethod(cls, sootMethod.getName(), sootMethod.getBytecodeParms());
	}

	public static void addCommonDynamicClass(ClassProvider provider, String className) {
		if (provider.find(className) != null) {
			Scene.v().addBasicClass(className);
		}
	}

	public static void addCommonDynamicClasses(ClassProvider provider) {
		/*
		 * For simulating the FileSystem class, we need the implementation of
		 * the FileSystem, but the classes are not loaded automatically due to
		 * the indirection via native code.
		 */
		addCommonDynamicClass(provider, "java.io.UnixFileSystem");
		addCommonDynamicClass(provider, "java.io.WinNTFileSystem");
		addCommonDynamicClass(provider, "java.io.Win32FileSystem");

		/* java.net.URL loads handlers dynamically */
		addCommonDynamicClass(provider, "sun.net.www.protocol.file.Handler");
		addCommonDynamicClass(provider, "sun.net.www.protocol.ftp.Handler");
		addCommonDynamicClass(provider, "sun.net.www.protocol.http.Handler");
		addCommonDynamicClass(provider, "sun.net.www.protocol.https.Handler");
		addCommonDynamicClass(provider, "sun.net.www.protocol.jar.Handler");
	}
}