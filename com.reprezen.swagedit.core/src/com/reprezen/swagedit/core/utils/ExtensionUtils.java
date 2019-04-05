/*******************************************************************************
 * Copyright (c) 2019 ModelSolv, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.core.utils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class ExtensionUtils {

	public static Object getExtension(String extensionPointId, String propertyName) throws CoreException {
		final IConfigurationElement[] configurationElementsFor = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(extensionPointId);
		return configurationElementsFor[0].createExecutableExtension(propertyName);
	}

}
