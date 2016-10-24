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
package com.reprezen.swagedit;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.reprezen.swagedit.messages";

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
    public static String error_typeNoMatch;
    public static String error_notInEnum;
    public static String error_additional_properties_not_allowed;
    public static String error_required_properties;
    public static String error_duplicate_keys;
    public static String error_cannot_read_content;
    public static String error_missing_reference;
    public static String error_invalid_reference;
    public static String error_array_missing_items;
    public static String warning_simple_reference;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
