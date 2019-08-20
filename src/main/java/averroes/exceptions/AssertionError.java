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
 * An assertion error is an error that is thrown when an assertion fails.
 *
 * @author karim
 */
public class AssertionError extends Error {

  private static final long serialVersionUID = 7356269331503470943L;

  /** Construct a new assertion error */
  public AssertionError() {
    super();
  }

  /**
   * Construct a new assertion error with the given error message.
   *
   * @param message
   */
  public AssertionError(String message) {
    super(message);
  }
}
