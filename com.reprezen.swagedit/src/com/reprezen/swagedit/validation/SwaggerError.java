package com.reprezen.swagedit.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.yaml.snakeyaml.parser.ParserException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.scanner.ScannerException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;

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

	public static SwaggerError create(ProcessingMessage message, int line) {		
		return new SwaggerError(getLevel(message), message.getMessage(), line);
	}

	public static SwaggerError create(ParserException e) {
		return new SwaggerError(IMarker.SEVERITY_ERROR, e.getMessage(), e.getProblemMark().getLine() + 1);
	}

	public static SwaggerError create(com.fasterxml.jackson.dataformat.yaml.snakeyaml.parser.ParserException e) {
		return new SwaggerError(IMarker.SEVERITY_ERROR, e.getMessage(), e.getProblemMark().getLine() + 1);
	}

	public static SwaggerError create(ScannerException e) {
		return new SwaggerError(IMarker.SEVERITY_ERROR, e.getMessage(), e.getProblemMark().getLine() + 1);
	}

//	private static String newMessage(ProcessingMessage message) {
//		JsonNode json = message.asJson();
//		
//	}
	
	protected static int getLevel(ProcessingMessage message) {
		if (message == null) {
			return IMarker.SEVERITY_INFO;
		}

		final LogLevel level = message.getLogLevel();

		switch (level) {
		case ERROR:
		case FATAL:
			return IMarker.SEVERITY_ERROR;
		case WARNING:
			return IMarker.SEVERITY_WARNING;
		default:
			return IMarker.SEVERITY_INFO;
		}
	}

	public IMarker addMarker(IFile target, IDocument document) throws CoreException {
		final IMarker marker = target.createMarker(IMarker.PROBLEM);
		marker.setAttribute(IMarker.SEVERITY, getLevel());
		marker.setAttribute(IMarker.MESSAGE, getMessage());
		marker.setAttribute(IMarker.LINE_NUMBER, getLine());

		return marker;
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

}
