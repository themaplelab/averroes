/**
 * ***************************************************************************** Copyright (c) 2015
 * Karim Ali and Ondřej Lhoták. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Karim Ali - initial API and implementation and/or initial documentation
 * *****************************************************************************
 */
package averroes.util;

import soot.*;
import soot.coffi.Util;

import java.util.Arrays;
import java.util.List;

public class BytecodeUtils {

  /**
   * Get the parameter types of a method from its descriptor.
   *
   * @param methodDescriptor
   * @return
   */
  public static List<Type> getParameterTypes(String methodDescriptor) {
    Type[] types = Util.v().jimpleTypesOfFieldOrMethodDescriptor(methodDescriptor);
    return Arrays.asList(types).subList(0, types.length - 1);
  }

  /**
   * Get the return type of a method from its descriptor.
   *
   * @param methodDescriptor
   * @return
   */
  public static Type getReturnType(String methodDescriptor) {
    Type[] types = Util.v().jimpleTypesOfFieldOrMethodDescriptor(methodDescriptor);
    return types[types.length - 1];
  }

  public static Type getFieldType(String fieldDescriptor) {
    return Util.v().jimpleTypeOfFieldDescriptor(fieldDescriptor);
  }

  /**
   * Resolve the given bits of a method signature to the corresponding SootMethod from the Soot
   * Scene.
   *
   * @param className
   * @param methodName
   * @param methodDescriptor
   * @return
   */
  public static SootMethod makeSootMethod(
      String className, String methodName, String methodDescriptor) {
    SootClass cls = Scene.v().getSootClass(className);

    List<Type> parameterTypes = getParameterTypes(methodDescriptor);
    Type returnType = getReturnType(methodDescriptor);

    /*
     * Get the method ref and resolve it to a Soot method. We need to resolve to the actual method that will be
     * called at runtime, or might be resolved by a static analysis. Otherwise, we'll just get the interface method.
     */
    SootMethodRef methodRef =
        Scene.v().makeMethodRef(cls, methodName, parameterTypes, returnType, false);
    SootMethod method;

    /*
     * We have to do this ugly code. Try first and see if the method is not static. If it is static, then create a
     * new methodRef in the catch and resolve it again with isStatic = true.
     */
    try {
      method = methodRef.resolve();
    } catch (ResolutionFailedException e) {
      methodRef = Scene.v().makeMethodRef(cls, methodName, parameterTypes, returnType, true);
    }
    method = methodRef.resolve();

    return method;
  }

  public static SootField makeSootField(
      String className, String fieldName, String fieldDescriptor) {
    SootClass cls = Scene.v().getSootClass(className);
    Type fieldType = getFieldType(fieldDescriptor);

    // Get the field ref and resolve it to a Soot field
    SootFieldRef fieldRef = Scene.v().makeFieldRef(cls, fieldName, fieldType, false);
    SootField field;

    /*
     * We have to do this ugly code. Try first and see if the field is not static. If it is static, then create a
     * new fieldRef in the catch and resolve it again with isStatic = true.
     */
    try {
      field = fieldRef.resolve();
    } catch (ResolutionFailedException e) {
      fieldRef = Scene.v().makeFieldRef(cls, fieldName, fieldType, true);
    }
    field = fieldRef.resolve();

    return field;
  }
}
