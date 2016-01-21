package com.reprezen.swagedit.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.error.YAMLException;

import com.google.common.base.Strings;

public class SwaggerError {

	public String schema;
	public String schemaPointer;
	public String instancePointer;
	public String keyword;
	public String message;

	public int level;
	public int line;
	public int indent = 0;

	public SwaggerError(int line, int level, String message) {
		this.line = line;
		this.level = level;
		this.message = message;
	}

	public SwaggerError(int level, String message) {
		this(1, level, message);
	}

	public SwaggerError(YAMLException exception) {
		this.level = IMarker.SEVERITY_ERROR;
		this.message = exception.getMessage();

		if (exception instanceof MarkedYAMLException) {
			this.line = ((MarkedYAMLException) exception).getProblemMark().getLine() + 1;
		} else {
			this.line = 1;
		}
	}

	public String getMessage() {
		return message;
	}

	public int getLevel() {
		return level;
	}

	public int getLine() {
		return line;
	}

	String getMessage(boolean withIndent) {
		if (withIndent) {
			final StringBuilder builder = new StringBuilder();
			builder.append(Strings.repeat("\t", indent));
			builder.append(" - ");
			builder.append(message);
			builder.append("\n");

			return builder.toString();
		}
		
		return message;
	}

	public static class MultipleSwaggerError extends SwaggerError {

		private final Map<String, Set<SwaggerError>> errors = new HashMap<>();

		public MultipleSwaggerError(int line, int level) {
			super(line, level, null);
		}

		public void put(String key, Set<SwaggerError> errors) {
			this.errors.put(key, errors);
		}

		public Map<String, Set<SwaggerError>> getErrors() {
			return errors;
		}

		@Override
		String getMessage(boolean withIndent) {
			return getMessage();
		}

		@Override
		public String getMessage() {
			final StringBuilder builder = new StringBuilder();
			final String tabs = Strings.repeat("\t", indent);

			builder.append(tabs);
			builder.append("Failed to match exactly one schema:");
			builder.append("\n");

			for (String key: errors.keySet()) {
				builder.append(tabs);
				builder.append(" - ");
				builder.append(key);
				builder.append(":");
				builder.append("\n");

				for (SwaggerError e: errors.get(key)) {
					builder.append(e.getMessage(true));
				}
			}

			return builder.toString();
		}
	}

}
