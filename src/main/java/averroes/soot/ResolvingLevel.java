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

import soot.SootClass;

/**
 * An enumeration for all possible values of {@value SootClass#resolvingLevel()}.
 *
 * @author karim
 */
public enum ResolvingLevel {
  DANGLING(0),
  HIERARCHY(1),
  SIGNATURES(2),
  BODIES(3);

  private final int value;

  ResolvingLevel(int value) {
    this.value = value;
  }

  public int value() {
    return value;
  }
}
