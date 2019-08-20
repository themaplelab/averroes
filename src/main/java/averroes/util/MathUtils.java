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

public class MathUtils {

  /**
   * Round the given double value to the specified number of places after the decimal point.
   *
   * @param value
   * @param places
   * @return
   */
  public static double round(double value, int places) {
    if (places < 0) throw new IllegalArgumentException();

    long factor = (long) Math.pow(10, places);
    value = value * factor;
    long tmp = Math.round(value);
    return (double) tmp / factor;
  }

  /**
   * Round a double value to the nearest two decimal places.
   *
   * @param value
   * @return
   */
  public static double round(double value) {
    return round(value, 2);
  }
}
