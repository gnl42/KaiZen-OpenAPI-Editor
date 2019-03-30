package com.reprezen.swagedit.core.utils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class ExtensionUtils {

	public static Object createExecutableExtension(String extensionPointId, String propertyName) throws CoreException {
		final IConfigurationElement[] configurationElementsFor = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(extensionPointId);
		return configurationElementsFor[0].createExecutableExtension(propertyName);
	}

}
