package com.reprezen.swagedit.tests

import com.reprezen.swagedit.editor.SwaggerDocument
import org.junit.Test
import org.yaml.snakeyaml.events.ScalarEvent
import static junit.framework.Assert.*

class SwaggerDocumentTest {

	private val document = new SwaggerDocument

	@Test
	def void testGetCorrectListOfEvents() {
		val yaml = '''
		key: 'value'
		'''

		document.set(yaml)		
		val events = document.getEvent(0)

		assertEquals(events.size, 2)
			
		val e1 = events.get(0) as ScalarEvent
		val e2 = events.get(1) as ScalarEvent
		
		assertEquals(e1.value, "key")
		assertEquals(e2.value, "value")
	}

	@Test
	def void test() {
		val yaml = '''
		info:
		  description: "Tax Blaster"
		  version: "1.0.0"
		'''

		document.set(yaml)		
		val events = document.getEvent(1)

		assertEquals(events.size, 2)

		val e1 = events.get(0) as ScalarEvent
		val e2 = events.get(1) as ScalarEvent

		assertEquals(e1.value, "description")
		assertEquals(e2.value, "Tax Blaster")
	}

}