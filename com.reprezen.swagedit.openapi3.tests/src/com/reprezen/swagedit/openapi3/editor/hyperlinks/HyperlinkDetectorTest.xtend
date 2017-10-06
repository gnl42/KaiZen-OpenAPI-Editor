/*******************************************************************************
 *  Copyright (c) 2016 ModelSolv, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *     ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
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
import com.reprezen.swagedit.openapi3.hyperlinks.LinkOperationRefHyperlinkDetector

class HyperlinkDetectorTest {

	val securityDetector = new SecuritySchemeHyperlinkDetector
	val operationDetector = new LinkOperationHyperlinkDetector
	val operationRefDetector = new LinkOperationRefHyperlinkDetector() {
		override protected getActiveEditor() {
			null
		}
	}

	@Test
	def void testShouldCreateHyperLink_FromSecurityRequirement() throws Exception {
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

	@Test
	def void testShouldCreateHyperLink_FromOperationRef() throws Exception {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val viewer = Mocks.mockTextViewer(document)

		val groups = Cursors.setUpRegions('''
			paths:
			  /:
			    <2>get:
			      operationId: list
			components:
			  links:
			    foo:
			      operationRef: '#/<1>paths/~1/get'
		''', document)

		val region = groups.get("1")
		val hyperlinks = operationRefDetector.detectHyperlinks(viewer, region, false)

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
	def void testShouldCreateHyperLink_FromLinkInsideResponses() throws Exception {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val viewer = Mocks.mockTextViewer(document)

		val groups = Cursors.setUpRegions('''
			paths:
			  /:
			    get:
			      <2>operationId: list
			      responses:
			        '200':
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

	@Test
	def void testShouldCreateHyperLink_FromLinkOperationRefInsideResponses() throws Exception {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val viewer = Mocks.mockTextViewer(document)

		val groups = Cursors.setUpRegions('''
			paths:
			  /:
			    <2>get:
			      operationId: list
			      responses:
			        '200':
			          links:
			            foo:
			              operationRef: '#/<1>paths/~1/get'
		''', document)

		val region = groups.get("1")
		val hyperlinks = operationRefDetector.detectHyperlinks(viewer, region, false)

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
