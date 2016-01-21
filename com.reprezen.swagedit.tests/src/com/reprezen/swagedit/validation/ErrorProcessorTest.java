package com.reprezen.swagedit.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.nodes.Node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ErrorProcessorTest {

	private ErrorProcessor processor;
	private ObjectMapper mapper = new ObjectMapper();

	@Before
	public void setUp() {
		Node document = null;
		processor = new ErrorProcessor(document);
	}

	@Test
	public void testProcessNode_WithSingleError() throws Exception {
		JsonNode fixture = mapper.readTree(Paths.get("resources", "error-1.json").toFile());
		Set<SwaggerError> errors = processor.processMessageNode(fixture);

		assertEquals(1, errors.size());
		assertTrue(errors.iterator().next() instanceof SwaggerError);
	}

	@Test
	public void testProcessNode_WithOneOfError() throws Exception {
		JsonNode fixture = mapper.readTree(Paths.get("resources", "error-2.json").toFile());
		Set<SwaggerError> errors = processor.processMessageNode(fixture);

		assertEquals(1, errors.size());	
		assertTrue(errors.iterator().next() instanceof SwaggerError.MultipleSwaggerError);
	}

}
