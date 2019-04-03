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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;

import com.reprezen.swagedit.core.Activator;
import com.reprezen.swagedit.core.providers.ValidationProvider;

public class ExtensionUtils {

    public static Set<ValidationProvider> resolveProviders(String ID) {
        return Stream.of(Platform.getExtensionRegistry() //
                .getConfigurationElementsFor(ID)) //
                .map(e -> {
                    try {
                        return e.createExecutableExtension("class");
                    } catch (CoreException ex) {
                        Activator.getDefault().logError(ex.getMessage(), ex);
                        return null;
                    }
                }) //
                .filter(e -> e instanceof ValidationProvider) //
                .map(e -> (ValidationProvider) e) //
                .collect(Collectors.toSet());
    }

}
