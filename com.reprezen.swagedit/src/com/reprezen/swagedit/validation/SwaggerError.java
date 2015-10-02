package com.reprezen.swagedit.validation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.reprezen.swagedit.editor.SwaggerDocumentProvider.SwaggerDocument;

public class SwaggerError {

	private final int level;
	private final String message;
	private final Object original;

	public SwaggerError(int level, String message) {
		this(level, message, null);
	}

	public SwaggerError(int level, String message, Object original) {
		this.level = level;
		this.message = message;
		this.original = original;
	}

	public static SwaggerError create(JsonMappingException exception) {
		return new SwaggerError(IMarker.SEVERITY_ERROR, exception.getMessage(), exception);
	}

	public static SwaggerError create(ProcessingMessage message) {
		return new SwaggerError(getLevel(message), message.getMessage(), message);
	}

	public static List<SwaggerError> create(ProcessingReport report) {
		final List<SwaggerError> errors = new ArrayList<>();
		if (report != null) {
			for (Iterator<ProcessingMessage> it = report.iterator(); it.hasNext();) {
				errors.add(create(it.next()));
			}
		}
		return errors;
	}

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
		marker.setAttribute(IMarker.SEVERITY, level);
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.LINE_NUMBER, 1);

		if (original instanceof JsonMappingException) {
			JsonMappingException exception = (JsonMappingException) original;

			if (exception.getLocation() != null) {
				marker.setAttribute(IMarker.LINE_NUMBER, exception.getLocation().getLineNr());
			}
		} else if (original instanceof ProcessingMessage) {
			ProcessingMessage message = (ProcessingMessage) original;

			if (document instanceof SwaggerDocument) {
				SwaggerDocument sd = (SwaggerDocument) document;
				marker.setAttribute(IMarker.LINE_NUMBER, sd.getLine(message));
			}
		}

		return marker;
	}

}
