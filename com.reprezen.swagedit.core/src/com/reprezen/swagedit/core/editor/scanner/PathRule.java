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
package com.reprezen.swagedit.core.editor.scanner;

import org.eclipse.jface.text.rules.IToken;

/**
 * Scanner rule for matching keys that are paths (starting with /).
 */
public class PathRule extends KeyRule {

    public PathRule(IToken token) {
        super(token);
    }

    @Override
    protected String getKeyRegex() {
        return "([/] [\\w \\s \\. \\\\ \\- _ + / { }]*:)\\s.*";
    }
}
