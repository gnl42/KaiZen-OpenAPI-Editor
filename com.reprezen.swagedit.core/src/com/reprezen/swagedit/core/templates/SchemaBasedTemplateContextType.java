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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;

import com.fasterxml.jackson.core.JsonPointer;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.schema.ComplexTypeDefinition;
import com.reprezen.swagedit.core.schema.ReferenceTypeDefinition;
import com.reprezen.swagedit.core.schema.TypeDefinition;

public class SchemaBasedTemplateContextType extends TemplateContextType {

    private final List<String> pathToSchemaType;

    public SchemaBasedTemplateContextType(String id, String name, String... pathToSchemaType) {
        super(id, name);
        this.pathToSchemaType = Arrays.asList(pathToSchemaType);
        addGlobalResolvers();
    }

    public boolean matches(JsonDocument doc, final String path) {
        if (doc == null || doc.asJson().at(path) == null) {
            return false;
        }

        TypeDefinition type = doc.getContent().getTypes().get(JsonPointer.compile(path));
        return matches(type);
    }

    protected boolean matches(TypeDefinition type) {
        if (type instanceof ReferenceTypeDefinition) {
            type = ((ReferenceTypeDefinition) type).resolve();
        }
        if (type instanceof ComplexTypeDefinition) {
            Collection<TypeDefinition> types = ((ComplexTypeDefinition) type).getComplexTypes();
            for (TypeDefinition subtype : types) {
                if (matches(subtype)) {
                    return true;
                }
            }

        }
        return pathToSchemaType.contains(type.getPointer().toString());
    }

    private void addGlobalResolvers() {
        addResolver(new GlobalTemplateVariables.Cursor());
        addResolver(new GlobalTemplateVariables.WordSelection());
        addResolver(new GlobalTemplateVariables.LineSelection());
        addResolver(new GlobalTemplateVariables.Dollar());
        addResolver(new GlobalTemplateVariables.Date());
        addResolver(new GlobalTemplateVariables.Year());
        addResolver(new GlobalTemplateVariables.Time());
        addResolver(new GlobalTemplateVariables.User());
        addResolver(new ElementNameResolver());
    }

}
