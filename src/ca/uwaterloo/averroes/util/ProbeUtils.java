package ca.uwaterloo.averroes.util;

import java.util.StringTokenizer;

import org.apache.bcel.classfile.Utility;

import probe.ObjectManager;
import probe.ProbeClass;
import probe.ProbeMethod;

/**
 * A utility class for Probe.
 * 
 * @author karim
 * 
 */
public class ProbeUtils {

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
	 * Get the declaring class of a method given its signature. Note: I copied this as is from soot.Scene because I
	 * don't want to depend on the Scene for this utility method.
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
	 * Get the subsignature of a method given its signature. Note: I copied this as is from soot.Scene because I don't
	 * want to depend on the Scene for this utility method.
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

}
