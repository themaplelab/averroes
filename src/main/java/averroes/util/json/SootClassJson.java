package averroes.util.json;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import soot.Modifier;
import soot.SootMethod;
import soot.Type;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;

/**
 * A JSON representation for a Soot class.
 *
 * @author Karim Ali
 */
public class SootClassJson {

  private HashMap<String, HashSet<String>> methodToObjectCreations =
      new HashMap<String, HashSet<String>>();
  private HashMap<String, HashSet<String>> methodToInvocations =
      new HashMap<String, HashSet<String>>();
  private HashMap<String, HashSet<String>> methodToFieldReads =
      new HashMap<String, HashSet<String>>();
  private HashMap<String, HashSet<String>> methodToFieldWrites =
      new HashMap<String, HashSet<String>>();

  /**
   * Check whether a method is synthetic or not. This helps ignore checking some methods that the
   * Java compiler generates such as access$0.
   *
   * @param method
   * @return
   */
  private static boolean isSynthetic(SootMethod method) {
    return Modifier.isSynthetic(method.getModifiers());
  }

  /**
   * Add an object creation.
   *
   * @param method
   * @param type
   */
  public void addObjectCreation(SootMethod method, Type type) {
    if (!isSynthetic(method)) {
      methodToObjectCreations.putIfAbsent(method.getSignature(), new HashSet<String>());
      methodToObjectCreations.get(method.getSignature()).add(JsonUtils.toJson(type));
    }
  }

  /**
   * Add a method invocation.
   *
   * @param method
   * @param invoke
   */
  public void addInvocation(SootMethod method, InvokeExpr invoke) {
    if (!isSynthetic(method)) {
      methodToInvocations.putIfAbsent(method.getSignature(), new HashSet<String>());
      methodToInvocations.get(method.getSignature()).add(JsonUtils.toJson(invoke));
    }
  }

  /**
   * Add a field read.
   *
   * @param method
   * @param fieldRef
   */
  public void addFieldRead(SootMethod method, FieldRef fieldRef) {
    if (!isSynthetic(method)) {
      methodToFieldReads.putIfAbsent(method.getSignature(), new HashSet<String>());
      methodToFieldReads.get(method.getSignature()).add(JsonUtils.toJson(fieldRef));
    }
  }

  /**
   * Add a field write.
   *
   * @param method
   * @param fieldRef
   */
  public void addFieldWrite(SootMethod method, FieldRef fieldRef) {
    if (!isSynthetic(method)) {
      methodToFieldWrites.putIfAbsent(method.getSignature(), new HashSet<String>());
      methodToFieldWrites.get(method.getSignature()).add(JsonUtils.toJson(fieldRef));
    }
  }

  /**
   * Is this object equivalent to another SootClassJson (based on the contents of the maps)?
   *
   * @param other
   * @return
   */
  public boolean isEquivalentTo(SootClassJson other) {
    MapDifference<String, HashSet<String>> objectCreationsDifference =
        Maps.difference(methodToObjectCreations, other.methodToObjectCreations);
    if (!objectCreationsDifference.areEqual()) {
      System.out.println("There are some differences in object creations.");
      System.out.println(
          "Methods with different object creations between generated and expected code: "
              + objectCreationsDifference.entriesDiffering());
      System.out.println(
          "Methods with object creations that only show up in generated code: "
              + objectCreationsDifference.entriesOnlyOnLeft());
      System.out.println(
          "Method with object creations that only show up in expected code: "
              + objectCreationsDifference.entriesOnlyOnRight());
      System.out.println();
      System.out.println();
      return false;
    }

    MapDifference<String, HashSet<String>> invocationsDifference =
        Maps.difference(methodToInvocations, other.methodToInvocations);
    if (!invocationsDifference.areEqual()
        && !ignoreCalls(invocationsDifference.entriesOnlyOnLeft())) {
      System.out.println("There are some differences in invocations.");
      System.out.println(
          "Methods with different invocations between generated and expected code: "
              + invocationsDifference.entriesDiffering());
      System.out.println(
          "Methods with invocations that only show up in generated code: "
              + invocationsDifference.entriesOnlyOnLeft());
      System.out.println(
          "Methods with invocations that only show up in expected code: "
              + invocationsDifference.entriesOnlyOnRight());
      System.out.println();
      System.out.println();
      return false;
    }

    MapDifference<String, HashSet<String>> fieldReadsDifference =
        Maps.difference(methodToFieldReads, other.methodToFieldReads);
    if (!fieldReadsDifference.areEqual()) { // &&
      // !onlyGuardReadIsMissing(fieldReadsDifference.entriesOnlyOnLeft()))
      // {
      System.out.println("There are some differences in field reads.");
      System.out.println(
          "Methods with different field reads between generated and expected code: "
              + fieldReadsDifference.entriesDiffering());
      System.out.println(
          "Methods with field reads that only show up in generated code: "
              + fieldReadsDifference.entriesOnlyOnLeft());
      System.out.println(
          "Methods with field reads that only show up in expected code: "
              + fieldReadsDifference.entriesOnlyOnRight());
      System.out.println();
      System.out.println();
      return false;
    }

    MapDifference<String, HashSet<String>> fieldWritesDifference =
        Maps.difference(methodToFieldWrites, other.methodToFieldWrites);
    if (!fieldWritesDifference.areEqual()
        && !ignoreFieldWrites(fieldWritesDifference.entriesOnlyOnLeft())) {
      System.out.println("There are some differences in field writes.");
      System.out.println(
          "Methods with different field writes between generated and expected code: "
              + fieldWritesDifference.entriesDiffering());
      System.out.println(
          "Methods with field writes that only show up in generated code: "
              + fieldWritesDifference.entriesOnlyOnLeft());
      System.out.println(
          "Methods with field writes that only show up in expected code: "
              + fieldWritesDifference.entriesOnlyOnRight());
      System.out.println();
      System.out.println();
      return false;
    }

    // TODO need to also compare array reads/writes

    return true;
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

  private boolean ignoreCalls(Map<String, HashSet<String>> callsDiff) {
    // All method calls are from the default constructor of inner classes and to the default
    // constructor
    // of the superclass. This is a hackish way of ignoring such calls, but necessary because
    // Averroes
    // needs those default constructors to generate valid bytecode that involves casting the LPT
    // to the types required to prepare arguments for methods calls to constructors. Without those
    // default
    // constructors, Averroes will just drop dead.
    return callsDiff.keySet().stream()
            .allMatch(m -> m.contains("$") && m.endsWith("void <init>()>"))
        && callsDiff.values().stream()
            .flatMap(Collection::stream)
            .allMatch(c -> c.endsWith("void <init>()>"));
  }

  private boolean ignoreFieldWrites(Map<String, HashSet<String>> fieldWritesDiff) {
    return fieldWritesDiff.keySet().stream()
        .allMatch(m -> m.contains("$") && m.endsWith("void <init>()>"));
  }

  // private boolean onlyGuardReadIsMissing(Map<String, HashSet<String>>
  // fieldReadsInGeneratedCode) {
  // fieldReadsInGeneratedCode.values().stream().flatMap(Collection::stream).allMatch(s
  // -> s.equalsIgnoreCase(anotherString) || s.equals)
  // }
}
