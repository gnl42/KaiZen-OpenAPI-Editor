package com.reprezen.swagedit.validation;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.dadacoalition.yedit.YEditLog;
import org.eclipse.core.resources.IMarker;
import org.yaml.snakeyaml.nodes.Node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.parser.ParserException;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.json.JsonSchemaManager;

/**
 * This class contains methods for validating a Swagger YAML document.
 * 
 * Validation is done against the Swagger JSON Schema.
 * 
 * @see SwaggerError
 */
public class Validator {

	private static final JsonSchemaManager schemaManager = new JsonSchemaManager();

	/**
	 * Returns a list or errors if validation fails.
	 * 
	 * This method accepts as input a swagger YAML document and validates it
	 * against the swagger JSON Schema.
	 * 
	 * @param content
	 * @return list or errors
	 * @throws IOException
	 * @throws ParserException
	 */
	public Set<SwaggerError> validate(SwaggerDocument document) {
		JsonNode jsonContent = null;
		try {
			jsonContent = document.asJson();
		} catch (Exception e) {
			YEditLog.logException(e);
		}

		if (jsonContent == null) {
			return Collections.singleton(new SwaggerError(IMarker.SEVERITY_ERROR, 
					"Unable to read content. It may be invalid YAML"));
		} else {
			final Node yaml = document.getYaml();
			final ErrorProcessor processor = new ErrorProcessor(yaml);

			ProcessingReport report = null;
			try {
				report = schemaManager.getSwaggerSchema().getSchema().validate(jsonContent, true);
			} catch (ProcessingException e) {
				return processor.processMessage(e.getProcessingMessage());
			}

			return processor.processReport(report);
		}
	}

}
