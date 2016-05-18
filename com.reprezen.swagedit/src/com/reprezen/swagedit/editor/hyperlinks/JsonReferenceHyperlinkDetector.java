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
package com.reprezen.swagedit.editor.hyperlinks;

import static com.google.common.base.Strings.emptyToNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.reprezen.swagedit.editor.SwaggerDocument;

/**
 * Hyperlink detector that detects links from JSON references.
 *
 */
public class JsonReferenceHyperlinkDetector extends AbstractSwaggerHyperlinkDetector {

	protected static final Pattern LOCAL_REF_PATTERN = Pattern.compile("['|\"]?#([/\\w+]+)['|\"]?");

	@Override
	protected boolean canDetect(String basePath) {
		return emptyToNull(basePath) != null && basePath.endsWith("$ref");
	}

	@Override
	protected IHyperlink[] doDetect(SwaggerDocument doc, ITextViewer viewer, HyperlinkInfo info, String basePath) {
		if (info.text.matches(LOCAL_REF_PATTERN.pattern())) {
			return doDetectLocalLink(doc, viewer, info);
		} else {
			return doDetectExternalLink(info);
		}
	}

	private IHyperlink[] doDetectExternalLink(HyperlinkInfo info) {
		String filePath;
		String pointer = null;
		IRegion target = null;

		if (info.text.contains("#")) {
			filePath = sanitize(info.text.split("#")[0]);
			pointer = sanitize(info.text.split("#")[1]);
		} else {
			filePath = sanitize(info.text);
		}

		IFile file = getFile(filePath);
		if (file == null || !file.exists()) {
			return null;
		}

		if (pointer != null) {
			SwaggerDocument doc = getExternalDocument(file);
			if (doc != null) {
				target = doc.getRegion(asPath(pointer));
			}
		}

		return new IHyperlink[] { new SwaggerFileHyperlink(info.region, info.text, file, target) };
	}

	private IHyperlink[] doDetectLocalLink(SwaggerDocument doc, ITextViewer viewer, HyperlinkInfo info) {
		Matcher matcher = LOCAL_REF_PATTERN.matcher(info.text);
		String pointer = null;
		if (matcher.find()) {
			pointer = matcher.group(1);
		}

		IRegion target = doc.getRegion(asPath(pointer));
		if (target == null) {
			return null;
		}

		return new IHyperlink[] { new SwaggerHyperlink(pointer, viewer, info.region, target) };
	}

	private IFile getFile(String filePath) {
		IEditorInput input = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow()
				.getActivePage()
				.getActiveEditor()
				.getEditorInput();

		if (input instanceof FileEditorInput) {
			FileEditorInput fileInput = (FileEditorInput) input;

			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IPath extPath = new Path(filePath);
			if (!extPath.isAbsolute()) {
				extPath = new Path(fileInput.getURI().resolve(extPath.toOSString()).getPath());
			}

			return root.getFileForLocation(extPath);
		}

		return null;
	}

	private SwaggerDocument getExternalDocument(IFile file) {
		final SwaggerDocument doc = new SwaggerDocument();
		try {
			doc.set(CharStreams.toString(new InputStreamReader(file.getContents())));
		} catch (IOException | CoreException e) {
			return null;
		}

		return doc;
	}

	private String asPath(String pointer) {
		if (Strings.emptyToNull(pointer) == null) {
			return null;
		}

		return pointer.replaceAll("/", ":").replaceAll("~1", "/");
	}

	private String sanitize(String s) {
		if (Strings.emptyToNull(s) == null) {
			return null;
		}

		return s.trim().replaceAll("'|\"", "");
	}

}
