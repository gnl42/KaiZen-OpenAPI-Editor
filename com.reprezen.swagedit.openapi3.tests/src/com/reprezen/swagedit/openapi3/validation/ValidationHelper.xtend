/*******************************************************************************
 * Copyright (c) 2016 ModelSolv, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse License v1.0
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
import com.reprezen.swagedit.core.editor.JsonDocument
import com.reprezen.swagedit.core.json.references.JsonDocumentManager
import com.reprezen.swagedit.core.model.AbstractNode
import com.reprezen.swagedit.openapi3.schema.OpenApi3Schema
import com.reprezen.swagedit.openapi3.validation.OpenApi3ReferenceValidator.OpenApi3ReferenceFactory
import com.reprezen.swagedit.openapi3.validation.OpenApi3Validator.OpenApi3SchemaValidator
import java.net.URI
import java.util.Map

class ValidationHelper {

	def validate(JsonDocument document) {
		val Map<String, JsonNode> preloadedSchemas = Maps.newHashMap()
		preloadedSchemas.put(OpenApi3Schema.URL, getSchema().getRootType().asJson())
		schemaValidator.validate(document)
	}

	def protected getSchema() {
		return new OpenApi3Schema();
	}

	def static schemaValidator() {
		val schemas = ImmutableMap.of(OpenApi3Schema.URL, new OpenApi3Schema().asJson)
		new OpenApi3SchemaValidator(new OpenApi3Schema().asJson, schemas)
	}

	def static validator() {
		validator(false)
	}

	def static validator(boolean advanced) {
		val docManager = new JsonDocumentManager() {
			override getFile(URI uri) {
				null
			}
		}
		val schemas = ImmutableMap.of(OpenApi3Schema.URL, new OpenApi3Schema().asJson)
		val schemaVal = new OpenApi3SchemaValidator(new OpenApi3Schema().asJson, schemas)

		val refVal = new OpenApi3ReferenceValidator(schemaVal, new OpenApi3ReferenceFactory() {
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

		val validator = new OpenApi3Validator(schemas) {
			override getReferenceValidator() {
				refVal
			}

			override getSchemaValidator() {
				schemaVal
			}

			override isAdvancedValidation() {
				advanced
			}
		}
		validator
	}

}
