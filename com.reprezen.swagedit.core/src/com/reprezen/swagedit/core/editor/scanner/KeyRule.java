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
 * Scanner rule for matching keys in a mapping.
 * 
 * This rule is based on YEdit KeyRule but uses a different regex that allows slash and dollar sign. It is necessary to
 * allow media types such as `application/json` and references `$ref` as keys.
 */
public class KeyRule extends org.dadacoalition.yedit.editor.scanner.KeyRule {

    public KeyRule(IToken token) {
        super(token);
    }

    protected String getKeyRegex() {
        return "([\\w \\- _ + $] [\\w \\s \\. \\\\ \\- _ + /]*:)\\s.*";
    }

}
