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

import com.reprezen.swagedit.core.assist.StyledCompletionProposal
import com.reprezen.swagedit.core.model.Model
import com.reprezen.swagedit.openapi3.editor.OpenApi3Document
import com.reprezen.swagedit.openapi3.schema.OpenApi3Schema
import com.reprezen.swagedit.openapi3.utils.Mocks
import java.util.ArrayList
import org.junit.Test

import static com.reprezen.swagedit.openapi3.utils.Cursors.*
import static org.hamcrest.core.IsCollectionContaining.*
import static org.junit.Assert.*

class JsonReferenceProposalProvider_Quotes_Test {

	val referenceProvider = new OpenApi3ReferenceProposalProvider(new OpenApi3Schema) {
		override protected getActiveFile() {
			Mocks.mockJsonReferenceProposalFile()
		}
	}

	val processor = new OpenApi3ContentAssistProcessor(null, referenceProvider) {
		override protected initTextMessages(Model model) { new ArrayList }

		override protected getContextTypeRegistry() { null }

		override protected getTemplateStore() { null }

		override protected getContextTypeId(Model model, String path) { null }

	}

	@Test
	def void test_inside_double_quotes() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
---
openapi: "3.0.0"
info:
  version: 1.0.0
  title: My API Spec
paths: {}
components:
  schemas:
    ReferencedType:
      type: string
    MyType:
      properties:
        prop1:
          $ref: "<1>"
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("\"#/components/schemas/MyType")
		)
	}

	@Test
	def void test_after_opening_double_quote() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
---
openapi: "3.0.0"
info:
  version: 1.0.0
  title: My API Spec
paths: {}
components:
  schemas:
    ReferencedType:
      type: string
    MyType:
      properties:
        prop1:
          $ref: "<1>
		''', document)

		val proposals = test.apply(processor, "1")
		val proposal = proposals.get(0);
		println((proposal as StyledCompletionProposal).replacementString)
		
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("#/components/schemas/MyType")
		)
	}

	@Test
	def void test_inside_single_quotes() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
---
openapi: "3.0.0"
info:
  version: 1.0.0
  title: My API Spec
paths: {}
components:
  schemas:
    ReferencedType:
      type: string
    MyType:
      properties:
        prop1:
          $ref: '<1>'
		''', document)

		val proposals = test.apply(processor, "1")
		val proposal = proposals.get(0);
		println((proposal as StyledCompletionProposal).replacementString)
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("#/components/schemas/MyType")
		)
	}

	@Test
	def void test_after_opening_single_quote() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
---
openapi: "3.0.0"
info:
  version: 1.0.0
  title: My API Spec
paths: {}
components:
  schemas:
    ReferencedType:
      type: string
    MyType:
      properties:
        prop1:
          $ref: '<1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("#/components/schemas/MyType")
		)
	}

}
