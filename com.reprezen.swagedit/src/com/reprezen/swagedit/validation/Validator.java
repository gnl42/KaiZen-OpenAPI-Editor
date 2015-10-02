package com.reprezen.swagedit.validation;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IMarker;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;

import io.swagger.util.Yaml;

public class Validator {

	private static final ObjectMapper yamlMapper = Yaml.mapper();
	private static final Schema schema = new Schema();

	public List<SwaggerError> validate(String content) {
		JsonNode spec = null;
		try {
			spec = yamlMapper.readTree(content);
		} catch (Exception e) {
			if (e instanceof JsonMappingException) {
				return Collections.singletonList(SwaggerError.create((JsonMappingException) e));
			}
		}

		if (spec == null) {
			return Collections.singletonList(new SwaggerError(
					IMarker.SEVERITY_ERROR, 
					"Unable to read content.  It may be invalid JSON or YAML"));
		} else {
			ProcessingReport report = null;
			try {
				report = schema.getSchema().validate(spec);
			} catch (ProcessingException e) {
				e.printStackTrace();
			}

			return SwaggerError.create(report);
		}
	}

}
