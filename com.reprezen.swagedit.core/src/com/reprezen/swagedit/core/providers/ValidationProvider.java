package com.reprezen.swagedit.core.providers;

import java.net.URI;
import java.util.Set;

import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.validation.SwaggerError;

public interface ValidationProvider {

    Set<SwaggerError> validate(JsonDocument document, URI baseURI, AbstractNode node);

}
