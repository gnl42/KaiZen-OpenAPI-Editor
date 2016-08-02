package com.reprezen.swagedit.editor.outline

import org.junit.Test
import org.yaml.snakeyaml.Yaml
import java.io.StringReader

import static org.junit.Assert.*
import com.reprezen.swagedit.editor.SwaggerDocument

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
		
		val doc = new SwaggerDocument
		doc.set(text)

		val position = el.getPosition(doc)
		// end of first line
		assertEquals(8, position.offset)
		assertEquals(0, position.length)

		// position is first line
		assertEquals(0, doc.getLineOfOffset(position.offset))
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
		
		val doc = new SwaggerDocument
		doc.set(text)

		val position = el.getPosition(doc)
		// after foo:
		assertEquals(4, position.offset)
		assertEquals(0, position.length)

		// should be first line
		assertEquals(0, doc.getLineOfOffset(position.offset))
	}

	@Test
	def void testCreateFromObjectValues() {
		val text = '''
		  foo: 
		    k1: hello
		    k2: world
		'''

		val elements = OutlineElement.create(yaml.compose(new StringReader(text)))

		assertEquals(1, elements.size)

		val el = elements.get(0)

		assertEquals("foo", el.text)
		assertEquals(2, el.children.size)
		
		assertEquals("k1: hello", el.children.get(0).text)
		assertEquals("k2: world", el.children.get(1).text)

		val doc = new SwaggerDocument
		doc.set(text)

		val position = el.getPosition(doc)
		// after foo:
		assertEquals(4, position.offset)
		assertEquals(0, position.length)

		// should be first line
		assertEquals(0, doc.getLineOfOffset(position.offset))
	}
}