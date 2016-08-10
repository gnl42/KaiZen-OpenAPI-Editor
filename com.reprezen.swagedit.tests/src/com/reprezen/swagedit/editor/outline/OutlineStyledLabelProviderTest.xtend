package com.reprezen.swagedit.editor.outline

import com.reprezen.swagedit.model.Model
import org.eclipse.swt.graphics.RGB
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

class OutlineStyledLabelProviderTest {

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

		val root = Model.parseYaml(text).root		
		val el = root.elements.get(0).elements.get(0)

		assertEquals("key: value", el.text)
		assertEquals(el.text, provider.getSyledString(el).toString)
	}

	@Test
	def void should_OnlyDisplayLabel_ForPrimitiveArrayProperties() {
		val text = '''
			schemes:
			  - http
			  - https
		'''

		val els = Model.parseYaml(text).root

		assertEquals("schemes", els.get(0).text)
		assertEquals(els.get(0).text, provider.getSyledString(els.get(0)).toString)
		
		val http = els.get(0).elements.get(0)
		assertEquals("http", http.text)
		assertEquals(http.text, provider.getSyledString(http).toString)
		
		val https = els.get(0).elements.get(1)
		assertEquals("https", https.text)
		assertEquals(https.text, provider.getSyledString(https).toString)
	}

	@Test
	def void should_Display_Name_And_Type_For_Single_Valued_Objects() {
		val text = '''
			object:
			  name: hello
			  value: world
		'''

		val els = Model.parseYaml(text).root
		assertEquals("object", els.get(0).text)

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
		
		val els = Model.parseYaml(text).root
		assertEquals("objects", els.get(0).text)
		
		// TODO
	}

	@Test
	def void should_Display_Name_And_Type_For_Object_Elements() {
		val text = '''
			object:
			  name: hello
			  value: world
		'''
		
		val els = Model.parseYaml(text).root
		assertEquals("object", els.get(0).text)
		
		// TODO
	}

}
