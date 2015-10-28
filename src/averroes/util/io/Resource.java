/*******************************************************************************
 * Copyright (c) 2015 Karim Ali Ondřej Lhoták.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Karim Ali - initial API and implementation and/or initial documentation
 *******************************************************************************/
package averroes.util.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * A resource is something to which we can open an InputStream 1 or more times.
 * 
 * Similar to FoundFile in soot.SourceLocator, which is not accessible.
 */
public interface Resource {
	public InputStream open() throws IOException;
}
