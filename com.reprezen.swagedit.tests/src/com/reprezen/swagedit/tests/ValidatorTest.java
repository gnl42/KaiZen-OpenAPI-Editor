package com.reprezen.swagedit.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.junit.Test;

import com.reprezen.swagedit.validation.SwaggerError;
import com.reprezen.swagedit.validation.Validator;

public class ValidatorTest {

	private Validator validator = new Validator();

	@Test
	public void shouldNotReturnErrorsIfDocumentIsValid() throws IOException {
		String content = read("fixt1.yaml");
		
		assertEquals(0, validator.validate(content).size());
	}

	@Test
	public void shouldReturnSingleErrorIfMissingRootProperty() throws IOException {
		String content = read("fixt2.yaml");

		List<SwaggerError> errors = validator.validate(content);
		assertEquals(1, errors.size());
		
		SwaggerError error = errors.get(0);
		assertEquals(IMarker.SEVERITY_ERROR, error.getLevel());
		assertEquals(1, error.getLine());
	}

	@Test
	public void shouldReturnSingleErrorIfTypeOfPropertyIsIncorrect() throws IOException {
		String content = read("fixt3.yaml");

		List<SwaggerError> errors = validator.validate(content);
		assertEquals(1, errors.size());

		SwaggerError error = errors.get(0);
		assertEquals(IMarker.SEVERITY_ERROR, error.getLevel());
		assertEquals(8, error.getLine());
	}

	@Test
	public void shouldReturnSingleErrorIfTypeOfDeepPropertyIsIncorrect() throws IOException {
		String content = read("fixt4.yaml");

		List<SwaggerError> errors = validator.validate(content);
		assertEquals(1, errors.size());

		SwaggerError error = errors.get(0);
		assertEquals(IMarker.SEVERITY_ERROR, error.getLevel());
		assertEquals(11, error.getLine());
	}

	@Test
	public void shouldReturnSingleErrorIfInvalidResponseCode() throws IOException {
		String content = read("fixt5.yaml");

		List<SwaggerError> errors = validator.validate(content);
		assertEquals(1, errors.size());

		SwaggerError error = errors.get(0);
		assertEquals(IMarker.SEVERITY_ERROR, error.getLevel());
		assertEquals(12, error.getLine());
	}

	@Test
	public void shouldReturnErrorForInvalidScheme() throws IOException {
		String content = read("fixt6.yaml");

		List<SwaggerError> errors = validator.validate(content);
		assertEquals(1, errors.size());

		SwaggerError error = errors.get(0);
		assertEquals(IMarker.SEVERITY_ERROR, error.getLevel());
		assertEquals(8, error.getLine());
	}

	private String read(String fileName) throws IOException {
		return new String(Files.readAllBytes(Paths.get("fixtures", fileName)));
	}

}
