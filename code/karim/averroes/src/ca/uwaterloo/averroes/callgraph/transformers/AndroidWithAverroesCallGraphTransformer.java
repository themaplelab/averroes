package ca.uwaterloo.averroes.callgraph.transformers;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import probe.CallGraph;
import probe.ObjectManager;
import probe.ProbeClass;
import probe.ProbeMethod;
import soot.ClassProvider;
import soot.CoffiClassProvider;
import soot.DexClassProvider;
import soot.JavaClassProvider;
import soot.JimpleClassProvider;
import soot.Kind;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.jimple.spark.SparkTransformer;
import soot.options.Options;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.util.DexUtils;

public class AndroidWithAverroesCallGraphTransformer {
	public static CallGraph run(String benchmark) throws IOException {
		System.out.println("Generating the call graph for " + benchmark + ".apk");

		// Initialize soot
		soot.G.reset();

		// Set the application classes
		SourceLocator.v().setClassProviders(classProviders());
		Set<String> appClasses = DexUtils.applicationClassesOfDex(AverroesProperties.getApkLocation());
		appClasses.add(AverroesProperties.getMainClass());
		Options.v().classes().addAll(appClasses);
		Options.v().set_main_class(AverroesProperties.getMainClass());

		Options.v().set_whole_program(true);
		Options.v().set_full_resolver(true);
		Options.v().set_soot_classpath(AverroesProperties.getAndroidAverroesClassPath(benchmark));
//		Options.v().set_src_prec(Options.src_prec_apk); TODO: change back
//		Options.v().set_force_android_jar(FileUtils.androidPlaceholderLibraryJarFile(benchmark)); TODO: change back

		// Options.v().set_verbose(true);

		// Load the necessary classes
		Scene.v().loadNecessaryClasses();
		Scene.v().setMainClassFromOptions();

		// Make the main method of the dummy class the entry point of the call graph
		// SootMethod mainMethod = Scene.v().getMethod(Names.AVERROES_DO_IT_ALL_METHOD_SIGNATURE);
		Scene.v().setEntryPoints(Collections.singletonList(Scene.v().getMainMethod()));

		// Run the Spark transformer
		SparkTransformer.v().transform("", Transformer.sparkOptions(true));

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
	 * Convert a soot method to a probe method.
	 * 
	 * @param sootMethod
	 * @return
	 */
	private static ProbeMethod probeMethod(SootMethod sootMethod) {
		SootClass sootClass = sootMethod.getDeclaringClass();
		ProbeClass cls = ObjectManager.v().getClass(sootClass.toString());
		return ObjectManager.v().getMethod(cls, sootMethod.getName(), sootMethod.getBytecodeParms());
	}

	/**
	 * Return a list of class providers. We put coffi before dex, becasue in android, the library classes in
	 * android.support.v4 are included in the classes.dex. Therefore, when Soot looks for a library classes that happens
	 * to be in this package, it will get it from the classes.dex instead of the placeholder library because of the
	 * default order of the class providers in SourceLocator.
	 * 
	 * @return
	 */
	private static LinkedList<ClassProvider> classProviders() {
		LinkedList<ClassProvider> result = new LinkedList<ClassProvider>();
		result.add(new CoffiClassProvider());
		result.add(new DexClassProvider());
		result.add(new JavaClassProvider());
		result.add(new JimpleClassProvider());
		return result;
	}
}
