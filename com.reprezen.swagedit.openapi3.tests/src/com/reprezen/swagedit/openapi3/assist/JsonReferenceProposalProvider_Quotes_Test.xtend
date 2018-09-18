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
import java.util.HashMap
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.Region
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.eclipse.xtext.xbase.lib.Functions.Function2
import org.junit.Ignore
import org.junit.Test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

class JsonReferenceProposalProvider_Quotes_Test {

	@Test
	def void inside_no_quotes() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest(model("$ref: <1>"), document, "")

		val proposals = test.apply(processor, "1")
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("\"#/components/schemas/MyType\"")
		)

		val proposal = proposals.get(0)
		proposal.apply(document)
		val actualAfterCodeAssist = document.get()
		val String expectedAfterCodeAssist = model('''$ref: "#/components/schemas/MyType"''')
		assertThat(actualAfterCodeAssist, equalTo(expectedAfterCodeAssist))
	}

	@Test
	def void inside_double_quotes() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest(model('''$ref: "<1>"'''), document, "")

		val proposals = test.apply(processor, "1")
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItem("\"#/components/schemas/MyType")
		)
		assertThat(proposals.size, equalTo(1))
		val proposal = proposals.get(0)
		proposal.apply(document)
		val actualAfterCodeAssist = document.get()
		val String expectedAfterCodeAssist = model('''$ref: "#/components/schemas/MyType"''')

		assertThat(actualAfterCodeAssist, equalTo(expectedAfterCodeAssist))
	}

	@Test @Ignore
	def void after_opening_double_quote() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest(model('''$ref: "<1> '''), document, "")

		val proposals = test.apply(processor, "1")
		val proposal = proposals.get(0);
		println((proposal as StyledCompletionProposal).replacementString)

		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("#/components/schemas/MyType")
		)
	}

	@Test
	def void inside_single_quotes() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest(model('''$ref: '<1>' '''), document, "")


		val proposals = test.apply(processor, "1")
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("'#/components/schemas/MyType")
		)

		val proposal = proposals.get(0)
		proposal.apply(document)
		val actualAfterCodeAssist = document.get()
		val String expectedAfterCodeAssist = model('''$ref: '#/components/schemas/MyType' ''')
		assertThat(actualAfterCodeAssist, equalTo(expectedAfterCodeAssist))
	}

	@Test @Ignore
	def void after_opening_single_quote() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest(model("$ref: '<1>"), document, "")

		val proposals = test.apply(processor, "1")
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("#/components/schemas/MyType")
		)
	}
	
	@Test 
	def void inside_double_quotes_withSelection() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest(model('''$ref: "<1>SELECTED"'''), document, "SELECTED")


		val proposals = test.apply(processor, "1")
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("\"#/components/schemas/MyType")
		)
		
		val proposal = proposals.get(0)
		proposal.apply(document)
		val actualAfterCodeAssist = document.get()
		val String expectedAfterCodeAssist = model('''$ref: "#/components/schemas/MyType"''')
		assertThat(actualAfterCodeAssist, equalTo(expectedAfterCodeAssist))

	}
	
	def String model(CharSequence ref) {
		'''
---
openapi: "3.0.0"
info:
  version: 1.0.0
  title: My API Spec
paths: {}
components:
  schemas:
    MyType:
      properties:
        prop1:
          «ref»
		'''
	}

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

	/*============    Code below copied from utils.Cursors, otherwise Xtend won't compile     ============= */
	static def (IContentAssistProcessor, String)=>ICompletionProposal[] setUpContentAssistTest(String yaml,
		OpenApi3Document doc, String selection) {

		val groups = groupMarkers(yaml)
		doc.set(removeMarkers(yaml))
		doc.onChange

		new Function2<IContentAssistProcessor, String, ICompletionProposal[]>() {
			override ICompletionProposal[] apply(IContentAssistProcessor processor, String marker) {
				// TODO check why we need to add +1 to offset here
				val offset = groups.get(marker).offset + 1
				val viewer = Mocks.mockTextViewer(doc, offset, selection)
			
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
