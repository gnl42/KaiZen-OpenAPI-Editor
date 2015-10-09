package com.reprezen.swagedit.validation;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;

/**
 * This class contains methods for validating a Swagger YAML document.
 * 
 * Validation is done against the Swagger JSON Schema.
 * 
 * @see SwaggerError
 */
public class Validator {

	private static final ObjectMapper yamlMapper = io.swagger.util.Yaml.mapper();
	private static final Schema schema = new Schema();
	private final Yaml yaml = new Yaml();

	/**
	 * Returns a list or errors if validation fails. 
	 * 
	 * This method accepts as input a swagger YAML document and 
	 * validates it against the swagger JSON Schema.
	 * 
	 * @param content
	 * @return list or errors
	 */
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
					"Unable to read content.  It may be invalid YAML"));
		} else {
			ProcessingReport report = null;
			try {
				report = schema.getSchema().validate(spec);
			} catch (ProcessingException e) {
				e.printStackTrace();
			}

			final Reader reader = new StringReader(content);
			final Node yamlTree = yaml.compose(reader);

			return create(report, yamlTree);
		}
	}

	private List<SwaggerError> create(ProcessingReport report, Node yamlTree) {
		final List<SwaggerError> errors = new ArrayList<>();
		if (report != null) {
			for (Iterator<ProcessingMessage> it = report.iterator(); it.hasNext();) {
				final ProcessingMessage next = it.next();
				final int line = getLine(next, yamlTree);

				errors.add(SwaggerError.create(next, line));
			}
		}

		return errors;
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
	private int getLine(ProcessingMessage message, Node yamlTree) {	
		final JsonNode m = message.asJson();
		if (!m.has("instance") || !m.get("instance").has("pointer"))
			return 1;

		String path = m.get("instance").get("pointer").asText();

		if (path == null || path.isEmpty()) 
			return 1;

		path = path.substring(1, path.length());
		String[] strings = path.split("/");

		if (yamlTree instanceof MappingNode) {
			MappingNode mn = (MappingNode) yamlTree;

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
		return child.getKeyNode();
	}

}
