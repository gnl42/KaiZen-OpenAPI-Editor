/*******************************************************************************
 *  Copyright (c) 2016 ModelSolv, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *     ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.core.utils;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IMarkerResolutionGenerator2;

import com.reprezen.swagedit.core.Activator;
import com.reprezen.swagedit.core.providers.PreferenceProvider;
import com.reprezen.swagedit.core.providers.ValidationProvider;

public class ExtensionUtils {

    @SuppressWarnings("unchecked")
    private static <T> Set<T> resolveProviders(String ID, Predicate<? super IConfigurationElement> predicate) {
        return Stream.of(Platform.getExtensionRegistry() //
                .getConfigurationElementsFor(ID)) //
                .filter(predicate) //
                .map(e -> {
                    try {
                        return e.createExecutableExtension("class");
                    } catch (CoreException ex) {
                        Activator.getDefault().logError(ex.getMessage(), ex);
                        return null;
                    }
                }) //
                .map(e -> {
                    try {
                        return (T) e;
                    } catch (ClassCastException ex) {
                        return null;
                    }
                }) //
                .filter(Objects::nonNull) //
                .collect(Collectors.toSet());
    }

    public static Set<ValidationProvider> getValidationProviders() {
        return resolveProviders(ValidationProvider.ID, (e) -> true);
    }

    public static Set<PreferenceProvider> getPreferenceProviders() {
        return resolveProviders(PreferenceProvider.ID, (e) -> true);
    }

    public static Set<PreferenceProvider> getPreferenceProviders(String preferencePage) {
        return resolveProviders(PreferenceProvider.ID,
                (e) -> preferencePage.equalsIgnoreCase(e.getAttribute("preferencePage")));
    }

    public static Set<IMarkerResolutionGenerator2> getMarkerResolutionGenerators() {
        return resolveProviders("com.reprezen.swagedit.quickfix", (e) -> true);
    }

}
