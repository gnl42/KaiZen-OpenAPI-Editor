package com.reprezen.swagedit.openapi3.editor.hyperlinks

import com.reprezen.swagedit.openapi3.editor.OpenApi3Document
import com.reprezen.swagedit.openapi3.hyperlinks.SecuritySchemeHyperlinkDetector
import com.reprezen.swagedit.openapi3.schema.OpenApi3Schema
import com.reprezen.swagedit.openapi3.utils.Cursors
import com.reprezen.swagedit.openapi3.utils.Mocks
import org.junit.Test

import static org.junit.Assert.*
import com.reprezen.swagedit.core.hyperlinks.SwaggerHyperlink
import com.reprezen.swagedit.openapi3.hyperlinks.LinkOperationHyperlinkDetector

class HyperlinkDetectorTest {

	val securityDetector = new SecuritySchemeHyperlinkDetector
	val operationDetector = new LinkOperationHyperlinkDetector

	@Test
	def void testShouldCreateHyperLink_FromSecuirtyRequirement() throws Exception {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val viewer = Mocks.mockTextViewer(document)

		val groups = Cursors.setUpRegions('''
			paths:
			  /:
			    get:
			      security:
			        - bas<1>ic: []
			components:
			  securitySchemes:
			    <2>basic:
			      type: http
		''', document)

		val region = groups.get("1")
		val hyperlinks = securityDetector.detectHyperlinks(viewer, region, false)

		assertNotNull(hyperlinks);
		assertEquals(1, hyperlinks.size)

		val link = hyperlinks.get(0)
		val target = (link as SwaggerHyperlink).target

		// target on same line
		assertEquals(
			document.getLineOfOffset(groups.get("2").offset),
			document.getLineOfOffset(target.offset)
		)
	}

	@Test
	def void testShouldCreateHyperLink_FromOperationId() throws Exception {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val viewer = Mocks.mockTextViewer(document)

		val groups = Cursors.setUpRegions('''
			paths:
			  /:
			    get:
			      <2>operationId: list
			components:
			  links:
			    foo:
			      operationId: li<1>st
		''', document)

		val region = groups.get("1")
		val hyperlinks = operationDetector.detectHyperlinks(viewer, region, false)

		assertNotNull(hyperlinks);
		assertEquals(1, hyperlinks.size)

		val link = hyperlinks.get(0)
		val target = (link as SwaggerHyperlink).target

		// target on same line
		assertEquals(
			document.getLineOfOffset(groups.get("2").offset),
			document.getLineOfOffset(target.offset)
		)
	}
}
