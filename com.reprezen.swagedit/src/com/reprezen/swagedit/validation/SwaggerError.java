package com.reprezen.swagedit.validation;

import java.util.Objects;

import org.eclipse.core.resources.IMarker;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.error.YAMLException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.scanner.ScannerException;
import com.google.common.base.Joiner;
import com.reprezen.swagedit.Messages;

public class SwaggerError {

	private final int level;
	private final String message;
	private final int line;

	public SwaggerError(int level, String message) {
		this(level, message, 1);
	}

	public SwaggerError(int level, String message, int line) {
		this.level = level;
		this.message = message;
		this.line = line;
	}

	public static SwaggerError create(JsonMappingException exception) {
		int line = 1;
		if (exception.getLocation() != null) {
			line = exception.getLocation().getLineNr();
		}

		return new SwaggerError(IMarker.SEVERITY_ERROR, exception.getMessage(), line);
	}

	public static SwaggerError create(JsonNode error, int line) {
		return new SwaggerError(getLevel(error), rewriteError(error), line);
	}

	public static SwaggerError create(YAMLException error) {
		if (error instanceof MarkedYAMLException) {
			return new SwaggerError(IMarker.SEVERITY_ERROR, 
					error.getMessage(),
					((MarkedYAMLException) error).getProblemMark().getLine() + 1);
		} else {
			return new SwaggerError(IMarker.SEVERITY_ERROR, error.getMessage(), 1);
		}
	}

	public static SwaggerError create(com.fasterxml.jackson.dataformat.yaml.snakeyaml.parser.ParserException e) {
		return new SwaggerError(IMarker.SEVERITY_ERROR, e.getMessage(), e.getProblemMark().getLine() + 1);
	}

	public static SwaggerError create(ScannerException e) {
		return new SwaggerError(IMarker.SEVERITY_ERROR, e.getMessage(), e.getProblemMark().getLine() + 1);
	}

	private static String rewriteError(JsonNode error) {
		if (error == null || !error.has("keyword")) {
			return "";
		}

		switch (error.get("keyword").asText()) {
		case "type":
			return rewriteTypeError(error);
		case "enum":
			return rewriteEnumError(error);
		case "additionalProperties":
			return rewriteAdditionalProperties(error);
		case "required":
			return rewriteRequiredProperties(error);
		default:
			return error.get("message").asText();
		}
	}

	private static String rewriteRequiredProperties(JsonNode error) {
		JsonNode missing = error.get("missing");

		return String.format(Messages.error_required_properties, Joiner.on(", ").join(missing));
	}

	private static String rewriteAdditionalProperties(JsonNode error) {
		final JsonNode unwanted = error.get("unwanted");

		return String.format(Messages.error_additional_properties_not_allowed, Joiner.on(", ").join(unwanted));
	}

	private static String rewriteTypeError(JsonNode error) {
		final JsonNode found = error.get("found");
		final JsonNode expected = error.get("expected");

		String expect;
		if (expected.isArray()) {
			expect = expected.get(0).asText();
		} else {
			expect = expected.asText();
		}

		return String.format(Messages.error_typeNoMatch, found.asText(), expect);
	}

	private static String rewriteEnumError(JsonNode error) {
		final JsonNode value = error.get("value");
		final JsonNode enums = error.get("enum");
		final String enumString = Joiner.on(", ").join(enums);

		return String.format(Messages.error_notInEnum, value.asText(), enumString);
	}

	protected static int getLevel(JsonNode message) {
		if (message == null || !message.has("level")) {
			return IMarker.SEVERITY_INFO;
		}

		final String level = message.get("level").asText();

		switch (level) {
		case "error":
		case "fatal":
			return IMarker.SEVERITY_ERROR;
		case "warning":
			return IMarker.SEVERITY_WARNING;
		default:
			return IMarker.SEVERITY_INFO;
		}
	}

	public int getLevel() {
		return level;
	}

	public int getLine() {
		return line;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "{ (level=" + getLevel() + ") " + getMessage() + " at line " + getLine() + " }";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SwaggerError) {
			return Objects.equals(level, ((SwaggerError) obj).level) &&
					Objects.equals(line, ((SwaggerError) obj).line) &&
					Objects.equals(message, ((SwaggerError) obj).message);
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return message.hashCode();
	}
}
