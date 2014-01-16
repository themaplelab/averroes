package ca.uwaterloo.averroes.callgraph.transformers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import probe.CallGraph;
import probe.ObjectManager;
import probe.ProbeClass;
import probe.ProbeMethod;
import soot.G;
import soot.Kind;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.data.AndroidMethod;
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
		// Options.v().set_src_prec(Options.src_prec_apk);

		// output as APK, too//-f J
		// Options.v().set_output_format(Options.output_format_dex);

		// the android apk
		// Options.v().set_android_jars("android");

		// the input application
		// List<String> dirs = new ArrayList<String>();
		// dirs.add(apkFileLocation);
		// Options.v().set_process_dir(dirs);

		// Set some soot parameters
		// Options.v().set_prepend_classpath(true);
		// Options.v().set_soot_classpath("android/smali-1.4.2.jar");
		// Options.v().set_dynamic_class(AverroesProperties.getDynamicClasses());
		// Options.v().classes().addAll(organizer.applicationClassNames());
		// Options.v().set_main_class(AverroesProperties.getMainClass());
		// Options.v().set_whole_program(true);

		// Load the necessary classes
		// Scene.v().loadNecessaryClasses();
		// Scene.v().setMainClassFromOptions();

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
		
		
		
		SootMethod dummyMain = app.getEntryPointCreator().createDummyMain();
		System.out.println(dummyMain);
		System.exit(0);
		
//		for (String key : app.callbackMethods.keySet()) {
//			System.out.println(key);
//			for (AndroidMethod m : app.callbackMethods.get(key)) {
//				System.out.println("\t" + m);
//			}
//		}
		
//		Set<AndroidMethod> callbackMethods = new HashSet<AndroidMethod>();
//		for(Set<AndroidMethod> s : app.callbackMethods.values()) {
//			callbackMethods.addAll(s);
//		}
		
		
		
//		for(AndroidMethod m : callbackMethods) {
//			SootMethod sootMethod = Scene.v().getMethod(m.toString());
//			entrypoints.add(sootMethod);
//		}
		

		// Set the main class
//		SootClass mainClass = Scene.v().getSootClass("ca.uwaterloo.helloworld.MainActivity");
//		SootMethod onCreate = mainClass.getMethod("void onCreate(android.os.Bundle)");
//		System.out.println(onCreate);

		// Set entry points
//		List<SootMethod> entryPoints = new ArrayList<SootMethod>();
//		entryPoints.add(onCreate);

		System.out.println(Scene.v().getEntryPoints());
		// System.out.println(Scene.v().getMainClass());
		// System.out.println(Scene.v().getMainMethod());
		// System.exit(0);

		// Run the Spark transformer
		SparkTransformer.v().transform("", Transformer.SPARK.options());

		// Retrieve the call graph edges
		CallGraph probecg = new CallGraph();
		soot.jimple.toolkits.callgraph.CallGraph cg = Scene.v().getCallGraph();

		Iterator<soot.jimple.toolkits.callgraph.Edge> it = cg.listener();
		while (it.hasNext()) {
			soot.jimple.toolkits.callgraph.Edge e = it.next();
			System.out.println(e.toString()); // TODO
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
