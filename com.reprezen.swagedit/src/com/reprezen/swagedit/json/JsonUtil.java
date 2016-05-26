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

import org.eclipse.core.runtime.IPath;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.editor.DocumentUtils;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.json.JsonSchemaManager.JSONSchema;

public class JsonUtil {

	private static final JsonSchemaManager schemaManager = new JsonSchemaManager();

	/**
	 * Returns true if the node is a reference to another node.
	 * 
	 * @param node
	 * @return true if is reference
	 */
	public static boolean isRef(JsonNode node) {
		return node.isObject() && node.has("$ref");
	}

	/**
	 * Returns the node that is referenced by the refNode.
	 * 
	 * @param document
	 * @param refNode
	 * @return referenced node
	 */
	public static SchemaDefinition getReference(JsonNode document, JsonNode refNode) {
		if (!isRef(refNode) || document == null)
			return new SchemaDefinition(document, refNode);

		JsonNode found = null;
		String ref = refNode.get("$ref").asText();

		if (ref.startsWith("http") || ref.startsWith("https")) {
			JSONSchema schema = schemaManager.getSchema(ref);
			if (schema != null) {
				document = schema.asJson();
			}
			ref = ref.substring(ref.indexOf("#"));
		}

		final String[] keys = (ref.startsWith("#/") ? ref.substring(2) : ref).split("/");

		found = document;
		String lastKey = null;
		for (String key : keys) {
			JsonNode value = found.get(key);
			if (value != null) {
				found = value;
				lastKey = key;
			}
		}

		return new SchemaDefinition(document, found != null ? found : refNode, lastKey);
	}

	public static JsonNode at(SwaggerDocument doc, String ref) {
		JsonReference reference = create(ref);
		if (reference == null) {
			return null;
		} else if (reference instanceof ExternalJsonReference) {
			SwaggerDocument extDoc = DocumentUtils.getDocument(((ExternalJsonReference) reference).path);
			if (extDoc == null) {
				return null;
			}
			return extDoc.asJson().at(reference.pointer);
		} else {
			return doc.asJson().at(reference.pointer);
		}
	}

	protected static JsonReference create(String ref) {
		if (ref.startsWith("#")) {
			// local
			return new JsonReference(JsonPointer.compile(ref.substring(1)));
		} else {
			String filePath = ref.contains("#") ? ref.split("#")[0] : ref;
			String pointer = ref.contains("#") ? ref.split("#")[1] : "";

			IPath path = DocumentUtils.resolve(
				DocumentUtils.getActiveEditorInput().getPath(), 
				filePath);

			if (path == null) {
				return null;
			}

			return new ExternalJsonReference(path, JsonPointer.compile(pointer));
		}
	}

	static class JsonReference {

		public final JsonPointer pointer;

		JsonReference(JsonPointer pointer) {
			this.pointer = pointer;
		}
	}
	
	static class ExternalJsonReference extends JsonReference {

		public final IPath path;

		ExternalJsonReference(IPath path, JsonPointer pointer) {
			super(pointer);
			this.path = path;
		}

	}

}
