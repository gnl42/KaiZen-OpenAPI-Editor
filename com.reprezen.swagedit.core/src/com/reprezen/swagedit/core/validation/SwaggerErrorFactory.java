package com.reprezen.swagedit.core.validation;

import static org.eclipse.core.resources.IMarker.SEVERITY_ERROR;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.core.Activator;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.model.Location;

public class SwaggerErrorFactory {

    public enum ErrorKind {
        unwanted, expected;
    }

    private final ErrorMessageProcessor processor = new ErrorMessageProcessor();

    private int[] computeOffsetAndLength(JsonDocument doc, AbstractNode node) {
        int[] values = new int[] { 0, 0 };
        if (node != null) {
            try {
                Location start = node.getStart();
                Location end = node.getEnd();

                values[0] = doc.getLineOffset(start.getLine()) + start.getColumn();
                values[1] = (doc.getLineOffset(end.getLine()) + end.getColumn()) - values[0];
            } catch (BadLocationException ee) {
                Activator.getDefault().logError(ee.getMessage(), ee);
            }
        }
        return values;
    }

    private int[] computeOffsetAndLength(JsonDocument doc, AbstractNode node, String value) {
        int[] values = new int[] { 0, 0 };
        if (node != null) {
            try {
                Location start = node.getStart();

                values[0] = doc.getLineOffset(start.getLine()) + (start.getColumn());
                values[1] = value.length();
            } catch (BadLocationException ee) {
                Activator.getDefault().logError(ee.getMessage(), ee);
            }
        }
        return values;
    }

    public SwaggerError fromSchemaReport(JsonDocument doc, JsonNode error) {
        String ptr = null;

        if (error.has("instance") && error.get("instance").has("pointer")) {
            ptr = error.get("instance").get("pointer").asText();
        }

        AbstractNode node = doc.getModel().find(ptr);
        if (node == null) {
            node = doc.getModel().getRoot();
        }

        if (node != null) {
            if (error.has("unwanted")) {
                return createUnwantedError(doc, error, node);
            }

            int[] pos = computeOffsetAndLength(doc, node);
            return new SwaggerError(SEVERITY_ERROR, pos[0], pos[1], processor.rewriteMessage(error));
        }

        return null;
    }

    public SwaggerError fromExampleError(JsonDocument doc, JsonNode error, AbstractNode example) {
        AbstractNode errorNode = null;
        if (error.has("instance")) {
            String ptr = error.get("instance").get("pointer").asText();
            JsonPointer pointer = example.getPointer().append(JsonPointer.compile(ptr));
            errorNode = example.getModel().find(pointer);
        }

        int[] pos = computeOffsetAndLength(doc, errorNode);

        return new SwaggerError(SEVERITY_ERROR, pos[0], pos[1], processor.rewriteMessage(error));
    }

    private SwaggerError createUnwantedError(JsonDocument doc, JsonNode error, AbstractNode node) {
        String unwanted = error.get("unwanted").get(0).asText();

        AbstractNode unwantedNode = node.get(unwanted);
        int[] pos = computeOffsetAndLength(doc, unwantedNode, unwanted);

        return new SwaggerError(SEVERITY_ERROR, pos[0], pos[1], processor.rewriteMessage(error));
    }

    private static YamlErrorProcessor yamlProcessor = new YamlErrorProcessor();

    public SwaggerError newYamlError(JsonDocument doc, YAMLException exception) {
        int line = 1;
        int column = 1;

        if (exception instanceof MarkedYAMLException) {
            line = ((MarkedYAMLException) exception).getProblemMark().getLine();
            column = ((MarkedYAMLException) exception).getProblemMark().getColumn();
        }

        int offset = 1;
        if (line > 1) {
            try {
                offset = doc.getLineOffset(line - 1);
            } catch (BadLocationException e) {
                // ignore
            }
        }

        return new SwaggerError(IMarker.SEVERITY_ERROR, offset, column, yamlProcessor.rewriteMessage(exception));
    }

    public SwaggerError newJsonError(JsonProcessingException exception) {
        int line = (exception.getLocation() != null) ? exception.getLocation().getLineNr() : 1;
        return new SwaggerError(line, IMarker.SEVERITY_ERROR, 0, exception.getMessage());
    }

    public SwaggerError fromMessage(JsonDocument document, AbstractNode node, int level, String message) {
        int[] offsetAndLength = computeOffsetAndLength(document, node);

        return new SwaggerError(level, offsetAndLength[0], offsetAndLength[1], message);
    }

    public SwaggerError fromNode(JsonDocument document, Node node, int level, String message) {
        int line = node.getStartMark().getLine() + 1;
        int column = node.getStartMark().getColumn() + 1;
        int offset = 1;
        if (line > 1) {
            try {
                offset = document.getLineOffset(line - 1);
            } catch (BadLocationException e) {
                // ignore
            }
        }

        return new SwaggerError(level, offset, column, message);
    }
}
