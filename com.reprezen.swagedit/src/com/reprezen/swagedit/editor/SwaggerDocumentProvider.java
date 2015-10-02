package com.reprezen.swagedit.editor;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.dadacoalition.yedit.editor.ColorManager;
import org.dadacoalition.yedit.editor.scanner.YAMLScanner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.report.ProcessingMessage;

import io.swagger.models.Swagger;
import io.swagger.parser.Swagger20Parser;

public class SwaggerDocumentProvider extends FileDocumentProvider {

	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null) {
			SwaggerPartitionScanner scanner = new SwaggerPartitionScanner(new ColorManager());
			FastPartitioner partitioner = new FastPartitioner(scanner, null);
			document.setDocumentPartitioner(partitioner);
			partitioner.connect(document);
		}

		return document;
	}

	@Override
	protected IDocument createEmptyDocument() {
		return new SwaggerDocument();
	}

	public static class SwaggerPartitionScanner extends YAMLScanner implements IPartitionTokenScanner {

		public SwaggerPartitionScanner(ColorManager colorManager) {
			super(colorManager);
		}

		@Override
		public void setPartialRange(IDocument document, int offset, int length, String contentType,
				int partitionOffset) {

		}

	}

	public class SwaggerDocument extends Document {

		private final Yaml yaml = new Yaml();
		private final Swagger20Parser parser = new Swagger20Parser();

		public Swagger getSwagger() {
			Swagger swagger = null;
			try {
				swagger = parser.parse(get());
			} catch (Exception e) {
				swagger = null;
			}
			return swagger;
		}

		@SuppressWarnings("unchecked")
		public LinkedHashMap<String, Object> getYaml() {
			return (LinkedHashMap<String, Object>) yaml.load(get());
		}

		public List<Event> getEvent(int position) {
			final List<Event> events = new ArrayList<>();
			final Reader reader = new StringReader(get());

			for (Event event : yaml.parse(reader)) {
				if (event.getStartMark().getLine() == position) {
					events.add(event);
				}
			}

			return events;
		}

		public int getLine(ProcessingMessage message) {
			final Reader reader = new StringReader(get());
			final Node node = yaml.compose(reader);
			final JsonNode m = message.asJson();
			final String path = m.get("instance").get("pointer").asText();

			if (node instanceof MappingNode) {
				MappingNode mn = (MappingNode) node;
				for (NodeTuple type : mn.getValue()) {
					if (type.getKeyNode() instanceof ScalarNode) {
						ScalarNode sn = (ScalarNode) type.getKeyNode();

						if (sn.getValue().equals(path.replace("/", ""))) {
							return sn.getStartMark().getLine() + 1;
						}
					}
				}
			}

			return 1;
		}
	}

}
