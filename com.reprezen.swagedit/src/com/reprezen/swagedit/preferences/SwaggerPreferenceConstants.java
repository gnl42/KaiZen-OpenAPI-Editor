/*******************************************************************************
 * Copyright (c) 2016 ModelSolv, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.preferences;

import java.util.Arrays;
import java.util.List;

public class SwaggerPreferenceConstants {

    public static String EXAMPLE_VALIDATION = "swagger.example.validation"; //$NON-NLS-1$
    public static String VALIDATION_REF_SECURITY_DEFINITIONS_OBJECT = "validation.ref.security_definitions"; //$NON-NLS-1$
    public static String VALIDATION_REF_SECURITY_SCHEME_OBJECT = "validation.ref.security_scheme_object"; //$NON-NLS-1$
    public static String VALIDATION_REF_SECURITY_REQUIREMENTS_ARRAY = "validation.ref.security_requirements_array"; //$NON-NLS-1$
    public static String VALIDATION_REF_SECURITY_REQUIREMENT_OBJECT = "validation.ref.security_requirement_object"; //$NON-NLS-1$

    public static final List<String> ALL_VALIDATION_PREFS = Arrays.asList(//
            EXAMPLE_VALIDATION, //
            VALIDATION_REF_SECURITY_DEFINITIONS_OBJECT, //
            VALIDATION_REF_SECURITY_SCHEME_OBJECT, //
            VALIDATION_REF_SECURITY_REQUIREMENTS_ARRAY, //
            VALIDATION_REF_SECURITY_REQUIREMENT_OBJECT);

}
