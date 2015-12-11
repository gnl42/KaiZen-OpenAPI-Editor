package com.reprezen.swagedit.validation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dadacoalition.yedit.YEditLog;
import org.eclipse.core.resources.IMarker;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.parser.ParserException;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
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
			return Collections.singleton(
					new SwaggerError(IMarker.SEVERITY_ERROR, "Unable to read content.  It may be invalid YAML"));
		} else {
			final Node yaml = document.getYaml();
			ProcessingReport report = null;

			try {
				report = schemaManager.getSwaggerSchema().getSchema().validate(jsonContent, true);
			} catch (ProcessingException e) {
				final ProcessingMessage pm = e.getProcessingMessage();
				final int line = getLine(pm.asJson(), yaml);

				return Collections.singleton(SwaggerError.create(pm.asJson(), line));
			}

			return create(report, document.getYaml());
		}
	}

	private Set<SwaggerError> create(ProcessingReport report, Node yamlTree) {
		final Set<SwaggerError> errors = new HashSet<>();
		if (report != null) {
			for (Iterator<ProcessingMessage> it = report.iterator(); it.hasNext();) {
				final ProcessingMessage next = it.next();
				final Set<JsonNode> reportNodes = collectReports(next.asJson());

				for (JsonNode reportNode : reportNodes) {
					errors.add( SwaggerError.create(reportNode, getLine(reportNode, yamlTree)) );
				}
			}
		}

		return errors;
	}

	private Set<JsonNode> collectReports(JsonNode message) {
		final Set<JsonNode> allReports = new HashSet<>();

		if (message.has("reports")) {
			final JsonNode reports = message.get("reports");
			allReports.add(((ObjectNode) message).without("reports"));

			for (Iterator<String> it = reports.fieldNames(); it.hasNext();) {
				String key = it.next();
				JsonNode value = reports.get(key);

				if (value.isArray()) {
					for (JsonNode current : value) {
						allReports.addAll(collectReports(current));
						allReports.add(((ObjectNode) current).without("reports"));
					}
				} else if (value.isObject()) {
					allReports.addAll(collectReports(value));
					allReports.add(((ObjectNode) value).without("reports"));
				}
			}
		} else {
			allReports.add(message);
		}

		return allReports;
	}

	/*
	 * Returns the line for which an error message has been produced.
	 *
	 * The error message is a JSON object that contains the path to the invalid
	 * node. The path is accessible via instance.pointer. The path is in the
	 * forms: 
	 *  - /{id} 
	 *  - /{id}/~{nb} 
	 *  - /{id}/~{id2}
	 *
	 * The line number is computed by after parsing the yaml content with the
	 * yaml parser. This latter returns a tree of Node, each corresponding to a
	 * yaml construct and including a position.
	 *
	 * The Node matching the path is found by the methods findNode().
	 */
	private int getLine(JsonNode error, Node yamlTree) {
		if (!error.has("instance") || !error.get("instance").has("pointer"))
			return 1;

		String path = error.get("instance").get("pointer").asText();

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
		// ~1 is use to escape /
		if (path.contains("~1")) {
			path = path.replaceAll("~1", "/");
		}

		for (NodeTuple child : root.getValue()) {
			if (child.getKeyNode() instanceof ScalarNode) {
				ScalarNode scalar = (ScalarNode) child.getKeyNode();

				if (scalar.getValue().equals(path)) {
					return findNode(child, next);
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
