package ca.uwaterloo.averroes.callgraph.transformers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import probe.CallGraph;
import probe.ObjectManager;
import probe.ProbeClass;
import probe.ProbeMethod;
import soot.Kind;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.spark.SparkTransformer;
import soot.options.Options;

public class AndroidCallGraphTransformer {
	// @SuppressWarnings("unchecked")
	public CallGraph run() throws IOException {
		System.out.println("Generating the call graph for an Android apk.");

		// Stuff from infoflow-android
		String androidJar = "android";
		String apkFileLocation = "droidbench/HelloWorld-debug-unaligned.apk";
		String path = apkFileLocation + File.pathSeparator + Scene.v().getAndroidJarPath(androidJar, apkFileLocation);

		SetupApplication app = new SetupApplication("android", apkFileLocation);
		app.calculateSourcesSinksEntrypoints("SourcesAndSinks.txt");

		// Initialize soot
		soot.G.reset();
		Options.v().set_no_bodies_for_excluded(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_whole_program(true);
		Options.v().set_soot_classpath(path);
		Options.v().set_src_prec(Options.src_prec_apk);
		soot.options.Options.v().set_android_jars(androidJar);
		Scene.v().loadNecessaryClasses();

		List<String> entryPoints = new ArrayList<String>();

		for (String key : app.getEntryPointCreator().getCallbackFunctions().keySet()) {
			System.out.println(key);
			for (String m : app.getEntryPointCreator().getCallbackFunctions().get(key)) {
				System.out.println("\t" + m);
				entryPoints.add(m);
			}
		}

		SootMethod dummyMain = app.getEntryPointCreator().createDummyMain(entryPoints);
		Scene.v().setEntryPoints(Collections.singletonList(dummyMain));

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
