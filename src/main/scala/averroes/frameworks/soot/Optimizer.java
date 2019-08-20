package averroes.frameworks.soot;

import averroes.soot.SootSceneUtil;
import averroes.util.io.Printers;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import soot.ArrayType;
import soot.Body;
import soot.Local;
import soot.PackManager;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Transform;
import soot.Trap;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JInstanceOfExpr;
import soot.jimple.internal.JNewArrayExpr;
import soot.jimple.internal.JNewExpr;
import soot.jimple.internal.JNewMultiArrayExpr;
import soot.jimple.toolkits.invoke.SiteInliner;

public class Optimizer {
  static final boolean DEBUG = false;

  /** Return true if it is possible for the application to override this library method. */
  public static boolean isOverridable(SootMethod method) {
    if (method.isFinal()) return false;
    if (method.isPrivate()) return false;

    if (method.isPublic()) return true;
    return method.isProtected();

    // TODO: Check whether method is overridden (in library) by any other overridable method.
    // If yes, then return true because if the app overrides the overriding method, then
    // it also overrides this one.
  }

  public void optimize() {
    for (int i = 0; i < 5; i++) {
      new CHABuilder().run();
      ((Transform) PackManager.v().getPhase("wjtp.si")).apply();
    }

    new CHABuilder().run();

    removeUnreachableMethods();

    LocalOptimizer.apply();

    removeUnusedFields();

    replaceEmptyClasses();

    validate();
  }

  public void removeUnreachableMethods() {
    Set<String> signaturesToBeKept = new HashSet();
    signaturesToBeKept.add("void writeObject(java.io.ObjectOutputStream)");
    signaturesToBeKept.add("void readObject(java.io.ObjectInputStream)");
    signaturesToBeKept.add("void readObjectNoData()");

    Set<SootMethod> reachables = new ReachableMethodsFinder().apply();
    for (SootClass cls : SootSceneUtil.getClasses()) {
      //            System.out.println("removing unreachable methods in "+cls);
      for (SootMethod method : cls.getMethods()) {
        //                System.out.println(method.getSubSignature());
        //                System.out.println(method+" is reachable? "+reachables.contains(method));
        if (!reachables.contains(method)
            && !signaturesToBeKept.contains(method.getSubSignature())) {
          Printers.logInliningInfo(
              "removing unreachable method " + method + " from class " + cls, method);
          cls.removeMethod(method);
        }
      }
    }
  }

  void removeUnusedFields() {
    Set<SootField> usedFields = new HashSet<SootField>();
    for (SootClass cls : SootSceneUtil.getClasses()) {
      for (SootMethod method : cls.getMethods()) {
        if (method.hasActiveBody()) {
          Body body = method.getActiveBody();
          for (ValueBox box : body.getUseAndDefBoxes()) {
            Value v = box.getValue();
            if (v instanceof FieldRef) {
              FieldRef fieldRef = (FieldRef) v;
              usedFields.add(fieldRef.getField());
            }
          }
        }
      }
    }
    for (SootClass cls : SootSceneUtil.getClasses()) {
      for (SootField fld : new ArrayList<SootField>(cls.getFields())) {
        if (fld.isPublic() || usedFields.contains(fld)) continue;
        cls.removeField(fld);
      }
    }
  }

  private boolean hasMethodsOrFields(SootClass cls) {
    if (!cls.getFields().isEmpty()) {
      //            System.out.println("class "+cls+" has fields "+cls.getFields());
      return true;
    }
    for (SootMethod method : cls.getMethods()) {
      if (method.isConstructor()) continue;
      //            System.out.println("class "+cls+" has method "+method);
      return true;
    }
    return false;
  }

  public void replaceEmptyClasses() {
    for (SootClass cls : new ArrayList<SootClass>(SootSceneUtil.getClasses())) {
      if (!cls.isPublic() && !hasMethodsOrFields(cls)) {
        if (DEBUG) System.out.println("folding class " + cls + " into " + cls.getSuperclass());
        inlineConstructorCalls(cls);
        new ClassReplacer(cls, cls.getSuperclass()).apply();
      }
    }
  }

  public void inlineConstructorCalls(SootClass toRemove) {
    //        System.out.println("inlining constructors of empty class "+toRemove);

    for (SootClass cls : SootSceneUtil.getClasses()) {
      for (SootMethod method : cls.getMethods()) {
        if (method.isConcrete()) {
          ArrayList<Stmt> sites = new ArrayList<Stmt>();
          ArrayList<SootMethod> targets = new ArrayList<SootMethod>();
          Body body = method.getActiveBody();
          for (Unit u : body.getUnits()) {
            Stmt s = (Stmt) u;
            if (s.containsInvokeExpr()) {
              InvokeExpr ie = s.getInvokeExpr();
              SootMethod target = ie.getMethod();
              if (target.isConstructor() && target.getDeclaringClass().equals(toRemove)) {
                sites.add(s);
                targets.add(target);
              }
            }
          }
          for (int i = 0; i < sites.size(); i++) {
            Printers.logInliningInfo(
                "inlining constructor call " + sites.get(i) + " into " + method, method);
            SiteInliner.inlineSite(targets.get(i), sites.get(i), method);
          }
        }
      }
    }
  }

  void validate() {
    for (SootClass cls : SootSceneUtil.getClasses()) {
      cls.validate();
      for (SootMethod method : cls.getMethods()) {
        if (method.hasActiveBody()) {
          method.getActiveBody().validate();
        }
      }
    }
  }

  class ReachableMethodsFinder {
    private Set<SootMethod> reachables = new HashSet();

    private void makeReachable(SootMethod method) {
      if (reachables.add(method)) {
        if (method.isConcrete()) {
          Body body = method.getActiveBody();
          for (Unit u : body.getUnits()) {
            Stmt s = (Stmt) u;
            if (((Stmt) u).containsInvokeExpr()) {
              InvokeExpr ie = ((Stmt) u).getInvokeExpr();
              SootMethod target = ie.getMethod();
              if (target.getDeclaringClass().isApplicationClass()) {
                logReachable(method, "it is called by " + method);
                makeReachable(target);
              }
            }
          }
        }
      }
    }

    public Set<SootMethod> apply() {
      for (SootClass cls : SootSceneUtil.getClasses()) {
        for (SootMethod method : cls.getMethods()) {
          if (isOverridable(method)) {
            logReachable(method, "it is overridable");
            makeReachable(method);
          }
        }
      }
      return reachables;
    }

    private void logReachable(SootMethod method, String reason) {
      if (!reachables.contains(method)) {
        //                System.out.println(method + " is reachable because " + reason);
      }
    }
  }
}

class ClassReplacer {
  SootClass original;
  SootClass replacement;

  ClassReplacer(SootClass original, SootClass replacement) {
    this.original = original;
    this.replacement = replacement;
  }

  private Type replacementType(Type originalType) {
    if (original.getType().equals(originalType)) return replacement.getType();
    else if (originalType instanceof ArrayType) {
      ArrayType arrayType = (ArrayType) originalType;
      if (original.getType().equals(arrayType.getArrayElementType()))
        return ArrayType.v(replacement.getType(), arrayType.numDimensions);
    }
    return null;
  }

  private Type replacementOrOriginal(Type originalType) {
    Type ret = replacementType(originalType);
    if (ret == null) return originalType;
    else return ret;
  }

  private SootClass replacementOrOriginal(SootClass originalClass) {
    if (original.equals(originalClass)) return replacement;
    else return originalClass;
  }

  void apply() {
    Scene.v().removeClass(original);
    for (SootClass cls : SootSceneUtil.getClasses()) {
      apply(cls);
    }
  }

  void apply(SootClass cls) {
    if (cls.hasOuterClass() && original.equals(cls.getOuterClass())) cls.setOuterClass(replacement);
    if (original.equals(cls.getSuperclass())) cls.setSuperclass(replacement);
    if (cls.implementsInterface(original.getName())) {
      cls.removeInterface(original);
      if (!cls.implementsInterface(replacement.getName())) cls.addInterface(replacement);
    }
    for (SootMethod method : cls.getMethods()) {
      apply(method);
    }
    for (SootField field : cls.getFields()) {
      apply(field);
    }
  }

  void apply(SootMethod method) {
    if (method.throwsException(original)) {
      method.removeException(original);
      method.addExceptionIfAbsent(replacement);
    }

    Type type = replacementType(method.getReturnType());
    if (type != null) method.setReturnType(type);

    method.setParameterTypes(apply(method.getParameterTypes()));

    if (method.hasActiveBody()) {
      apply(method.getActiveBody());
    }
  }

  List<Type> apply(List<Type> types) {
    List<Type> ret = new ArrayList<>();
    for (Type type : types) {
      ret.add(replacementOrOriginal(type));
    }
    return ret;
  }

  void apply(Body body) {
    for (Local local : body.getLocals()) apply(local);
    for (Unit unit : body.getUnits()) apply((Stmt) unit);
    for (Trap trap : body.getTraps()) apply(trap);
  }

  void apply(Local local) {
    Type type = replacementType(local.getType());
    if (type != null) local.setType(type);
  }

  void apply(Stmt s) {
    for (ValueBox box : s.getUseAndDefBoxes()) apply(box);
  }

  void apply(Trap trap) {
    if (trap.getException().equals(original)) trap.setException(replacement);
  }

  void apply(ValueBox box) {
    Value v = box.getValue();
    if (v instanceof JCastExpr) {
      JCastExpr jce = (JCastExpr) v;
      Type type = replacementType(jce.getCastType());
      if (type != null) box.setValue(new JCastExpr(jce.getOp(), type));
    } else if (v instanceof JInstanceOfExpr) {
      JInstanceOfExpr jioe = (JInstanceOfExpr) v;
      Type type = replacementType(jioe.getCheckType());
      if (type != null) box.setValue(new JInstanceOfExpr(jioe.getOp(), type));
    } else if (v instanceof JNewExpr) {
      JNewExpr jne = (JNewExpr) v;
      Type type = replacementType(jne.getType());
      if (type != null) box.setValue(new JNewExpr((RefType) type));
    } else if (v instanceof JNewArrayExpr) {
      JNewArrayExpr jnae = (JNewArrayExpr) v;
      Type type = replacementType(jnae.getType());
      if (type != null) box.setValue(new JNewArrayExpr(type, jnae.getSize()));
    } else if (v instanceof JNewMultiArrayExpr) {
      JNewMultiArrayExpr jnmae = (JNewMultiArrayExpr) v;
      Type type = replacementType(jnmae.getType());
      if (type != null) box.setValue(new JNewMultiArrayExpr((ArrayType) type, jnmae.getSizes()));
    } else if (v instanceof InvokeExpr) {
      InvokeExpr invokeExpr = (InvokeExpr) v;
      SootMethodRef methodRef = invokeExpr.getMethodRef();
      invokeExpr.setMethodRef(
          Scene.v()
              .makeMethodRef(
                  replacementOrOriginal(methodRef.declaringClass()),
                  methodRef.name(),
                  apply(methodRef.parameterTypes()),
                  replacementOrOriginal(methodRef.returnType()),
                  methodRef.isStatic()));
    } else if (v instanceof FieldRef) {
      FieldRef fieldRef = (FieldRef) v;
      SootFieldRef sfr = fieldRef.getFieldRef();
      fieldRef.setFieldRef(
          Scene.v()
              .makeFieldRef(
                  replacementOrOriginal(sfr.declaringClass()),
                  sfr.name(),
                  replacementOrOriginal(sfr.type()),
                  sfr.isStatic()));
    }
  }

  void apply(SootField field) {
    Type type = replacementType(field.getType());
    if (type != null) field.setType(type);
  }
}
