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
package com.reprezen.swagedit.editor;

import com.reprezen.swagedit.core.editor.TextContentDescriber;

public class SwaggerContentDescriber extends TextContentDescriber {

    @Override
    protected boolean isSupported(String content) {
        return content.contains("swagger: \"2.0\"") || content.contains("swagger: '2.0'");
    }

}
