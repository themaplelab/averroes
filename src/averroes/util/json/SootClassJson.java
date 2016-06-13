package averroes.util.json;

import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.Maps;

import soot.SootMethod;
import soot.Type;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;

/**
 * A JSON representation for a Soot class.
 * 
 * @author Karim Ali
 *
 */
public class SootClassJson {

	private HashMap<String, HashSet<String>> methodToObjectCreations = new HashMap<String, HashSet<String>>();
	private HashMap<String, HashSet<String>> methodToInvocations = new HashMap<String, HashSet<String>>();
	private HashMap<String, HashSet<String>> methodToFieldReads = new HashMap<String, HashSet<String>>();
	private HashMap<String, HashSet<String>> methodToFieldWrites = new HashMap<String, HashSet<String>>();

	/**
	 * Add an object creation.
	 * 
	 * @param method
	 * @param type
	 */
	public void addObjectCreation(SootMethod method, Type type) {
		methodToObjectCreations.putIfAbsent(method.getSignature(), new HashSet<String>());
		methodToObjectCreations.get(method.getSignature()).add(JsonUtils.toJson(type));
	}

	/**
	 * Add a method invocation.
	 * 
	 * @param method
	 * @param invoke
	 */
	public void addInvocation(SootMethod method, InvokeExpr invoke) {
		methodToInvocations.putIfAbsent(method.getSignature(), new HashSet<String>());
		methodToInvocations.get(method.getSignature()).add(JsonUtils.toJson(invoke));
	}

	/**
	 * Add a field read.
	 * 
	 * @param method
	 * @param fieldRef
	 */
	public void addFieldRead(SootMethod method, FieldRef fieldRef) {
		methodToFieldReads.putIfAbsent(method.getSignature(), new HashSet<String>());
		methodToFieldReads.get(method.getSignature()).add(JsonUtils.toJson(fieldRef));
	}

	/**
	 * Add a field write.
	 * 
	 * @param method
	 * @param fieldRef
	 */
	public void addFieldWrite(SootMethod method, FieldRef fieldRef) {
		methodToFieldWrites.putIfAbsent(method.getSignature(), new HashSet<String>());
		methodToFieldWrites.get(method.getSignature()).add(JsonUtils.toJson(fieldRef));
	}

	/**
	 * Is this object equivalent to another SootClassJson (based on the contents
	 * of the maps)?
	 * 
	 * @param other
	 * @return
	 */
	public boolean isEquivalentTo(SootClassJson other) {
		return Maps.difference(methodToObjectCreations, other.methodToObjectCreations).areEqual()
				&& Maps.difference(methodToInvocations, other.methodToInvocations).areEqual()
				&& Maps.difference(methodToFieldReads, other.methodToFieldReads).areEqual()
				&& Maps.difference(methodToFieldWrites, other.methodToFieldWrites).areEqual();
	}

	public HashMap<String, HashSet<String>> getMethodToObjectCreations() {
		return methodToObjectCreations;
	}

	public HashMap<String, HashSet<String>> getMethodToInvocations() {
		return methodToInvocations;
	}

	public HashMap<String, HashSet<String>> getMethodToFieldReads() {
		return methodToFieldReads;
	}

	public HashMap<String, HashSet<String>> getMethodToFieldWrites() {
		return methodToFieldWrites;
	}
}
