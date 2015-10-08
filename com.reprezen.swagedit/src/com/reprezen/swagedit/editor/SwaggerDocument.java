package com.reprezen.swagedit.editor;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.Document;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.Event.ID;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.report.ProcessingMessage;

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

	/*
	 * Returns the line for which an error message has been produced.
	 *
	 *  The error message is a JSON object that contains the path to
	 *  the invalid node. The path is accessible via instance.pointer.
	 *  The path is in the forms:
	 *  - /{id}
	 *  - /{id}/~{nb}
	 *  - /{id}/~{id2}
	 *
	 *  The line number is computed by after parsing the yaml content with
	 *  the yaml parser. This latter returns a tree of Node, each corresponding
	 *  to a yaml construct and including a position.
	 *
	 *  The Node matching the path is found by the methods findNode().
	 */
	public int getLine(ProcessingMessage message) {
		final Reader reader = new StringReader(get());
		final Node node = yaml.compose(reader);
		final JsonNode m = message.asJson();
		String path = m.get("instance").get("pointer").asText();
		path = path.substring(1, path.length());
		String[] strings = path.split("/");

		if (node instanceof MappingNode) {
			MappingNode mn = (MappingNode) node;

			Node findNode = findNode(mn, Arrays.asList(strings));
			if (findNode != null) {
				return findNode.getStartMark().getLine() + 1;
			}
		}
		return 1;
	}

	/*
	 * Returns the yaml node that matches the given path.
	 *
	 * The path is given as a list of String. The Node matching the
	 * path is found by traversing the children of the node pass as
	 * first parameter.
	 *
	 */
	private Node findNode(MappingNode root, List<String> paths) {
		if (paths.isEmpty())
			return root;

		String path = paths.get(0);
		if (path.startsWith("/")) {
			path = path.substring(1, path.length());
		}

		final List<String> next = paths.subList(1, paths.size());

		if (path.startsWith("~")) {
			path = path.substring(1, path.length());
			// path positions start at 1
			int pos = Integer.valueOf(path) - 1;
			if (pos >= 0 && pos < root.getValue().size()) {
				NodeTuple nodeTuple = root.getValue().get(pos);
				if (nodeTuple != null) {
					return findNode(nodeTuple, next);
				}
			}
		} else {
			for (NodeTuple child: root.getValue()) {
				if (child.getKeyNode() instanceof ScalarNode) {
					ScalarNode scalar = (ScalarNode) child.getKeyNode();

					if (scalar.getValue().equals(path)) {
						return findNode(child, next);
					}
				}
			}
		}
		return root;
	}

	private Node findNode(NodeTuple child, List<String> paths) {
		if (child.getValueNode() instanceof MappingNode) {
			return findNode((MappingNode) child.getValueNode(), paths);
		}
		return null;
	}

}
