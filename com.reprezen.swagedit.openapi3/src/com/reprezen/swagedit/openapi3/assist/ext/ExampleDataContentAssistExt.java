/**
 * 
 */
package com.reprezen.swagedit.openapi3.assist.ext;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import com.fasterxml.jackson.core.JsonPointer;
import com.reprezen.swagedit.core.assist.ProposalDescriptor;
import com.reprezen.swagedit.core.assist.ext.ContentAssistExt;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.model.ObjectNode;
import com.reprezen.swagedit.core.schema.TypeDefinition;
import com.reprezen.swagedit.openapi3.ExampleDataProvider;

public class ExampleDataContentAssistExt implements ContentAssistExt {

	private static final JsonPointer pointer = JsonPointer.compile("/definitions/any");

	private static final ProposalDescriptor defaultProposal = new ProposalDescriptor("Generate Example")
			.replacementString("example:").type("string");

	@Override
	public boolean canProvideContentAssist(TypeDefinition type) {
		return type != null && type.getPointer() != null && pointer.equals(type.getPointer());
	}

	@Override
	public Collection<ProposalDescriptor> getProposals(TypeDefinition type, AbstractNode node, String prefix) {
		final AbstractNode schemaNode = findSchemaNode(node);

		try {
			final ExampleDataProvider exampleDataProvider = (ExampleDataProvider) getDataProvider();
			if (exampleDataProvider != null) {
				return Arrays.asList(new ProposalDescriptor("Generate Example:")
						.replacementString(exampleDataProvider.getData(schemaNode.getPointerString())).type("string"));
			} else {
				return Arrays.asList(defaultProposal);
			}
		} catch (CoreException e) {
			return Arrays.asList(defaultProposal);
		}

	}

	private AbstractNode findSchemaNode(AbstractNode node) {
		if (node instanceof ObjectNode) {
			final ObjectNode objectNode = (ObjectNode) node;
			if (objectNode.get("schema") != null) {
				return objectNode;
			}
		}
		return findSchemaNode(node.getParent());
	}

	private ExampleDataProvider getDataProvider() throws CoreException {
		final IConfigurationElement[] configurationElementsFor = Platform.getExtensionRegistry()
				.getConfigurationElementsFor("com.reprezen.swagedit.openapi3.exampleprovider");
		return (ExampleDataProvider) configurationElementsFor[0].createExecutableExtension("class");
	}

//	private JsonNode getPointer(AbstractNode node) {
//		final FileEditorInput activeEditorInput = DocumentUtils.getActiveEditorInput();
//		final OpenApi3Document openApi3Document = new OpenApi3Document();
//		try {
//			openApi3Document.set(DocumentUtils.getDocumentContent(activeEditorInput.getPath()));
//			return openApi3Document.asJson().at(node.getParent().getPointer());
//		} catch (IOException e) {
//			// TODO: Handle exception
//		}
//		return null;
//	}

//	@Override
//	public Collection<ProposalDescriptor> getProposals(TypeDefinition type, AbstractNode node, String prefix) {
//		final FileEditorInput activeEditorInput = DocumentUtils.getActiveEditorInput();
//		try {
//			final OpenApi3Document openApi3Document = new OpenApi3Document();
//			openApi3Document.set(DocumentUtils.getDocumentContent(activeEditorInput.getPath()));
//			
//			final JsonNode at = openApi3Document.asJson().at(node.getParent().getPointer());
//			JsonNode jsonNode = at.get("schema");
//			System.out.println(jsonNode);
//			
//			final AbstractNode abstractNode = node.getParent().get("schema");
//			final JsonReference create = factory.create(abstractNode);
//
//			final JsonNode resolve = create.resolve(openApi3Document, activeEditorInput.getURI());
//			normalize(resolve, openApi3Document, activeEditorInput.getURI());
//			return Arrays.asList(
//					new ProposalDescriptor("Generate Example").replacementString(resolve.toString()).type("string"));
//		} catch (IOException e1) {
//			return Arrays
//					.asList(new ProposalDescriptor("Generate Example").replacementString("example:").type("string"));
//		}
//
//	}
//
//	private void normalize(JsonNode node, OpenApi3Document document, URI baseURI) {
//		node.fields().forEachRemaining(entry -> {
//			JsonNode value = entry.getValue();
//			if (isReference(value)) {
//				JsonReference reference = factory.create(value);
//				value = reference.resolve(document, baseURI);
//
//				if (value != null && !value.isMissingNode()) {
//					((ObjectNode) node).set(entry.getKey(), value);
//				}
//			}
//
//			normalize(value, document, baseURI);
//		});
//	}

}
