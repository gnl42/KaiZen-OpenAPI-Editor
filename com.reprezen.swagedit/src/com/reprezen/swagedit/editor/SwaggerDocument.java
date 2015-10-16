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
import org.yaml.snakeyaml.nodes.Node;

import com.fasterxml.jackson.databind.JsonNode;
import org.yaml.snakeyaml.parser.ParserException;

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
		return yaml.compose(new StringReader(get()));
	}

	public JsonNode getTree() throws ParserException, IOException {
		return io.swagger.util.Yaml.mapper().readTree(get());
	}

	/*
	 * Returns the yaml event that matches the given position (line)
	 * in the document.
	 */
	public List<Event> getEvent(int position) {
//		Node node = getYaml();
		final List<Event> result = new ArrayList<>();
		final List<Node> resultTree = new LinkedList<>();

//		System.out.println(node);
//		Node current = node;
//		Node found = null;
//		while (found == null || current != null) {
//			if (current instanceof MappingNode) {
//				List<NodeTuple> values = ((MappingNode) node).getValue();
//				
//				
//			} else if (current instanceof ScalarNode) {
//				ScalarNode scalar = (ScalarNode) current;
//				System.out.println("scalar " + scalar);
//				if (current.getStartMark().getLine() == position) {
//					found = current;
//					System.out.println("found " + scalar);
//				}
//			}
//		}
		
		try {
			for (Event event: events) {
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

}
