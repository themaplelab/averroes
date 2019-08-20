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

/**
 * A numberer for local variables in a the Jimple body of a method. This is extensively by {@link
 * CodeGenerator}.
 *
 * @author karim
 */
public class LocalVariableNumberer {

  private int number;
  private String letter;

  public LocalVariableNumberer() {
    this(0, "r");
  }

  public LocalVariableNumberer(int number, String letter) {
    this.number = number;
    this.letter = letter;
  }

  public String next() {
    return letter + number++;
  }
}
