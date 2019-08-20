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
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A resource that represents a ZIP archive entry. That's used for both ZIP and JAR archives.
 *
 * @author Karim Ali
 */
public class ZipEntryResource implements Resource {

  private ZipFile archive;
  private ZipEntry entry;

  /**
   * Construct a new ZIP entry resource.
   *
   * @param archive
   * @param entry
   */
  public ZipEntryResource(ZipFile archive, ZipEntry entry) {
    this.archive = archive;
    this.entry = entry;
  }

  @Override
  public InputStream open() {
    return doJDKBugWorkaround();
  }

  public ZipFile archive() {
    return archive;
  }

  public ZipEntry entry() {
    return entry;
  }

  /**
   * Copied from SourceLocator because FoundFile is not accessible outside the soot package.
   *
   * @param is
   * @param size
   * @return
   * @throws IOException
   */
  private InputStream doJDKBugWorkaround() {
    int sz = (int) entry.getSize();
    byte[] buf = new byte[sz];

    final int N = 1024;
    int ln = 0;
    int count = 0;
    try {
      InputStream is = archive.getInputStream(entry);
      while (sz > 0 && (ln = is.read(buf, count, Math.min(N, sz))) != -1) {
        count += ln;
        sz -= ln;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new ByteArrayInputStream(buf);
  }
}
