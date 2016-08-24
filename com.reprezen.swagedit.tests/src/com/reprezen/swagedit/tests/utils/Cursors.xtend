package com.reprezen.swagedit.tests.utils

import com.google.common.base.CharMatcher
import com.reprezen.swagedit.editor.SwaggerDocument
import java.util.HashMap
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.ITextSelection
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.Region
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.eclipse.jface.viewers.ISelectionProvider
import org.eclipse.swt.graphics.Point
import org.eclipse.xtext.xbase.lib.Functions.Function2
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2

import static org.junit.Assert.*
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class Cursors {

	static def (String, String)=>void setUpPathTest(String yaml, SwaggerDocument doc) {
		val groups = groupMarkers(yaml)
		doc.set(removeMarkers(yaml))

		new Procedure2<String, String>() {
			override apply(String path, String marker) {
				assertEquals(path, doc.getPath(groups.get(marker)))
			}
		}
	}

	static def (IContentAssistProcessor, String)=>ICompletionProposal[] setUpContentAssistTest(String yaml,
		SwaggerDocument doc, ITextViewer viewer) {
		val groups = groupMarkers(yaml)
		doc.set(removeMarkers(yaml))

		when(viewer.getDocument()).thenReturn(doc)
		when(viewer.getSelectedRange()).thenReturn(new Point(0, 0))

		val selectionProvider = mock(ISelectionProvider)
		val selection = mock(ITextSelection)

		when(viewer.getSelectionProvider()).thenReturn(selectionProvider)
		when(selectionProvider.getSelection()).thenReturn(selection)

		new Function2<IContentAssistProcessor, String, ICompletionProposal[]>() {
			override ICompletionProposal[] apply(IContentAssistProcessor processor, String marker) {
				val offset = groups.get(marker).offset
				when(selection.getOffset()).thenReturn(offset)
				processor.computeCompletionProposals(viewer, offset)
			}
		}
	}

	static def groupMarkers(String text) {
		val groups = new HashMap<String, IRegion>
		var start = false
		var String current = null
		var offset = 0

		val t = text.toCharArray
		for (var i = 0; i < t.length; i++) {
			var b = t.get(i)

			if (CharMatcher.is('{').matches(b)) {
				start = true
				current = new String()
			} else if (start) {
				if (CharMatcher.is('}').matches(b)) {
					start = false
					groups.put(current, new Region(offset, 1))
					current = null
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
		content.replaceAll("\\{\\d+\\}", "")
	}

}
