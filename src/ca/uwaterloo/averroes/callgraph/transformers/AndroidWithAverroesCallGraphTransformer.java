package ca.uwaterloo.averroes.callgraph.transformers;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import probe.CallGraph;
import probe.ObjectManager;
import probe.ProbeClass;
import probe.ProbeMethod;
import soot.Kind;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.spark.SparkTransformer;
import soot.options.Options;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.soot.Names;

public class AndroidWithAverroesCallGraphTransformer {
	// @SuppressWarnings("unchecked")
	public CallGraph run() throws IOException {
		System.out.println("Generating the call graph for an Android apk.");

		// Initialize soot
		soot.G.reset();
		// Options.v().set_no_bodies_for_excluded(true);
		// Options.v().set_allow_phantom_refs(true);
		// Options.v().set_output_format(Options.output_format_none);
		Options.v().set_whole_program(true);
		Options.v().set_soot_classpath(AverroesProperties.getAndroidAverroesClassPath());
		Options.v().set_src_prec(Options.src_prec_apk);
		soot.options.Options.v().set_android_jars(AverroesProperties.getAndroidPath());
		Scene.v().addBasicClass("android.app.Activity");
		Scene.v().loadNecessaryClasses();

		// SootClass sc = Scene.v().getSootClass("android.app.Activity");
		// System.out.println(SootClass.levelToString(sc.resolvingLevel()));

		// Make doItAll the entry point of the call graph
		SootMethod mainMethod = Scene.v().getMethod(Names.AVERROES_DO_IT_ALL_METHOD_SIGNATURE);
		Scene.v().setEntryPoints(Collections.singletonList(mainMethod));

		// Run the Spark transformer
		SparkTransformer.v().transform("", Transformer.SPARK.options());

		// Retrieve the call graph edges
		CallGraph probecg = new CallGraph();
		soot.jimple.toolkits.callgraph.CallGraph cg = Scene.v().getCallGraph();

		SootMethod m = Scene.v().getMethod(Names.AVERROES_DO_IT_ALL_METHOD_SIGNATURE);
		System.out.println(m.getActiveBody());
		
		for(Local l : m.getActiveBody().getLocals()) {
			
		}
		System.exit(0);
		
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
