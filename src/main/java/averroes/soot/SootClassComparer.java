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
import soot.FastHierarchy;
import soot.SootClass;

/**
 * A comparator that compares two Soot classes based on their position in the class hierarchy (i.e.,
 * the superclass relation).
 *
 * @author karim
 */
public class SootClassComparer implements Comparator<SootClass> {
  private FastHierarchy hierarchy;

  /** Construct a new class comparer. */
  public SootClassComparer() {
    super();
    hierarchy = new FastHierarchy();
  }

  /**
   * Compare two Soot classes based on the class hierarchy.
   *
   * @param classA
   * @param classB
   */
  public int compare(SootClass classA, SootClass classB) {
    if (isSubclass(classB, classA) || isSubinterface(classB, classA)) {
      return -1;
    } else if (isSubclass(classA, classB) || isSubinterface(classA, classB)) {
      return 1;
    } else {
      return classA.getName().compareTo(classB.getName());
    }
  }

  private boolean isSubclass(SootClass child, SootClass parent) {
    return !child.isInterface() && !parent.isInterface() && hierarchy.isSubclass(child, parent);
  }

  private boolean isSubinterface(SootClass child, SootClass parent) {
    return parent.isInterface() && hierarchy.getAllImplementersOfInterface(parent).contains(child);
  }
}
