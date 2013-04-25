package ca.uwaterloo.averroes.callgraph.transformers;

import java.io.IOException;
import java.util.Iterator;

import probe.CallGraph;
import probe.ObjectManager;
import probe.ProbeClass;
import probe.ProbeMethod;
import soot.G;
import soot.Kind;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.spark.SparkTransformer;
import soot.options.Options;
import ca.uwaterloo.averroes.jar.JarOrganizer;
import ca.uwaterloo.averroes.properties.AverroesProperties;

public class SparkCallGraphTransformer {
	private JarOrganizer organizer;

	public SparkCallGraphTransformer() {
		organizer = new JarOrganizer();
	}

	@SuppressWarnings("unchecked")
	public CallGraph getProbeCallGraph() throws IOException {
		System.out.println("Generating the call graph from Spark.");
		
		// Reset Soot
		G.reset();
		organizer.organizeInputJarFiles();

		// Set some soot parameters
		Options.v().set_prepend_classpath(false);
		Options.v().set_soot_classpath(AverroesProperties.getClasspath());
		Options.v().set_dynamic_class(AverroesProperties.getDynamicClasses());
		Options.v().classes().addAll(organizer.applicationClassNames());
		Options.v().set_main_class(AverroesProperties.getMainClass());
		Options.v().set_whole_program(true);

		// Load the necessary classes
		Scene.v().loadNecessaryClasses();
		Scene.v().setMainClassFromOptions();

		// Run the Spark transformer
		SparkTransformer.v().transform("", Transformer.SPARK.options());

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
	private ProbeMethod probeMethod(SootMethod sootMethod) {
		SootClass sootClass = sootMethod.getDeclaringClass();
		ProbeClass cls = ObjectManager.v().getClass(sootClass.toString());
		return ObjectManager.v().getMethod(cls, sootMethod.getName(), sootMethod.getBytecodeParms());
	}
}