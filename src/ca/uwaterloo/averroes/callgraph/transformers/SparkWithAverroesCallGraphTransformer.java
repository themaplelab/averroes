package ca.uwaterloo.averroes.callgraph.transformers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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

public class SparkWithAverroesCallGraphTransformer {
	private AverroesClassProvider provider;

	public SparkWithAverroesCallGraphTransformer(String benchmark) {
		provider = new AverroesClassProvider(benchmark);
	}

	@SuppressWarnings("unchecked")
	public probe.CallGraph run() throws IOException {
		System.out.println("Generating the call graph from Spark.");

		// Reset Soot
		G.reset();

		provider.prepare();

		// Set some soot parameters
		SourceLocator.v().setClassProviders(Collections.singletonList((ClassProvider) provider));
		// SootSceneUtil.addCommonDynamicClasses(provider);
		Options.v().classes().addAll(provider.getApplicationClassNames());
		Options.v().set_main_class(AverroesProperties.getMainClass());
		Options.v().set_whole_program(true);

		// Since we're using the crafted JAR, then we only need to add the dynamic application classes.
		// Options.v().set_dynamic_class(AverroesProperties.getDynamicApplicationClasses());
		Options.v().set_dynamic_class(new ArrayList<String>());

		// Load the necessary classes
		Scene.v().loadNecessaryClasses();
		Scene.v().setMainClassFromOptions();

		// Setting entry points (i.e., main method of the main class)
		Scene.v().setEntryPoints(entryPoints());

		// Run the Spark transformer
		SparkTransformer.v().transform("", Transformer.SPARK.options());

		// Retrieve the call graph edges
		probe.CallGraph probecg = new probe.CallGraph();
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
	 * The main method of the main class set in the Soot scene is the only entry point to the call graph.
	 * 
	 * @return
	 */
	private List<SootMethod> entryPoints() {
		List<SootMethod> result = new ArrayList<SootMethod>();
		result.add(Scene.v().getMainMethod());
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
}