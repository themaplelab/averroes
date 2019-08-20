/**
 * ***************************************************************************** Copyright (c) 2015
 * Karim Ali and Ondřej Lhoták. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Karim Ali - initial API and implementation and/or initial documentation
 * *****************************************************************************
 */
package averroes.util.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource that represents a class file.
 *
 * @author Karim Ali
 */
@Deprecated
public class ClassFileResource implements Resource {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private File file;

  /**
   * Construct a new class file resource.
   *
   * @param archive
   * @param entry
   */
  public ClassFileResource(File file) {
    this.file = file;
  }

  @Override
  public InputStream open() {
    try {
      return new ByteArrayInputStream(Files.readAllBytes(file.toPath()));
    } catch (IOException e) {
      logger.error("Cannot read class file " + file);
      return null;
    }
  }

  /**
   * The file holding this .class file.
   *
   * @return
   */
  public File file() {
    return file;
  }
}
