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
package com.reprezen.swagedit.core.providers;

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;

import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.validation.SwaggerError;

public interface ValidationProvider {

    Set<SwaggerError> validate(JsonDocument document, URI baseURI, AbstractNode node);

    boolean isActive(JsonDocument document, IPreferenceStore preferenceStore);

    List<FieldEditor> getPreferenceFields(boolean isOpenApi, Composite composite);

    void initializeDefaultPreferences(boolean isOpenApi, IPreferenceStore store);

}
