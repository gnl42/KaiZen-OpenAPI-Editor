package com.reprezen.swagedit.tests

import com.reprezen.swagedit.editor.SwaggerDocument
import org.junit.Test
import org.yaml.snakeyaml.events.ScalarEvent

import static org.assertj.core.api.Assertions.*

class SwaggerDocumentTest {

	private val document = new SwaggerDocument

	@Test
	def void testGetCorrectListOfEvents() {
		val yaml = '''
		key: 'value'
		'''

		document.set(yaml)		
		val events = document.getEvent(0)

		assertThat(events).hasSize(2)
			
		val e1 = events.get(0) as ScalarEvent
		val e2 = events.get(1) as ScalarEvent
		
		assertThat(e1.value).isEqualTo("key")
		assertThat(e2.value).isEqualTo("value")
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

		assertThat(events).hasSize(2)

		val e1 = events.get(0) as ScalarEvent
		val e2 = events.get(1) as ScalarEvent

		assertThat(e1.value).isEqualTo("description")
		assertThat(e2.value).isEqualTo("Tax Blaster")
	}

}