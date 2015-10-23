package com.reprezen.swagedit.editor;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.Event.ID;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.parser.ParserException;

import com.fasterxml.jackson.databind.JsonNode;

public class SwaggerDocument extends Document {

	private final Yaml yaml = new Yaml();
	private Iterable<Event> events = new ArrayList<>();

	public SwaggerDocument() {
		addDocumentListener(new IDocumentListener() {
			@Override
			public void documentChanged(DocumentEvent event) {}

			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {}
		});
	}

	public Node getYaml() {
		try {
			return yaml.compose(new StringReader(get()));
		} catch (Exception e) {
			return null;
		}
	}

	public JsonNode getTree() throws ParserException, IOException {
		return io.swagger.util.Yaml.mapper().readTree(get());
	}

	/*
	 * Returns the yaml event that matches the given position (line)
	 * in the document.
	 */
	public List<Event> getEvent(int position) {
		final List<Event> result = new ArrayList<>();

		try {
			for (Event event: yaml.parse(new StringReader(get()))) {
				System.out.println(event.getStartMark().getLine() + " > " + event.toString());
				if (event.getStartMark().getLine() == position) {
					if (event.is(ID.Scalar)) {
						result.add(event);
					}
				}
			}
		} catch (Exception e) {
			return result;
		}

		return result;
	}

	public List<String> getPath(int i) {
		final List<String> path = new LinkedList<>();
		final List<Event> events = new LinkedList<>();

		int starts = 0;
		int found = 0;

		MappingNode root = (MappingNode) yaml.compose(new StringReader(get()));
		NodeTuple previous = null;
		for (NodeTuple tuple: root.getValue()) {
			System.out.println(tuple);
			Node key = tuple.getKeyNode();

			if (key.getStartMark().getLine() == i) {
				System.out.println("start here");
			} else if (key.getStartMark().getLine() > i) {
				System.out.println("too far");				
				traverse(previous, i);
			}

			previous = tuple;
		}

		return path;
	}

	private void traverse(NodeTuple tuple, int position) {
		Node key = tuple.getKeyNode();
		Node value = tuple.getValueNode();

		System.out.println("here " + position + " " + key + " > " + value);
	}

}
