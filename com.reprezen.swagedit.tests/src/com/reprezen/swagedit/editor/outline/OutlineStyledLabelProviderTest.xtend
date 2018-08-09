package com.reprezen.swagedit.editor.outline

import org.eclipse.swt.graphics.RGB
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*
import com.reprezen.swagedit.schema.SwaggerSchema
import com.reprezen.swagedit.core.editor.outline.OutlineStyledLabelProvider
import com.reprezen.swagedit.core.json.JsonModel
import com.fasterxml.jackson.core.JsonPointer

class OutlineStyledLabelProviderTest {

	val schema = new SwaggerSchema
	OutlineStyledLabelProvider provider

	@Before
	def void setUp() {
		provider = new OutlineStyledLabelProvider {
			override protected getColor(RGB rgb) {
				null
			}
		}
	}

	@Test
	def void should_OnlyDisplayLabel_ForSingleValued_PrimitiveProperties() {
		val text = '''
			foo:
			  key: value
		'''

		val model = new JsonModel(schema, text, false)
		val root =  model.content
		val el = root.get(0)

		assertEquals("key: value", provider.getText(el, JsonPointer.compile("/foo/key")))
		assertEquals("key: value", provider.getStyledString(model, JsonPointer.compile("/foo/key")).toString)
	}

	@Test
	def void should_OnlyDisplayLabel_ForPrimitiveArrayProperties() {
		val text = '''
			schemes:
			  - http
			  - https
		'''

		val model = new JsonModel(schema, text, false)
		val els = model.content
		println(els)

		assertEquals("schemes", provider.getText(els, "/schemes".ptr))
		assertEquals("schemes schemesList", provider.getStyledString(model, "/schemes".ptr).toString)

println(els.get("schemes"))
		val http = els.get("schemes").get(0)
		println(http)
		assertEquals("http", provider.getText(http, "/schemes/0/http".ptr))
		assertEquals("http", provider.getStyledString(model, "/schemes/0/http".ptr).toString)

		val https = els.get("schemes").get(1)
		assertEquals("https", provider.getText(https, "/schemes/1/https".ptr))
		assertEquals("https", provider.getStyledString(model, "/schemes/1/https".ptr).toString)
	}

	@Test
	def void should_Display_Name_And_Type_For_Single_Valued_Objects() {
		val text = '''
			object:
			  name: hello
			  value: world
		'''

		val model = new JsonModel(schema, text, false)
		val els = model.content

		assertEquals("object", provider.getText(els, "/object".ptr))
		// TODO
	}

	@Test
	def void should_Display_Type_For_Arrays_Of_Objects() {
		val text = '''
			objects:
			  - name: hello
			    value: world
			  - name: hello
			    value: world
		'''

		val model = new JsonModel(schema, text, false)
		val els = model.content

		assertEquals("objects", provider.getText(els, "/objects".ptr))
		// TODO
	}

	@Test
	def void should_Display_Name_And_Type_For_Object_Elements() {
		val text = '''
			object:
			  name: hello
			  value: world
		'''

		val model = new JsonModel(schema, text, false)
		val els = model.content

		assertEquals("object", provider.getText(els, "/object".ptr))
		// TODO
	}

	def ptr(String ptr) {
		JsonPointer.compile(ptr)
	}
}
