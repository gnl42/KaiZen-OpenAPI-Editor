package com.reprezen.swagedit.tests.utils

import com.google.common.base.CharMatcher
import com.reprezen.swagedit.editor.SwaggerDocument
import java.util.HashMap
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.Region
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.eclipse.xtext.xbase.lib.Functions.Function2
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2

import static org.junit.Assert.*
import com.fasterxml.jackson.core.JsonPointer
import com.reprezen.swagedit.mocks.Mocks

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
				val offset = groups.get(marker).offset
				val viewer = Mocks.mockTextViewer(doc, offset)

				processor.computeCompletionProposals(viewer, offset)
			}
		}
	}

	static def groupMarkers(String text) {
		val groups = new HashMap<String, IRegion>
		var start = false
		var String current = null
		var offset = 0

		val t = text.replaceAll("(\\r|\\n)", "").toCharArray
		var Region region = null
		for (var i = 0; i < t.length; i++) {
			var b = t.get(i)

			if (CharMatcher.is('<').matches(b)) {
				start = true
				current = new String()
				region = new Region(offset, 1)
			} else if (start) {
				if (CharMatcher.is('>').matches(b)) {
					start = false
					groups.put(current, region)
					current = null
					region = null
				} else {
					current += b
				}
			} else {
				offset++
			}
		}
		groups
	}

	protected static def removeMarkers(String content) {
		content.replaceAll("<\\d+>", "")
	}

}
