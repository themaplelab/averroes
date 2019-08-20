/**
 * ***************************************************************************** Copyright (c) 2015
 * Karim Ali and Ondřej Lhoták. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Karim Ali - initial API and implementation and/or initial documentation
 * *****************************************************************************
 */
package averroes.tamiflex;

/**
 * An enumeration for all the reflective call types Averroes uses from TamiFlex.
 *
 * @author karim
 */
public enum ReflectiveCallType {
  ARRAY_NEW_INSTANCE("Array.newInstance"),
  CLASS_FOR_NAME("Class.forName"),
  CLASS_NEWINSTANCE("Class.newInstance"),
  CONSTRUCTOR_NEWINSTANCE("Constructor.newInstance"),
  METHOD_INVOKE("Method.invoke");

  private String type;

  ReflectiveCallType(String type) {
    this.type = type;
  }

  /**
   * Given the name of the reflective call type, gets it enum member.
   *
   * @param type
   * @return
   */
  public static final ReflectiveCallType stringToType(String type) {
    if (type.equals(ARRAY_NEW_INSTANCE)) {
      return ARRAY_NEW_INSTANCE;
    } else if (type.equals(CLASS_FOR_NAME.type())) {
      return CLASS_FOR_NAME;
    } else if (type.equals(CLASS_NEWINSTANCE.type())) {
      return CLASS_NEWINSTANCE;
    } else if (type.equals(CONSTRUCTOR_NEWINSTANCE.type())) {
      return CONSTRUCTOR_NEWINSTANCE;
    } else if (type.equals(METHOD_INVOKE.type())) {
      return METHOD_INVOKE;
    }

    return null;
  }

  public final String type() {
    return type;
  }
}
