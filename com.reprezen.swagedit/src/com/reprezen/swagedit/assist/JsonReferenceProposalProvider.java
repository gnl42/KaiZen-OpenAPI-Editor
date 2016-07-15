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
package com.reprezen.swagedit.assist;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.editor.DocumentUtils;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.json.JsonDocumentManager;

/**
 * Completion proposal provider for JSON references. 
 */
public class JsonReferenceProposalProvider extends AbstractProposalProvider {

	protected static final String SCHEMA_DEFINITION_REGEX = "^:definitions:(\\w+:)+\\$ref|.*schema:(\\w+:)?\\$ref";
	protected static final String RESPONSE_REGEX = ".*responses:\\d+:\\$ref";
	protected static final String PARAMETER_REGEX = ".*:parameters:@\\d+:\\$ref";
	protected static final String PATH_ITEM_REGEX = ":paths:/[^:]+:\\$ref";

	private final ObjectMapper mapper = new ObjectMapper();
	private final JsonDocumentManager manager = JsonDocumentManager.getInstance();

	protected IFile getActiveFile() {
		return DocumentUtils.getActiveEditorInput().getFile();
	}

	@Override
	public Iterable<JsonNode> createProposals(String path, SwaggerDocument document, int cycle) {
		final Scope scope = Scope.get(cycle);
		final ContextType type = ContextType.get(path);
		final List<JsonNode> proposals = Lists.newArrayList();
		final IFile currentFile = getActiveFile();
		final IPath basePath = currentFile.getParent().getFullPath();

		if (scope == Scope.LOCAL) {
			proposals.addAll(collectProposals(document.asJson(), type.value(), null));
		} else {
			IContainer parent;
			if (scope == Scope.PROJECT) {
				parent = currentFile.getProject();
			} else {
				parent = currentFile.getWorkspace().getRoot();
			}

			Iterable<IFile> files = collectFiles(parent);
			for (IFile file: files) {
				IPath relative	= file.equals(currentFile) ? null : 
					file.getFullPath().makeRelativeTo(basePath);

				JsonNode content = file.equals(currentFile) ? document.asJson() :
					manager.getDocument(file.getLocationURI());

				proposals.addAll(collectProposals(content, type.value(), relative));
			}
		}

		return proposals;
	}

	protected Iterable<IFile> collectFiles(IContainer parent) {
		final FileVisitor visitor = new FileVisitor();

		try {
			parent.accept(visitor, 0);
		} catch (CoreException e) {
			return Lists.newArrayList();
		}
		return visitor.getFiles();
	}

	/**
	 * Represents the scope for which the JSON reference proposals have to 
	 * be computed. 
	 * <br/>
	 * The default scope LOCAL means that JSON references will be computed only 
	 * from inside the currently edited file.
	 * The scope PROJECT means that JSON references will be computed from files 
	 * inside the same project has the currently edited file.
	 * The scope WORKSPACE means that JSON references will be computed from files
	 * present in the current workspace. 
	 */
	protected enum Scope {
		LOCAL(0),
		PROJECT(1),
		WORKSPACE(2);

		private final int value;

		Scope(int v) {
			this.value = v;
		}

		public int getValue() {
			return value;
		}

		public static Scope get(int cycle) {
			switch (cycle) {
			case 1:
				return PROJECT;
			case 2:
				return WORKSPACE;
			default:
				return Scope.LOCAL;
			}
		}
	}

	/**
	 * Represents the different contexts for which a JSON reference may be 
	 * computed.
	 * <br/>
	 * The context type is determined by the pointer (path) on which the completion proposal 
	 * has been activated.
	 */
	protected enum ContextType {
		SCHEMA_DEFINITION("definitions"),
		PATH_ITEM("paths"),
		PATH_PARAMETER("parameters"),
		PATH_RESPONSE("responses"),
		UNKNOWN(null);

		private final String value;
		private ContextType(String value) {
			this.value = value;
		}

		public String value() {
			return value;
		}

		public static ContextType get(String path) {
			if (Strings.emptyToNull(path) == null) {
				return UNKNOWN;
			}

			if (path.matches(SCHEMA_DEFINITION_REGEX)) {
				return SCHEMA_DEFINITION;
			} else if (path.matches(PARAMETER_REGEX)) {
				return PATH_PARAMETER;
			} else if (path.matches(RESPONSE_REGEX)) {
				return PATH_RESPONSE;
			} else if (path.matches(PATH_ITEM_REGEX)) {
				return PATH_ITEM;
			}

			return UNKNOWN;
		}
	}

	/**
	 * Returns all proposals found in the document at the specified field. 
	 * 
	 * @param document
	 * @param fieldName
	 * @param path
	 * @return Collection of proposals
	 */
	protected Collection<JsonNode> collectProposals(JsonNode document, String fieldName, IPath path) {
		final Collection<JsonNode> results = Lists.newArrayList();
		if (fieldName == null || !document.has(fieldName)) {
			return results;
		}

		final JsonNode parameters = document.get(fieldName);
		final String basePath = (path != null ? path.toString() : "") + "#/" + fieldName + "/";

		for (Iterator<String> it = parameters.fieldNames(); it.hasNext();) {
			String key = it.next();
			String value = basePath + key.replaceAll("/", "~1");
			String encoded = encodeURL(value);

			results.add(mapper.createObjectNode()
					.put("value", "\"" + encoded + "\"")
					.put("label", key)
					.put("type", value));
		}

		return results;
	}

	/*
	 * Encodes that path string so that it does not include illegal characters in 
	 * respect to URL encoding. 
	 * (see http://stackoverflow.com/questions/607176/java-equivalent-to-javascripts-encodeuricomponent-that-produces-identical-outpu)
	 */
	protected String encodeURL(String path) {
		try {
			return URLEncoder.encode(path, "UTF-8")
					// decode characters that don't 
					// need to be encoded.
					.replaceAll("\\+", "%20")
					.replaceAll("\\%21", "!")
					.replaceAll("\\%2F", "/")
					.replaceAll("\\%23", "#")
					.replaceAll("\\%27", "'")
					.replaceAll("\\%7E", "~");
		} catch (UnsupportedEncodingException e) {
			return path;
		}
	}

	private static class FileVisitor implements IResourceProxyVisitor {

		private final List<IFile> files = new ArrayList<>();

		@Override
		public boolean visit(IResourceProxy proxy) throws CoreException {
			if (proxy.getType() == IResource.FILE && 
					(proxy.getName().endsWith("yaml") || proxy.getName().endsWith("yml"))) {

				if (!proxy.isDerived()) {
					files.add((IFile) proxy.requestResource());
				}
			} else if (proxy.getType() == IResource.FOLDER && 
					(proxy.isDerived() || proxy.getName().equalsIgnoreCase("gentargets"))) {
				return false;
			}
			return true;
		}

		public List<IFile> getFiles() {
			return files;
		}
	}

}
