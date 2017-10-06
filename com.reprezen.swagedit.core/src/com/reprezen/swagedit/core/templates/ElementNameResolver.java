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
package com.reprezen.swagedit.core.templates;

import java.util.List;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

/**
 * Standard code template variables can only define values that are valid variables, e.g. no space allowed. </br>
 * This template variable resolver provides a way to create and select names with special characters (whitespaces, '(', ')') in code templates. For
 * example, `${element_name:element_name('(schema name)')}` creates `(schema name)` and selects it for editing.
 *
 */
public class ElementNameResolver extends TemplateVariableResolver {
    public ElementNameResolver() {
        super("element_name", "Provides human-friendly element name and selects it");
    }

    @Override
    public void resolve(TemplateVariable variable, TemplateContext context) {
        List params = variable.getVariableType().getParams();
        String[] bindings = new String[params.size()];
        for (int i = 0; i < params.size(); i++) {
            bindings[i] = params.get(i).toString();
        }
        if (bindings.length != 0)
            variable.setValues(bindings);
        variable.setResolved(true);
    }

}
