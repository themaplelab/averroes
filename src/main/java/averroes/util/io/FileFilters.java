/**
 * ***************************************************************************** Copyright (c) 2015
 * Karim Ali and Ondřej Lhoták. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Karim Ali - initial API and implementation and/or initial documentation
 * *****************************************************************************
 */
/** */
package averroes.util.io;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

/**
 * Utility class for file filters.
 *
 * @author Karim Ali
 */
public class FileFilters {
  public static IOFileFilter classFileFilter = FileFilterUtils.suffixFileFilter("class");

  public static IOFileFilter jarFileFilter = FileFilterUtils.suffixFileFilter("jar");

  public static IOFileFilter jreFileFilter =
      FileFilterUtils.or(
          FileFilterUtils.nameFileFilter("rt.jar"),
          FileFilterUtils.nameFileFilter("jsse.jar"),
          FileFilterUtils.nameFileFilter("jce.jar"),
          FileFilterUtils.nameFileFilter("charsets.jar"));

  public static IOFileFilter rtFileFilter = FileFilterUtils.nameFileFilter("rt.jar");
}
