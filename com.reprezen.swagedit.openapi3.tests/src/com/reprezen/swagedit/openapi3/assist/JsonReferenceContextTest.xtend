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
package com.reprezen.swagedit.openapi3.assist

import org.junit.Test

import static org.junit.Assert.*

class JsonReferenceContextTest {

	@Test
	def void headers_in_response() {
		assertTrue(
			"/paths/~1pets/get/responses/200/headers/X-Rate-Limit-Reset/$ref".matches(
				OpenApi3ReferenceProposalProvider.HEADER_REGEX))
	}

	@Test
	def void headers_in_link() {
		assertTrue(
			"/components/links/UserRepositories/headers/X-Rate-Limit-Reset/$ref".matches(
				OpenApi3ReferenceProposalProvider.HEADER_REGEX))
	}

	@Test
	def void callbacks_in_operation() {
		assertTrue(
			"/paths/~1pets/get/callbacks/myWebhook/$ref".matches(OpenApi3ReferenceProposalProvider.CALLBACK_REGEX))
	}

	@Test
	def void example_in_parameter_schema() {
		assertTrue(
			"/paths/~1pets~1{id}/parameters/0/schema/example/$ref".matches(
				OpenApi3ReferenceProposalProvider.SCHEMA_EXAMPLE_REGEX))
	}

	@Test
	def void examples_in_requestBody() {
		assertTrue(
			"/paths/~1pets~1{id}/post/requestBody/content/application~1json/examples/pizza/$ref".matches(
				OpenApi3ReferenceProposalProvider.EXAMPLE_REGEX))
	}

	@Test
	def void examples_in_response() {
		assertTrue(
			"/paths/~1pets~1{id}/post/responses/200/content/application~1json/examples/confirmation-success/$ref".
				matches(OpenApi3ReferenceProposalProvider.EXAMPLE_REGEX))
	}

	@Test
	def void example_in_property() {
		assertTrue(
			"/components/schemas/Pet/properties/name/example/$ref".matches(
				OpenApi3ReferenceProposalProvider.SCHEMA_EXAMPLE_REGEX))
	}
}
