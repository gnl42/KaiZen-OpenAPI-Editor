package com.reprezen.swagedit.editor;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Document;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.Event.ID;

import com.fasterxml.jackson.databind.JsonNode;

public class SwaggerDocument extends Document {

	private final Yaml yaml = new Yaml();

	public JsonNode getTree() {
		try {
			return io.swagger.util.Yaml.mapper().readTree(get());
		} catch (IOException e) {
			return null;
		}
	}

	/*
	 * Returns the yaml event that matches the given position (line)
	 * in the document.
	 */
	public List<Event> getEvent(int position) {
		final List<Event> events = new ArrayList<>();
		final Reader reader = new StringReader(get());

		Iterable<Event> parse;
		try {
			parse = yaml.parse(reader);
		} catch (Exception e) {
			return events;
		}

		try {
			for (Event event : parse) {
				if (event.getStartMark().getLine() == position) {
					if (event.is(ID.Scalar)) {
						events.add(event);
					}
				}
			}
		} catch (Exception e) {
			return events;
		}

		return events;
	}

}
