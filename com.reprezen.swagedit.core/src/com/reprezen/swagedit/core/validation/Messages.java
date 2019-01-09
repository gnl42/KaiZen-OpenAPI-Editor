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
package com.reprezen.swagedit.core.validation;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.reprezen.swagedit.core.validation.messages";

    // UI
    public static String swagedit_wizard_title;
    public static String swagedit_wizard_description;
    public static String content_assist_proposal_local;
    public static String content_assist_proposal_project;
    public static String content_assist_proposal_workspace;
    public static String outline_proposal_local;
    public static String outline_proposal_project;
    public static String outline_proposal_workspace;

    // errors
    public static String error_nullType;
    public static String error_typeNoMatch;
    public static String error_notInEnum;
    public static String error_additional_properties_not_allowed;
    public static String warning_required_properties;
    public static String error_duplicate_keys;
    public static String error_cannot_read_content;
    public static String error_missing_reference;
    public static String error_invalid_reference;
    public static String error_array_items_should_be_object;
    public static String error_invalid_reference_type;
    public static String error_invalid_parameter_location;
    public static String error_scope_should_be_empty;
    public static String error_scope_should_not_be_empty;
    public static String error_invalid_scope_reference;
    public static String error_invalid_operation_id;
    public static String error_invalid_operation_ref;
    public static String error_missing_property;
    public static String error_array_missing_items;
    public static String error_object_type_missing;
    public static String error_type_missing;
    public static String error_wrong_type;
    public static String warning_missing_properties;
    public static String warning_simple_reference;

    public static String error_yaml_parser_indentation;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
