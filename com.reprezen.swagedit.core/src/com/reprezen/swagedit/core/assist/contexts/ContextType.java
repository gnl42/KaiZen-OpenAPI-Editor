package com.reprezen.swagedit.core.assist.contexts;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.core.runtime.IPath;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.assist.Proposal;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.utils.URLUtils;
import com.reprezen.swagedit.core.validation.ValidationUtil;

/**
 * Represents the different contexts for which a JSON reference may be computed. <br/>
 * The context type is determined by the pointer (path) on which the completion proposal has been activated.
 */
public class ContextType {
    public static final ContextType UNKNOWN = new ContextType(null, "", null);

    private final String value;
    private final String label;
    final String regex;
    private final boolean isLocalOnly;

    public ContextType(String value, String label, String regex) {
        this.value = value;
        this.label = label;
        this.regex = regex;
        this.isLocalOnly = false;
    }

    public ContextType(String value, String label, String regex, boolean isLocalOnly) {
        this.value = value;
        this.label = label;
        this.regex = regex;
        this.isLocalOnly = isLocalOnly;
    }

    public String value() {
        return value;
    }

    public String label() {
        return label;
    }

    public boolean isLocalOnly() {
        return isLocalOnly;
    }

    public Collection<Proposal> collectProposals(JsonDocument document, IPath path) {
        return collectProposals(document.asJson(), path);
    }

    /**
     * Returns all proposals found in the document at the specified field.
     * 
     * @param document
     * @param fieldName
     * @param path
     * @return Collection of proposals
     */
    public Collection<Proposal> collectProposals(JsonNode document, IPath path) {
        final Collection<Proposal> results = Lists.newArrayList();
        if (value() == null) {
            return results;
        }

        final JsonNode nodes = ValidationUtil.findNode(value(), document);
        if (nodes == null) {
            return results;
        }

        final String basePath = (path != null ? path.toString() : "") + "#/" + value() + "/";

        for (Iterator<String> it = nodes.fieldNames(); it.hasNext();) {
            String key = it.next();
            String value = basePath + key.replaceAll("/", "~1");
            String encoded = URLUtils.encodeURL(value);

            results.add(new Proposal("\"" + encoded + "\"", key, null, value));
        }

        return results;
    }

    public static ContextTypeCollection newContentTypeCollection(Iterable<ContextType> contextTypes) {
        return new ContextTypeCollection(contextTypes);
    }

    public static ContextTypeCollection emptyContentTypeCollection() {
        return new ContextTypeCollection(Collections.<ContextType> emptyList());
    }
}