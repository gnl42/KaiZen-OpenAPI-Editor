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
package com.reprezen.swagedit.openapi3.utils

import com.fasterxml.jackson.core.JsonPointer
import com.reprezen.swagedit.openapi3.editor.OpenApi3Document
import java.util.HashMap
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.Region
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.eclipse.xtext.xbase.lib.Functions.Function2
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2

import static org.junit.Assert.*
import java.util.Map

class Cursors {

	static def Map<String, IRegion> setUpRegions(String yaml, OpenApi3Document doc) {
		val groups = groupMarkers(yaml)
		doc.set(removeMarkers(yaml))
		doc.onChange

		groups
	}

	static def (String, String)=>void setUpPathTest(String yaml, OpenApi3Document doc) {
		val groups = groupMarkers(yaml)
		doc.set(removeMarkers(yaml))
		doc.onChange

		new Procedure2<String, String>() {
			override apply(String path, String marker) {
				assertEquals(JsonPointer.compile(path), doc.getPath(groups.get(marker)))
			}
		}
	}

	static def (IContentAssistProcessor, String)=>ICompletionProposal[] setUpContentAssistTest(String yaml,
		OpenApi3Document doc) {

		val groups = groupMarkers(yaml)
		doc.set(removeMarkers(yaml))
		doc.onChange

		new Function2<IContentAssistProcessor, String, ICompletionProposal[]>() {
			override ICompletionProposal[] apply(IContentAssistProcessor processor, String marker) {
				// TODO check why we need to add +1 to offset here
				val offset = groups.get(marker).offset + 1
				val viewer = Mocks.mockTextViewer(doc, offset)

				processor.computeCompletionProposals(viewer, offset)
			}
		}
	}

	static def groupMarkers(String text) {
		val groups = new HashMap<String, IRegion>

		val doc = new Document(text)
		var i = 0
		var offset = 0
		var start = false
		var group = ""

		while (i < doc.getLength()) {
			var current = doc.get(i, 1)
			if (current.equals("<")) {
				start = true
			} else if (current.equals(">")) {
				start = false
				groups.put(group, new Region(Math.max(0, offset - 1), 1))
				group = ""
			} else {
				if (start) {
					group += current
				} else {
					offset++
				}
			}
			i++
		}

		groups
	}

	protected static def removeMarkers(String content) {
		content.replaceAll("<\\d+>", "")
	}

}
