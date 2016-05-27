package com.reprezen.swagedit.json.references;

import org.eclipse.core.runtime.IPath;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.reprezen.swagedit.editor.DocumentUtils;
import com.reprezen.swagedit.json.JsonUtil;

public abstract class JsonReference {

	public final JsonPointer pointer;

	JsonReference(JsonPointer pointer) {
		this.pointer = pointer;
	}

	public abstract boolean isValid();

	/*
	 * TODO
	 * We use different paths to locate elements in swagger documents
	 * paths like :paths:get:parameters
	 * This should be changed to use only json pointers.
	 * 
	 * See SwaggerDocument.getPath
	 * 
	 */
	public String pointerAsPath() {
		return pointer.toString().replaceAll("/", ":").replaceAll("~1", "/");
	}

	/**
	 * Returns the resolved node.
	 * 
	 * @return node
	 */
	public abstract JsonNode get();

	/**
	 * Creates a JSON reference from a path or pointer.
	 * 
	 * @param document current doc containing the ref node
	 * @param pathOrPointer pointing to a local node or external node
	 * @return JSON reference
	 */
	public static JsonReference create(JsonNode document, IPath basePath, String pathOrPointer) {
		if (Strings.emptyToNull(pathOrPointer) == null) {
			return new InvalidReference();
		}

		if (JsonUtil.isPointer(pathOrPointer)) {
			// local
			return new LocalReference(document, JsonUtil.asPointer(pathOrPointer));
		} else {
			String filePath = pathOrPointer.contains("#") ? pathOrPointer.split("#")[0] : pathOrPointer;
			String pointer = pathOrPointer.contains("#") ? pathOrPointer.split("#")[1] : "";

			// resolve the path against the path 
			// of the currently active editor
			IPath target = DocumentUtils.resolve(basePath, filePath);
			if (target == null) {
				return new InvalidReference();
			}

			return new ExternalReference(target, JsonUtil.asPointer(pointer));
		}
	}

}
