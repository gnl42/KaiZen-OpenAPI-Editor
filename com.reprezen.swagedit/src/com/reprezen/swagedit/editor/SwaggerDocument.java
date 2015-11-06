package com.reprezen.swagedit.editor;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.parser.ParserException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * SwaggerDocument
 * 
 */
public class SwaggerDocument extends Document {

	private final Yaml yaml = new Yaml();

	public SwaggerDocument() {
	}

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
	 * Will throw an exception if the content of the document is not valid YAML.
	 * 
	 * @return JsonNode
	 * @throws ParserException
	 * @throws IOException
	 */
	public JsonNode asJson() {
		try {
			return io.swagger.util.Yaml.mapper().readTree(get());
		} catch (Exception e) {
			return io.swagger.util.Yaml.mapper().createObjectNode();
		}
	}

	/**
	 * Returns position of the symbol ':' in respect to the given offset.
	 * 
	 * Will return -1 if reaches beginning of line of other symbol before
	 * finding ':'.
	 * 
	 * @param offset
	 * @return position
	 */
	public int getDelimiterPosition(int offset) {
		while (true) {
			try {
				char c = getChar(--offset);
				if (Character.isLetterOrDigit(c)) {
					return -1;
				}
				if (c == ':') {
					return offset;
				}
				if (Character.isWhitespace(c)) {
					continue;
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

	public String lastIndent(int offset) {
		try {
			int start = offset - 1;
			while (start >= 0 && getChar(start) != '\n') {
				start--;
			}

			int end = start;
			while (end < offset && Character.isSpaceChar(getChar(end))) {
				end++;
			}
			return get(start + 1, end - start - 1);
		} catch (BadLocationException e) {
			return "";
		}
	}

	public String getPath(int line, int column) {
		if (column == 0)
			return ":";

		final MappingNode root = (MappingNode) yaml.compose(new StringReader(get()));
		NodeTuple found = null, previous = null;

		// find root tuple that is after the line
		final Iterator<NodeTuple> it = root.getValue().iterator();
		while (found == null && it.hasNext()) {
			final NodeTuple current = it.next();
			final Node key = current.getKeyNode();

			if (key.getStartMark().getLine() > line) {				
				found = previous;
			}

			previous = current;
		}

		// should be at end of document
		if (found == null) {
			found = previous;
		}

		return getPath(found, line, column);
	}

	private String getPath(NodeTuple tuple, int line, int column) {
		String path = "";
		
		if (tuple == null)
			return path;

		final String id = getId(tuple.getKeyNode());
		if (!id.isEmpty())
			path += ":" + id;

		if (tuple.getValueNode().getNodeId() != NodeId.scalar) {
			return path += getPath(tuple.getValueNode(), line, column);
		} else {
			return path;
		}
	}

	private String getPath(Node node, int line, int column) {
		if (node instanceof MappingNode) {
			MappingNode map = (MappingNode) node;

			NodeTuple found = null;
			Iterator<NodeTuple> it = map.getValue().iterator();
			int currentLine = 0;
			while (currentLine <= line && it.hasNext()) {			
				final NodeTuple tuple = it.next();
				currentLine = tuple.getKeyNode().getStartMark().getLine();
				if (currentLine <= line) {
					found = tuple;
				}
			}

			String content;
			try {
				content = get(getLineOffset(line), getLineLength(line));
			} catch (BadLocationException e) {
				content = null;
			}

			if (found != null) {
				int c = found.getKeyNode().getStartMark().getColumn();
				if (column > c || !content.trim().isEmpty()) {			
					return getPath(found, line, column);					
				}
			}
		}

		if (node instanceof SequenceNode) {
			SequenceNode seq = (SequenceNode) node;
			for (Node current: seq.getValue()) {
				if (current.getStartMark().getLine() == line) {
					return ":@" + seq.getValue().indexOf(current) + getPath(current, line, column);
				}
			}
		}

		if (node.getStartMark().getLine() == line) {
			if (node.getNodeId() == NodeId.scalar) {
				String value = ((ScalarNode) node).getValue();
				if (value == null || value.isEmpty()) {
					return "";
				}
			}
			return ":" + getId(node);
		}

		if (node.getNodeId() == NodeId.scalar) {
			return ((ScalarNode) node).getValue();
		}

		// found nothing
		return "";
	}

	public String getId(Node node) {
		switch (node.getNodeId()) {
		case scalar:
			return ((ScalarNode) node).getValue();
		default:
			return "";
		}
	}

}
