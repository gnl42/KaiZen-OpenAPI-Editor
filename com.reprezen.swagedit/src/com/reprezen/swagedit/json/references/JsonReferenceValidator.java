package com.reprezen.swagedit.json.references;

import java.net.URI;
import java.util.Collection;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.yaml.snakeyaml.nodes.Node;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.reprezen.swagedit.Messages;
import com.reprezen.swagedit.validation.SwaggerError;

public class JsonReferenceValidator {

	private JsonReferenceCollector collector;

	public JsonReferenceValidator(JsonReferenceFactory factory) {
		this.collector = new JsonReferenceCollector(factory);
	}

	public Collection<? extends SwaggerError> validate(URI baseURI, Node document) {
		Set<SwaggerError> errors = Sets.newHashSet();

		for (JsonReference reference: collector.collect(document)) {
			if (!reference.isValid(baseURI)) {
				errors.add(createReferenceError((Node) reference.getSource()));
			}
		}

		return Lists.newArrayList();
	}

	private SwaggerError createReferenceError(Node node) {
		int line = node.getStartMark().getLine() + 1;

		return new SwaggerError(line, IMarker.SEVERITY_WARNING, Messages.error_invalid_reference);
	}

}
