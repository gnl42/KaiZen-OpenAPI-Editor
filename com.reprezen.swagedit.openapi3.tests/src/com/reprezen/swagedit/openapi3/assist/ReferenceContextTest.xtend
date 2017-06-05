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

import com.reprezen.swagedit.openapi3.editor.OpenApi3Document
import org.junit.Test

import static org.junit.Assert.*
import java.util.regex.Pattern
import com.reprezen.swagedit.openapi3.schema.OpenApi3Schema

class ReferenceContextTest {
	val KZOEref = "#KZOE-ref"
	val testNamePattern = Pattern::compile(".*name=\"([\\w\\s]+)\".*")
	val refValuePattern = Pattern::compile(".*value=\"([\\w/]+)\".*")

	@Test
	def void testCallbackRef_in_operation() {
		val text = '''
openapi: "3.0.0"
info:
  title: Callbacks Object
  version: "1.0.0"  
  
paths: 

  /pets:
    get:
      summary: Read
      description: Provide details for the entire list (for collection resources) or an item (for object resources)
      responses: {}
      callbacks:
        myWebhook:
          #KZOE-ref name="callback in operation", value="components/callbacks"
          $ref: "#/components/callbacks/myWebhook"

components: 

  callbacks:
    myWebhook:
      '$request.body#/url':
        post:
          requestBody:
            description: Callback payload
            content: 
              'application/json':
                schema:
                  $ref: '#/components/schemas/SomePayload'
          responses:
            '200':
              description: webhook successfully processed and no retries will be performed
		'''

		val document = new OpenApi3Document(new OpenApi3Schema())
		document.set(text)
		val offset = text.indexOf(KZOEref)
		val region = document.getLineInformationOfOffset(offset)
		val line = document.getLineOfOffset(offset)

		val path = document.getModel(offset).getPath(line, document.getColumnOfOffset(line, region))
		val allContextTypes = OpenApi3ReferenceProposalProvider.OPEN_API3_CONTEXT_TYPES
		val contextType = allContextTypes.get(path.toString + "/$ref")

		val annotationLine = document.get(region.offset, region.getLength())
		val matcher = refValuePattern.matcher(annotationLine)
		if (matcher.matches) {
			val String refValue = matcher.group(1);
			assertEquals(refValue, contextType.value);
		} else {
			fail("Invalid test annotation line: " + annotationLine)
		}
	}
}
