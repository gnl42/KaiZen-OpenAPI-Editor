package com.reprezen.swagedit.tests.utils

import com.fasterxml.jackson.core.JsonPointer
import com.reprezen.swagedit.editor.SwaggerDocument
import com.reprezen.swagedit.mocks.Mocks
import java.util.HashMap
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.Region
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.eclipse.xtext.xbase.lib.Functions.Function2
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2

import static org.junit.Assert.*

class Cursors {

	static def (String, String)=>void setUpPathTest(String yaml, SwaggerDocument doc) {
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
		SwaggerDocument doc) {

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
