package ca.uwaterloo.averroes.util.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A resource that represents a ZIP archive entry. That's used for both ZIP and JAR archives.
 * 
 * @author karim
 * 
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
	public InputStream open() throws IOException {
		return doJDKBugWorkaround(archive.getInputStream(entry), entry.getSize());
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
	private InputStream doJDKBugWorkaround(InputStream is, long size) throws IOException {
		int sz = (int) size;
		byte[] buf = new byte[sz];

		final int N = 1024;
		int ln = 0;
		int count = 0;
		while (sz > 0 && (ln = is.read(buf, count, Math.min(N, sz))) != -1) {
			count += ln;
			sz -= ln;
		}
		return new ByteArrayInputStream(buf);
	}
}