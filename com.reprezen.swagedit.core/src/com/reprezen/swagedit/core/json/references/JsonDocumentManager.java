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
package com.reprezen.swagedit.core.json.references;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.reprezen.swagedit.core.utils.DocumentUtils;

/**
 * JsonDocumentManager
 */
public class JsonDocumentManager {

    private static final JsonDocumentManager INSTANCE = new JsonDocumentManager();

    public static JsonDocumentManager getInstance() {
        return INSTANCE;
    }

    // for tests
    public JsonDocumentManager() {
    }

    private final ObjectMapper mapper = new ObjectMapper();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    private final Map<URL, JsonNode> documents = Collections.synchronizedMap(new WeakHashMap<URL, JsonNode>());
    private final Map<URL, Long> documentVersions = Collections.synchronizedMap(new WeakHashMap<URL, Long>());

    /**
     * Returns the JSON representation of the document located at the given URL. If the document is not found or the
     * document is not a valid JSON nor a valid YAML document, this method returns null.
     * 
     * @param url
     *            of the document
     * @return JSON tree
     */
    public JsonNode getDocument(URL url) {
        URL normalized = normalize(url);
        Long currentTime = getTimestamp(normalized);

        Long previousTime = -1L;
        if (documentVersions.containsKey(normalized)) {
             previousTime = documentVersions.get(normalized);
        }

        if (documents.containsKey(normalized) && previousTime >= currentTime) {
            return documents.get(normalized);
        }

        JsonNode document = parse(normalized);
        if (document != null) {
            documents.put(normalized, document);
            if (currentTime > 0) {
                documentVersions.put(normalized, currentTime);
            }
        }

        return document;
    }

    public JsonNode getDocument(URI uri) {
        try {
            return getDocument(uri.toURL());
        } catch (IllegalArgumentException | MalformedURLException e) {
            return null;
        }
    }

    /**
     * Returns the file located at the given URI if present in the workspace. Returns null otherwise.
     * 
     * @param uri
     *            - workspace file URI
     * @return file
     */
    public IFile getFile(URI uri) {
        return uri != null ? DocumentUtils.getWorkspaceFile(uri) : null;
    }

    private Long getTimestamp(URL url) {
        try {
            File file = Paths.get(url.toURI()).toFile();

            return file != null ? file.lastModified() : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private JsonNode parse(URL url) {
        if (url.getFile().endsWith("json")) {
            try {
                return mapper.readTree(url);
            } catch (Exception e) {
                return null;
            }
        } else if (url.getFile().endsWith("yaml") || url.getFile().endsWith("yml")) {
            try {
                return yamlMapper.readTree(url);
            } catch (Exception e) {
                return null;
            }
        } else {
            // cannot decide which format, so we try both parsers
            try {
                return mapper.readTree(url);
            } catch (Exception e) {
                try {
                    return yamlMapper.readTree(url);
                } catch (Exception ee) {
                    return null;
                }
            }
        }
    }

    private URL normalize(URL url) {
        try {
            return new URL(url.getProtocol(), url.getHost(), url.getFile());
        } catch (MalformedURLException e) {
            return url;
        }
    }

}
