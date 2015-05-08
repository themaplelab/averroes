package ca.uwaterloo.averroes.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.bcel.classfile.Utility;
import org.deri.iris.storage.IRelation;

import probe.CallEdge;
import probe.CallGraph;
import probe.ObjectManager;
import probe.ProbeClass;
import probe.ProbeMethod;
import probe.TextWriter;
import soot.SootMethod;
import ca.uwaterloo.averroes.callgraph.CallGraphSource;
import ca.uwaterloo.averroes.callgraph.gxl.GXLReader;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.soot.Names;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.impl.BasicCallGraph;

/**
 * A utility class for Probe.
 * 
 * @author karim
 * 
 */
public class ProbeUtils {

	public static final ProbeMethod LIBRARY_BLOB = ObjectManager.v().getMethod(
			ObjectManager.v().getClass(Names.AVERROES_LIBRARY_CLASS), Names.BLOB, "");

	/**
	 * Convert the generated Doop call graph to summarized Probe version.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static CallGraph convertDoopCallGraph(String doopHome) throws IOException {
		CallGraph probe = new CallGraph();
		IRelation edges = ResultImporter.getDoopCallGraphEdges(doopHome);
		IRelation entryPoints = ResultImporter.getDoopEntryPoints(doopHome);

		// Create the graph entry points
		for (int i = 0; i < entryPoints.size(); i++) {
			String methodSignature = (String) entryPoints.get(i).get(0).getValue();
			ProbeMethod method = createProbeMethodBySignature(methodSignature);
			probe.entryPoints().add(method);
		}

		// Create the edges according to the app_includes parameter
		for (int i = 0; i < edges.size(); i++) {
			String srcName = (String) edges.get(i).get(0).getValue();
			String dstName = (String) edges.get(i).get(2).getValue();

			ProbeMethod src = createProbeMethodBySignature(srcName);
			ProbeMethod dst = createProbeMethodBySignature(dstName);

			boolean isSrcApp = AverroesProperties.isApplicationMethod(src);
			boolean isDstApp = AverroesProperties.isApplicationMethod(dst);

			if (isSrcApp && isDstApp) {
				probe.edges().add(new CallEdge(src, dst));
			} else if (isSrcApp && !isDstApp) {
				probe.edges().add(new CallEdge(src, LIBRARY_BLOB));
			} else if (!isSrcApp && isDstApp) {
				probe.edges().add(new CallEdge(LIBRARY_BLOB, dst));
			}
		}

		return probe;
	}

	/**
	 * Convert a WALA call graph to a probe call graph.
	 * 
	 * @param walaCallGraph
	 * @return
	 */
	public static CallGraph convertWalaCallGraph(BasicCallGraph<?> walaCallGraph) {
		CallGraph probeGraph = new CallGraph();

		// Get the entry points
		for (CGNode entrypoint : walaCallGraph.getEntrypointNodes()) {
			probeGraph.entryPoints().add(ProbeUtils.probeMethod(entrypoint));
		}

		// Edges from fake root clinit node are also entry points
		CGNode root = walaCallGraph.getFakeRootNode();
		CGNode clinit = walaCallGraph.getFakeWorldClinitNode();
		Iterator<CGNode> moreEntryPoints = walaCallGraph.getSuccNodes(clinit);
		while (moreEntryPoints.hasNext()) {
			CGNode node = moreEntryPoints.next();
			ProbeMethod dst = ProbeUtils.probeMethod(node);
			probeGraph.entryPoints().add(dst);
		}

		// Get the edges
		for (CGNode node : walaCallGraph) {
			// Ignore edges from fakeRootNode, they have already been added as
			// entry points.
			// Also ignore edges from fakeWorldClinit
			// if(node.getMethod().getName().toString().equals("doItAll")) {
			// System.out.println("====================");
			// System.out.println(node);
			// System.out.println(node.getIR() + "\n\n");
			// }
			if (!node.equals(root) && !node.equals(clinit)) {
				Iterator<CGNode> successors = walaCallGraph.getSuccNodes(node);
				ProbeMethod src = ProbeUtils.probeMethod(node);

				while (successors.hasNext()) {
					CGNode succ = successors.next();
					// if(node.getMethod().getName().toString().equals("doItAll"))
					// {
					// System.out.println(succ);
					// }
					ProbeMethod dst = ProbeUtils.probeMethod(succ);
					probeGraph.edges().add(new CallEdge(src, dst));
				}
			}
		}

		return probeGraph;
	}

	/**
	 * Convert the report produced by the WALA dynamic call graph generator to a
	 * probe call graph.
	 * 
	 * @param dynamicCallGraphReport
	 * @return
	 * @throws IOException
	 */
	public static CallGraph convertWalaDynamicCallGraph(String dynamicCGFile) throws IOException {
		BufferedReader dynamicEdgesFile = new BufferedReader(new InputStreamReader(new GZIPInputStream(
				new FileInputStream(dynamicCGFile))));
		CallGraph probecg = new CallGraph();
		String line;
		while ((line = dynamicEdgesFile.readLine()) != null) {
			StringTokenizer edge = new StringTokenizer(line, "\t");
			String callerClass = edge.nextToken();

			// Lines that start with "root" are entry points
			if ("root".equals(callerClass)) {
				String cls = edge.nextToken().replaceAll("/", ".");
				String subSig = edge.nextToken();
				probecg.entryPoints().add(probeMethod(cls, subSig));
			} else { // a normal edge
				String callerSubSig = edge.nextToken();
				String calleeClass = edge.nextToken().replaceAll("/", ".");
				String calleeSubSig = edge.nextToken();
				callerClass = callerClass.replaceAll("/", ".");

				ProbeMethod src = probeMethod(callerClass, callerSubSig);
				ProbeMethod dst = probeMethod(calleeClass, calleeSubSig);
				probecg.edges().add(new CallEdge(src, dst));
			}

		}

		dynamicEdgesFile.close();

		return probecg;
	}
	
	/**
	 * Convert from the Averroes probe format to the standard probe format.
	 * 
	 * @param aveCallGraph
	 * @return
	 */
	public static CallGraph collapse(ca.uwaterloo.averroes.callgraph.CallGraph aveCallGraph) {
		CallGraph result = new CallGraph();

		// Add the entry points
		aveCallGraph.entryPoints().forEach(entry -> result.entryPoints().add(entry));

		// Add the edges appropriately
		aveCallGraph.appToAppEdges().forEach(edge -> result.edges().add(edge));
		aveCallGraph.appToLibEdges().forEach(src -> result.edges().add(new CallEdge(src, LIBRARY_BLOB)));
		aveCallGraph.libToAppEdges().forEach(dst -> result.edges().add(new CallEdge(LIBRARY_BLOB, dst)));

		return result;
	}

	/**
	 * Collapse the given probe call graph to an averroes call graph that
	 * summarizes the library in one blob.
	 * 
	 * @param probe
	 * @return
	 */
	public static CallGraph collapse(CallGraph probe) {
		CallGraph result = new CallGraph();

		// Add the entry points
		probe.entryPoints().forEach(entry -> result.entryPoints().add(entry));

		// Getting the correct placement for each edge
		// Note: If both src and dst of an edge are in the library, ignore it.
		for (CallEdge edge : probe.edges()) {
			ProbeMethod src = edge.src();
			ProbeMethod dst = edge.dst();

			/*
			 * We don't care about the following edges (primarily used for
			 * converting dynamic call graphs) 1) edges to <clinit> methods 2)
			 * edges to java.lang.ClassLoader:
			 * loadClassInternal(Ljava/lang/String;) 3) edges to
			 * java.lang.ClassLoader:
			 * checkPackageAccess(Ljava/lang/Class;Ljava/security
			 * /ProtectionDomain;)
			 */
			if (!isClinit(dst) && !isLoadClassInternal(dst) && !isCheckPackageAccess(dst)) {
				boolean isSrcApp = AverroesProperties.isApplicationMethod(src);
				boolean isDstApp = AverroesProperties.isApplicationMethod(dst);

				if (isSrcApp && isDstApp) {
					result.edges().add(edge);
				} else if (isSrcApp && !isDstApp) {
					result.edges().add(new CallEdge(src, LIBRARY_BLOB));
				} else if (!isSrcApp && isDstApp) {
					result.edges().add(new CallEdge(LIBRARY_BLOB, dst));
				}
			}
		}

		return result;
	}

	/**
	 * Create a probe method given the bytecode signature of the method.
	 * 
	 * @param methodSignature
	 * @return
	 */
	public static ProbeMethod createProbeMethodBySignature(String methodSignature) {
		String methodDeclaringClass = sootSignatureToMethodDeclaringClass(methodSignature);
		String name = sootSignatureToMethodName(methodSignature);
		String bcSig = sootSignatureToMethodArguments(methodSignature, true);

		ProbeClass cls = ObjectManager.v().getClass(methodDeclaringClass);

		return ObjectManager.v().getMethod(cls, name, bcSig);
	}

	/**
	 * Get the method arguments given a Soot method signature.
	 * 
	 * @param sootSignature
	 * @param isInBCFormat
	 * @return
	 */
	public static String sootSignatureToMethodArguments(String sootSignature, boolean isInBCFormat) {
		String sub = signatureToSubsignature(sootSignature);
		String args = sub.substring(sub.indexOf('(') + 1, sub.indexOf(')'));

		if (isInBCFormat) {
			StringBuffer buffer = new StringBuffer();
			StringTokenizer strTok = new StringTokenizer(args, ",");

			while (strTok.hasMoreTokens()) {
				buffer.append(Utility.getSignature(strTok.nextToken().trim()));
			}

			return buffer.toString();
		} else {
			return args;
		}
	}

	/**
	 * Get the method return type given its Soot signature.
	 * 
	 * @param sootSignature
	 * @param isInBCFormat
	 * @return
	 */
	public static String sootSignatureToMethodReturnType(String sootSignature, boolean isInBCFormat) {
		String sub = signatureToSubsignature(sootSignature);
		String type = sub.substring(0, sub.indexOf(" "));

		return isInBCFormat ? Utility.getSignature(type) : type;
	}

	/**
	 * Get the declaring class of a method given its signature. Note: I copied
	 * this as is from soot.Scene because I don't want to depend on the Scene
	 * for this utility method.
	 * 
	 * @param sootSignature
	 * @return
	 */
	public static String sootSignatureToMethodDeclaringClass(String sootSignature) {
		if (sootSignature.charAt(0) != '<') {
			throw new RuntimeException("oops " + sootSignature);
		}

		if (sootSignature.charAt(sootSignature.length() - 1) != '>') {
			throw new RuntimeException("oops " + sootSignature);
		}

		int index = sootSignature.indexOf(":");

		if (index < 0) {
			throw new RuntimeException("oops " + sootSignature);
		}

		return sootSignature.substring(1, index);
	}

	/**
	 * Get the subsignature of a method given its signature. Note: I copied this
	 * as is from soot.Scene because I don't want to depend on the Scene for
	 * this utility method.
	 * 
	 * @param sootSignature
	 * @return
	 */
	public static String signatureToSubsignature(String sootSignature) {
		if (sootSignature.charAt(0) != '<') {
			throw new RuntimeException("oops " + sootSignature);
		}

		if (sootSignature.charAt(sootSignature.length() - 1) != '>') {
			throw new RuntimeException("oops " + sootSignature);
		}

		int index = sootSignature.indexOf(":");

		if (index < 0) {
			throw new RuntimeException("oops " + sootSignature);
		}

		return sootSignature.substring(index + 2, sootSignature.length() - 1);
	}

	/**
	 * Get the method name from a Soot method signature.
	 * 
	 * @param sootSignature
	 * @return
	 */
	public static String sootSignatureToMethodName(String sootSignature) {
		String sub = signatureToSubsignature(sootSignature);
		String name = sub.substring(sub.indexOf(" ") + 1, sub.indexOf('('));
		return name;
	}

	/**
	 * Convert a Soot method signature to a bytecode signature.
	 * 
	 * @param sootSignature
	 * @return
	 */
	public static String sootSignatureToBytecodeSignature(String sootSignature) {
		String cls = sootSignatureToMethodDeclaringClass(sootSignature);
		String name = sootSignatureToMethodName(sootSignature);
		String args = sootSignatureToMethodArguments(sootSignature, true);
		String ret = sootSignatureToMethodReturnType(sootSignature, true);

		StringBuffer buffer = new StringBuffer();
		buffer.append("<");
		buffer.append(cls + ": ");
		buffer.append(name);
		buffer.append("(" + args + ")");
		buffer.append(ret);
		buffer.append(">");

		return buffer.toString();
	}

	/**
	 * Get a probe method given its signature.
	 * 
	 * @param signature
	 * @return
	 */
	public static ProbeMethod probeMethod(String signature) {
		/*
		 * A method that has the following signature tests.Match1:
		 * handleArgs(args: List[String]) has declaring class = tests.Match1,
		 * name = handleArgs, and descriptor = (args: List[String])
		 */
		int colon = signature.indexOf(':');
		int leftBracket = signature.indexOf('(');
		int rightBracket = signature.lastIndexOf(')');

		String decCls = signature.substring(0, colon);
		String name = signature.substring(colon + 2, leftBracket);
		String descriptor = signature.substring(leftBracket + 1, rightBracket);
		ProbeClass cls = ObjectManager.v().getClass(decCls);

		return ObjectManager.v().getMethod(cls, name, descriptor);
	}

	/**
	 * Get a probe method given the name of its class and the method
	 * subsignature.
	 * 
	 * @param cls
	 * @param method
	 * @return
	 */
	public static ProbeMethod probeMethod(String cls, String subsignature) {
		return probeMethod(cls + ": " + subsignature);
	}

	/**
	 * Get a probe method from a WALA call graph node.
	 * 
	 * @param node
	 * @return
	 */
	public static ProbeMethod probeMethod(CGNode node) {
		return probeMethod(node.getMethod());
	}

	/**
	 * Get a probe method from a WALA method.
	 * 
	 * @param method
	 * @return
	 */
	public static ProbeMethod probeMethod(IMethod method) {
		/*
		 * A method that has the following signature java.lang.Object.<init>()V
		 * has declaring class = java.lang.Object, name = <init>, and descriptor
		 * = ()
		 */
		String signature = method.getSignature();
		int leftBracket = signature.indexOf('(');
		int rightBracket = signature.lastIndexOf(')');
		int dot = signature.substring(0, leftBracket).lastIndexOf('.');

		String decCls = signature.substring(0, dot);
		String name = method.getName().toString();
		String descriptor = signature.substring(leftBracket + 1, rightBracket);
		ProbeClass cls = ObjectManager.v().getClass(decCls);

		return ObjectManager.v().getMethod(cls, name, descriptor);
	}

	public static boolean isClinit(ProbeMethod method) {
		return method.name().equalsIgnoreCase(SootMethod.staticInitializerName);
	}

	public static boolean isLoadClassInternal(ProbeMethod method) {
		return method.toString().equalsIgnoreCase("java.lang.ClassLoader: loadClassInternal(Ljava/lang/String;)");
	}

	public static boolean isCheckPackageAccess(ProbeMethod method) {
		return method.toString().equalsIgnoreCase(
				"java.lang.ClassLoader: checkPackageAccess(Ljava/lang/Class;Ljava/security/ProtectionDomain;)");
	}

	public static void main(String[] args) {
		try {
			ca.uwaterloo.averroes.callgraph.CallGraph aveCallGraph = new GXLReader().readCallGraph(new FileInputStream(
					args[0]), CallGraphSource.DUMMY);
			CallGraph probe = collapse(aveCallGraph);
			new TextWriter().write(probe,
					new GZIPOutputStream(new FileOutputStream(args[0].replace(".gxl", ".txt.gzip"))));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
