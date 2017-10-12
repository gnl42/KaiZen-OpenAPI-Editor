/*******************************************************************************
 * Copyright (c) 2016 ModelSolv, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.openapi3.validation

import com.fasterxml.jackson.databind.JsonNode
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Maps
import com.reprezen.swagedit.core.json.references.JsonDocumentManager
import com.reprezen.swagedit.core.model.AbstractNode
import com.reprezen.swagedit.core.validation.ErrorProcessor
import com.reprezen.swagedit.core.validation.SwaggerError
import com.reprezen.swagedit.openapi3.schema.OpenApi3Schema
import com.reprezen.swagedit.openapi3.validation.OpenApi3ReferenceValidator.OpenApi3ReferenceFactory
import java.net.URI
import java.util.Map
import java.util.Set

import static org.junit.Assert.*

class ValidationHelper {

	def public void validate(JsonNode documentAsJson) {
		val JsonNode schemaAsJson = getSchema().asJson()
		val ErrorProcessor processor = new ErrorProcessor(null, null) {
			override protected Set<SwaggerError> fromNode(JsonNode error, int indent) {
				fail('''JSON Schema validation error: «super.fromNode(error, indent)»''')
				return super.fromNode(error, indent)
			}
		}
		val Map<String, JsonNode> preloadedSchemas = Maps.newHashMap();
		preloadedSchemas.put("http://openapis.org/v3/schema.json", getSchema().getRootType().asJson());
		new OpenApi3Validator(new OpenApi3ReferenceValidator(), preloadedSchemas).
			validateAgainstSchema(processor, schemaAsJson, documentAsJson)
	}

	def protected getSchema() {
		return new OpenApi3Schema();
	}

	def static validator() {
		val docManager = new JsonDocumentManager() {
			override getFile(URI uri) {
				null
			}
		}
		val validator = new OpenApi3ReferenceValidator(new OpenApi3ReferenceFactory() {
			override create(AbstractNode node) {
				val reference = super.create(node)
				if (reference !== null) {
					reference.documentManager = docManager
				}
				reference
			}

			override createSimpleReference(URI baseURI, AbstractNode valueNode) {
				val reference = super.createSimpleReference(baseURI, valueNode)
				if (reference !== null) {
					reference.documentManager = docManager
				}
				reference
			}
		})

		new OpenApi3Validator(
			validator,
			ImmutableMap.of("http://openapis.org/v3/schema.json", new OpenApi3Schema().asJson)
		)
	}

}
