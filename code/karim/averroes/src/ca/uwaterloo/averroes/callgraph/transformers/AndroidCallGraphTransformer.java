package ca.uwaterloo.averroes.callgraph.transformers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

public class AndroidCallGraphTransformer {
	// private JarOrganizer organizer;

	public AndroidCallGraphTransformer() {
		// organizer = new JarOrganizer();
	}

	// @SuppressWarnings("unchecked")
	public CallGraph getProbeCallGraph() throws IOException {
		System.out.println("Generating the call graph for an Android apk.");

		// Reset Soot
		G.reset();
		// organizer.organizeInputJarFiles();

		// prefer Android APK files// -src-prec apk
		Options.v().set_src_prec(Options.src_prec_apk);

		// output as APK, too//-f J
		Options.v().set_output_format(Options.output_format_dex);

		// the android apk
		Options.v().set_android_jars("android");

		// the input application
		List<String> dirs = new ArrayList<String>();
		dirs.add("droidbench/ArraysAndLists_ArrayAccess1.apk");
		Options.v().set_process_dir(dirs);

		// Set some soot parameters
		Options.v().set_prepend_classpath(true);
		Options.v().set_soot_classpath("android/smali-1.4.2.jar");
		// Options.v().set_dynamic_class(AverroesProperties.getDynamicClasses());
		// Options.v().classes().addAll(organizer.applicationClassNames());
		// Options.v().set_main_class(AverroesProperties.getMainClass());
		Options.v().set_whole_program(true);

		System.out.println(Scene.v().getSootClassPath());

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
