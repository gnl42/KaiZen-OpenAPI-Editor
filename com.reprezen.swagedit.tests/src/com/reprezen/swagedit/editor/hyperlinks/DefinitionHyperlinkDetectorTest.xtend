package com.reprezen.swagedit.editor.hyperlinks

import com.reprezen.swagedit.editor.SwaggerDocument
import com.reprezen.swagedit.mocks.Mocks
import com.reprezen.swagedit.tests.utils.PointerHelpers
import java.util.Arrays
import org.eclipse.jface.text.BadLocationException
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.Region
import org.junit.Test

import static org.hamcrest.core.IsCollectionContaining.hasItem
import static org.junit.Assert.*

class DefinitionHyperlinkDetectorTest {

	extension PointerHelpers = new PointerHelpers

	val detector = new DefinitionHyperlinkDetector()

	@Test
	def void testCanDetect() {
		assertTrue("/tags/0".matches(DefinitionHyperlinkDetector.TAGS_PATTERN))
		assertTrue(detector.canDetect("/tags/0".ptr))
	}

	@Test
	def void testShouldCreateHyperLink_ToDefinition() throws BadLocationException {
		val document = new SwaggerDocument()
		val ITextViewer viewer = Mocks.mockTextViewer(document)

		val text = '''
			tags:  
			  - foo
			definitions:
			  foo:
			    type: object
		 '''

		document.set(text)

		// region that includes `- foo`
		val region = new Region("tags:\n  - fo".length(), 1)
		val hyperlinks = detector.detectHyperlinks(viewer, region, false);

		// expected region
		val linkRegion = new Region(document.getLineOffset(1) + "  - ".length(), 3)
		val targetRegion = new Region(29, 24)

		assertThat(Arrays.asList(hyperlinks), hasItem(new SwaggerHyperlink("foo", viewer, linkRegion, targetRegion)))
	}

	@Test
	def void testShouldCreateHyperLink_FromRequired_ToProperty() throws BadLocationException {
		val document = new SwaggerDocument()
		val ITextViewer viewer = Mocks.mockTextViewer(document)

		val text = '''
			NewPet:
			  required:
			    - name
			  properties:
			    name:
			      type: string
		'''

		document.set(text)
		// region that includes `- name`
		val region = new Region("NewPet:\nrequired:\n    - nam".length(), 1)
		val hyperlinks = detector.detectHyperlinks(viewer, region, false)

		// expected region
		val linkRegion = new Region(document.getLineOffset(2) + "    - ".length(), "name".length())
		val targetRegion = new Region(45, 29)

		assertThat(Arrays.asList(hyperlinks), hasItem(new SwaggerHyperlink("name", viewer, linkRegion, targetRegion)))
	}

}
