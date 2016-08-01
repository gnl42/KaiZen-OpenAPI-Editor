package com.reprezen.swagedit.editor.outline

import org.junit.Test
import org.yaml.snakeyaml.Yaml
import java.io.StringReader

import static org.junit.Assert.*

class OutlineElementTest {

	val yaml = new Yaml

	@Test
	def void testCreateFromSingleMapping() {
		val text = '''
		  foo: bar
		'''

		val elements = OutlineElement.create(yaml.compose(new StringReader(text)))

		assertEquals(1, elements.size)

		val el = elements.get(0)

		assertEquals("foo: bar", el.text)
		assertEquals(0, el.children.size)
	}

	@Test
	def void testCreateFromArrayValues() {
		val text = '''
		  foo: 
		    - hello
		    - world
		'''

		val elements = OutlineElement.create(yaml.compose(new StringReader(text)))

		assertEquals(1, elements.size)

		val el = elements.get(0)

		assertEquals("foo", el.text)
		assertEquals(2, el.children.size)
		
		assertEquals("hello", el.children.get(0).text)
		assertEquals("world", el.children.get(1).text)
	}

}