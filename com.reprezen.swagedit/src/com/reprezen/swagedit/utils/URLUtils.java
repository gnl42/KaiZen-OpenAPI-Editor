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
package com.reprezen.swagedit.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class URLUtils {

    /*
     * Encodes that path string so that it does not include illegal characters in respect to URL encoding. (see
     * http://stackoverflow
     * .com/questions/607176/java-equivalent-to-javascripts-encodeuricomponent-that-produces-identical-outpu)
     */
    public static String encodeURL(String path) {
        try {
            return URLEncoder.encode(path, "UTF-8")
                    // decode characters that don't
                    // need to be encoded.
                    .replaceAll("\\+", "%20") //
                    .replaceAll("\\%21", "!") //
                    .replaceAll("\\%2F", "/") //
                    .replaceAll("\\%23", "#") //
                    .replaceAll("\\%27", "'") //
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            return path;
        }
    }

}
