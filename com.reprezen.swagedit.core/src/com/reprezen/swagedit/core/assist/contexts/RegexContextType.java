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
package com.reprezen.swagedit.core.assist.contexts;

import com.fasterxml.jackson.core.JsonPointer;
import com.reprezen.swagedit.core.model.Model;

/**
 * 
 * The context type is determined by the pointer (path) on which the completion proposal has been activated.
 */
public class RegexContextType extends ContextType {

    private final String regex;

    public RegexContextType(String value, String label, String regex) {
        super(value, label);
        this.regex = regex;
    }

    public RegexContextType(String value, String label, String regex, boolean isLocalOnly) {
        super(value, label, isLocalOnly);
        this.regex = regex;
    }
    
    public boolean canProvideProposal(Model model, JsonPointer pointer) {
        if (pointer != null && regex != null) {
            return pointer.toString().matches(regex);
        }
        return false;
    }

}