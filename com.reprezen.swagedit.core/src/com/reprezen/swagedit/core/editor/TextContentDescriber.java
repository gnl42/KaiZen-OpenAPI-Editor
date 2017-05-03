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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;

import com.google.common.io.CharStreams;

public abstract class TextContentDescriber implements IContentDescriber {

    protected abstract boolean isSupported(String contentsAsText);

    @Override
    public int describe(InputStream contents, IContentDescription description) throws IOException {
        String content = CharStreams.toString(new InputStreamReader(contents));
        if (content.trim().isEmpty()) {
            return INDETERMINATE;
        }

        return isSupported(content) ? VALID : INVALID;
    }

    @Override
    public QualifiedName[] getSupportedOptions() {
        return null;
    }

    protected String toString(InputStream contents) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = contents.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

}