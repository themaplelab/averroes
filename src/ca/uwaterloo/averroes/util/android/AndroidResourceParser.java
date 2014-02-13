package ca.uwaterloo.averroes.util.android;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import pxb.android.axml.AxmlReader;
import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.AxmlVisitor.NodeVisitor;
import soot.jimple.infoflow.android.resources.AbstractResourceParser;
import soot.jimple.infoflow.android.resources.IResourceHandler;

public class AndroidResourceParser extends AbstractResourceParser {

	private String fileName;
	private Set<String> onClickMethodNames;

	public AndroidResourceParser(String fileName) {
		this.fileName = fileName;
		onClickMethodNames = new HashSet<String>();
		parse();
	}

	/**
	 * Get the names of methods that handle onClick events.
	 * 
	 * @return
	 */
	public Set<String> getOnClickMethodNames() {
		return onClickMethodNames;
	}

	/**
	 * Parse the android XML resource files in the given APK file, to collect the names of onClick handlers. This is
	 * later used to determine which method could potentially be called back from the placeholder lirbary.
	 */
	private void parse() {
		handleAndroidResourceFiles(fileName, null, new IResourceHandler() {
			@Override
			public void handleResourceFile(final String fileName, Set<String> fileNameFilter, InputStream stream) {
				// We only process valid layout XML files
				if (!fileName.startsWith("res/layout"))
					return;
				if (!fileName.endsWith(".xml")) {
					System.err.println("Skipping file " + fileName + " in layout folder...");
					return;
				}

				try {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					int in;
					while ((in = stream.read()) >= 0)
						bos.write(in);
					bos.flush();
					byte[] data = bos.toByteArray();
					if (data == null || data.length == 0) // File empty?
						return;

					AxmlReader rdr = new AxmlReader(data);
					rdr.accept(new AxmlVisitor() {
						@Override
						public NodeVisitor first(String ns, String name) {
							return new LayoutParser(fileName);
						}
					});
				} catch (Exception ex) {
					System.err.println("Could not read binary XML file: " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		});
	}

	/**
	 * Parser for layout components defined in XML files
	 */
	private class LayoutParser extends NodeVisitor {
		private final String layoutFile;

		public LayoutParser(String layoutFile) {
			this.layoutFile = layoutFile;
		}

		@Override
		public NodeVisitor child(String ns, String name) {
			return new LayoutParser(layoutFile);
		}

		@Override
		public void attr(String ns, String name, int resourceId, int type, Object obj) {
			// Check that we're actually working on an android attribute
			if (!isAndroidNamespace(ns))
				return;

			// Read out the field data
			String tname = name.trim();
			if (isActionListener(tname) && type == AxmlVisitor.TYPE_STRING && obj instanceof String) {
				String strData = ((String) obj).trim();
				onClickMethodNames.add(strData);
			}

			super.attr(ns, name, resourceId, type, obj);
		}

		/**
		 * Checks whether this name is the name of a well-known Android listener attribute. This is a function to allow
		 * for future extension.
		 * 
		 * @param name
		 *            The attribute name to check. This name is guaranteed to be in the android namespace.
		 * @return True if the given attribute name corresponds to a listener, otherwise false.
		 */
		private boolean isActionListener(String name) {
			return name.equals("onClick");
		}

		/**
		 * Checks whether the given namespace belongs to the Android operating system
		 * 
		 * @param ns
		 *            The namespace to check
		 * @return True if the namespace belongs to Android, otherwise false
		 */
		private boolean isAndroidNamespace(String ns) {
			if (ns == null)
				return false;
			ns = ns.trim();
			if (ns.startsWith("*"))
				ns = ns.substring(1);
			if (!ns.equals("http://schemas.android.com/apk/res/android"))
				return false;
			return true;
		}
	}
}
