/**
 * 
 */
package com.reprezen.swagedit.openapi3.assist.ext;

import static com.reprezen.swagedit.core.json.references.JsonReference.isReference;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.core.assist.ProposalDescriptor;
import com.reprezen.swagedit.core.assist.ext.ContentAssistExt;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.json.references.JsonReference;
import com.reprezen.swagedit.core.json.references.JsonReferenceFactory;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.schema.TypeDefinition;
import com.reprezen.swagedit.core.utils.DocumentUtils;
import com.reprezen.swagedit.core.utils.ExtensionUtils;
import com.reprezen.swagedit.core.utils.ModelUtils;
import com.reprezen.swagedit.openapi3.ExampleDataProvider;

public class ExampleDataContentAssistExt implements ContentAssistExt {

	private static final JsonPointer pointer = JsonPointer.compile("/definitions/any");

	private static final ProposalDescriptor defaultProposal = new ProposalDescriptor("Generate Example")
			.replacementString("example:").type("string");

	private static final String extensionPointName = "com.reprezen.swagedit.openapi3.exampleprovider";

	@Override
	public boolean canProvideContentAssist(TypeDefinition type) {
		return type != null && type.getPointer() != null && pointer.equals(type.getPointer());
	}

	@Override
	public Collection<ProposalDescriptor> getProposals(TypeDefinition type, AbstractNode node, String prefix,
			JsonDocument jsonDocument) {
		final JsonNode jsonSchemaNode = getSchemaNode(node, jsonDocument);
		// TODO: Can the jsonschemaNode be null???
		new JsonSchemaNormalizer(jsonDocument, DocumentUtils.getActiveEditorInputURI()).normalize(jsonSchemaNode);

		try {
			final ExampleDataProvider exampleDataProvider = (ExampleDataProvider) getExampleDataProvider();
			final String exampleData = exampleDataProvider.getData(jsonSchemaNode);
			return Arrays
					.asList(new ProposalDescriptor("Generate Example:").replacementString(exampleData).type("string"));
		} catch (CoreException e) {
			// TODO: Log a message here and then return the default proposal
			return Arrays.asList(defaultProposal);
		}

	}

	private JsonNode getSchemaNode(AbstractNode node, JsonDocument jsonDocument) {
		final AbstractNode schemaNode = ModelUtils.getParentNode(node, "schema");
		// TODO: What happens if there is no schema node?????
		final JsonPointer schemaPointer = JsonPointer.compile(schemaNode.getPointerString());
		return jsonDocument.asJson().at(schemaPointer);
	}

	private ExampleDataProvider getExampleDataProvider() throws CoreException {
		return (ExampleDataProvider) ExtensionUtils.createExecutableExtension(extensionPointName, "class");
	}

	private static class JsonSchemaNormalizer {

		private final JsonDocument document;
		private final URI baseURI;

		private static final JsonReferenceFactory factory = new JsonReferenceFactory();

		public JsonSchemaNormalizer(JsonDocument document, URI baseURI) {
			this.document = document;
			this.baseURI = baseURI;
		}

		private void normalize(JsonNode node) {
			node.fields().forEachRemaining(entry -> {
				JsonNode value = entry.getValue();
				if (isReference(value)) {
					final JsonReference reference = factory.create(value);
					value = reference.resolve(document, baseURI);

					if (value != null && !value.isMissingNode()) {
						((com.fasterxml.jackson.databind.node.ObjectNode) node).set(entry.getKey(), value);
					}
				}
				normalize(value);
			});
		}

	}

}
