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
package com.reprezen.swagedit.json;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.reprezen.swagedit.editor.DocumentUtils;

/**
 * JsonDocumentManager
 */
public class JsonDocumentManager {

	private static final JsonDocumentManager INSTANCE = new JsonDocumentManager();

	public static JsonDocumentManager getInstance() {
		return INSTANCE;
	}

	// for tests
	public JsonDocumentManager() {}

	private final ObjectMapper mapper = new ObjectMapper();
	private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

	private final Map<URL, JsonNode> documents = Collections.synchronizedMap(new WeakHashMap<URL, JsonNode>());	

	/**
	 * Returns the JSON representation of the document located at the given URL. If the 
	 * document is not found or the document is not a valid JSON nor a valid YAML document, 
	 * this method returns null.
	 * 
	 * @param url of the document
	 * @return JSON tree
	 */
	public JsonNode getDocument(URL url) {
		if (documents.containsKey(url)) {
			return documents.get(url);
		}

		JsonNode document;
		if (url.getFile().endsWith("json")) {
			try {
				document = mapper.readTree(url);
			} catch (Exception e) {
				document = null;
			}
		} else if (url.getFile().endsWith("yaml") || url.getFile().endsWith("yml")) {
			try {
				document = yamlMapper.readTree(url);
			} catch (IOException e) {
				document = null;
			}
		} else {
			// cannot decide which format, so we try both parsers
			try {
				document = mapper.readTree(url);
			} catch (Exception e) {
				try {
					document = yamlMapper.readTree(url);
				} catch (IOException ee) {
					document = null;
				}
			}
		}

		if (document != null) {
			documents.put(url, document);
		}
		return document;
	}

	public JsonNode getDocument(URI uri) {
		try {
			return getDocument(uri.toURL());
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Returns the file located at the given URI if present 
	 * in the workspace. Returns null otherwise.
	 * 
	 * @param uri - workspace file URI
	 * @return file
	 */
	public IFile getFile(URI uri) {
		return DocumentUtils.getWorkspaceFile(uri);
	}

}
