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
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dadacoalition.yedit.YEditLog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.base.Strings;

import io.swagger.util.Json;

public class JsonSchemaManager {

	private final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
	private final ObjectMapper mapper = Json.mapper();
	private static final Map<String, JSONSchema> schemas = new ConcurrentHashMap<>();

	/**
	 * Returns swagger 2.0 schema
	 * 
	 * @return swagger schema.
	 */
	public JSONSchema getSwaggerSchema() {
		return getSchema("swagger");
	}

	public JSONSchema getSchema(String url) {
		if (Strings.emptyToNull(url) == null) {
			return null;
		}

		// remove fragment
		if (url.contains("#")) {
			url = url.substring(0, url.indexOf("#"));
		}

		if (schemas.containsKey(url)) {
			return schemas.get(url);
		}

		JSONSchema schema = null;
		if (url.startsWith("http") || url.startsWith("https")) {
			try {
				schema = new JSONSchema(readTree(new URL(url)));
			} catch (IOException e) {
				YEditLog.logException(e);
				return null;
			}
		} else if (url.equals("swagger")) {
			try {
				schema = new JSONSchema(readTree(getClass().getResourceAsStream("schema.json")));
			} catch (IOException e) {
				YEditLog.logException(e);
				return null;
			}
		}

		if (schema != null) {
			schemas.put(url, schema);
		}

		return schema;
	}

	private JsonNode readTree(InputStream inputStream) throws IOException {
		return mapper.readTree(inputStream);
	}

	private JsonNode readTree(URL url) throws IOException {
		return mapper.readTree(url);
	}

	public class JSONSchema {
		private final JsonNode content;
		private com.github.fge.jsonschema.main.JsonSchema schema;

		JSONSchema(JsonNode content) {
			this.content = content;
		}

		public JsonNode asJson() {
			return content;
		}

		public com.github.fge.jsonschema.main.JsonSchema getSchema() {
			if (schema == null && content != null) {
				try {
					schema = factory.getJsonSchema(content);
				} catch (ProcessingException e) {
					YEditLog.logException(e);
				}
			}
			return schema;
		}

	}

}
