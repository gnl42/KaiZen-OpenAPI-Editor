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
package com.reprezen.swagedit.core.editor;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;

import com.reprezen.swagedit.core.utils.StringUtils;

public abstract class TextContentDescriber implements IContentDescriber {

    protected abstract boolean isSupported(String contentsAsText);

    @Override
    public int describe(InputStream contents, IContentDescription description) throws IOException {
        String content = StringUtils.toString(contents);
        if (content.trim().isEmpty()) {
            return INDETERMINATE;
        }

        return isSupported(content) ? VALID : INVALID;
    }

    @Override
    public QualifiedName[] getSupportedOptions() {
        return null;
    }

}