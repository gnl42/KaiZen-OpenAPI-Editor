package com.reprezen.swagedit.editor;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.Event.ID;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.parser.ParserException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * SwaggerDocument
 * 
 */
public class SwaggerDocument extends Document {

	private final Yaml yaml = new Yaml();

	public SwaggerDocument() {}

	/**
	 * Returns YAML abstract representation of the document.
	 * 
	 * @return Node
	 */
	public Node getYaml() {
		try {
			return yaml.compose(new StringReader(get()));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns the JSON representation of the document.
	 * 
	 * Will throw an exception if the content of the document is not 
	 * valid YAML.
	 * 
	 * @return JsonNode
	 * @throws ParserException
	 * @throws IOException
	 */
	public JsonNode asJson() throws ParserException, IOException {
		return io.swagger.util.Yaml.mapper().readTree(get());
	}

	/**
	 * Returns position of the symbol ':' in respect to 
	 * the given offset.
	 * 
	 * Will return -1 if reaches beginning of line of other symbol 
	 * before finding ':'.
	 * 
	 * @param offset
	 * @return position
	 */
	public int getDelimiterPosition(int offset) {
		while(true) {
			try {
				char c = getChar(--offset);				
				if (Character.isLetterOrDigit(c)) {
					return -1;
				}
				if (c == ':') {
					return offset;
				}
				if (c != ':' && !Character.isLetterOrDigit(c)) {
					return -1;
				}
			} catch (BadLocationException e) {
				return -1;
			}
		}
	}

	/**
	 * Returns the first word it encounters by reading backwards on the line 
	 * starting from the offset.
	 * 
	 * @param offset
	 * @return string
	 */
	public String getWordBeforeOffset(int offset) {
		final StringBuffer buffer = new StringBuffer();
		while (true) {
			try {
				char c = getChar(--offset);
				if (!Character.isLetterOrDigit(c) || Character.isWhitespace(c)) {
					return buffer.reverse().toString().trim(); 
				} else {
					buffer.append(c);
				}
			} catch (BadLocationException e) {
				return buffer.reverse().toString().trim();
			}
		}
	}

	/**
	 * Returns the yaml event that matches the given position (line)
	 * in the document.
	 * 
	 * @param position
	 * @return list of events
	 */
	public List<Event> getEvent(int position) {
		final List<Event> result = new ArrayList<>();

		try {
			for (Event event: yaml.parse(new StringReader(get()))) {
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
