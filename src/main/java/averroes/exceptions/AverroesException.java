/**
 * ***************************************************************************** Copyright (c) 2015
 * Karim Ali and Ondřej Lhoták. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Karim Ali - initial API and implementation and/or initial documentation
 * *****************************************************************************
 */
package averroes.exceptions;

/**
 * An Averroes exception is thrown whenever Averroes fails to complete any of its tasks.
 *
 * @author karim
 */
public class AverroesException extends Exception {

  private static final long serialVersionUID = 7398074375006931772L;

  /**
   * Construct a new Averroes exception and initialize it with an error message and the cause of the
   * exception.
   *
   * @param message
   * @param cause
   */
  public AverroesException(String message, Throwable cause) {
    super(message, cause);
  }
}
