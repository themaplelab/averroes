/**
 * ***************************************************************************** Copyright (c) 2015
 * Karim Ali and Ondřej Lhoták. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Karim Ali - initial API and implementation and/or initial documentation
 * *****************************************************************************
 */
package averroes.soot;

import java.util.Comparator;
import soot.SootMethod;

/**
 * A comparator that compares two Soot methods based on the name of their declaring class.
 *
 * @author karim
 */
public class SootMethodComparer implements Comparator<SootMethod> {

  /** Construct a new Soot method comparator. */
  public SootMethodComparer() {
    super();
  }

  /**
   * Compare two Soot methods based on the name of their declaring class.
   *
   * @param methodA
   * @param methodB
   * @return
   */
  public int compare(SootMethod methodA, SootMethod methodB) {
    return methodA.getDeclaringClass().getName().compareTo(methodB.getDeclaringClass().getName());
  }
}
