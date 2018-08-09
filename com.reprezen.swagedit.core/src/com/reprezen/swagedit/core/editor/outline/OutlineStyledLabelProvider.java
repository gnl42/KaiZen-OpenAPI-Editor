/*******************************************************************************
 * Copyright (c) 2016 ModelSolv, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.core.editor.outline;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.core.Activator;
import com.reprezen.swagedit.core.Activator.Icons;
import com.reprezen.swagedit.core.json.JsonModel;
import com.reprezen.swagedit.core.schema.TypeDefinition;

public class OutlineStyledLabelProvider extends StyledCellLabelProvider {

    private final Styler TAG_STYLER;
    private final Styler TEXT_STYLER;

    private final Activator activator = Activator.getDefault();

    public OutlineStyledLabelProvider() {
        TAG_STYLER = new Styler() {
            public void applyStyles(TextStyle textStyle) {
                textStyle.foreground = getColor(new RGB(149, 125, 71));
            }
        };

        TEXT_STYLER = new Styler() {
            public void applyStyles(TextStyle textStyle) {
                // do not apply any extra styles
            }
        };
    }

    public Styler getTagStyler() {
        return TAG_STYLER;
    }

    public Styler getTextStyler() {
        return TEXT_STYLER;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update(ViewerCell cell) {
        Object element = cell.getElement();

        if (element instanceof Pair) {
            JsonModel model = ((Pair<JsonModel, JsonPointer>) element).getLeft();
            JsonPointer ptr = ((Pair<JsonModel, JsonPointer>) element).getRight();

            StyledString styledString = getStyledString(model, ptr);

            cell.setText(styledString.toString());
            cell.setStyleRanges(styledString.getStyleRanges());
            cell.setImage(getImage(getIcon(model, ptr)));
        }
    }

    public StyledString getStyledString(JsonModel model, JsonPointer ptr) {
        JsonNode node = model.getContent().at(ptr);
        JsonNode parent = ptr.head() == null ? null : model.getContent().at(ptr.head());

        StyledString styledString = new StyledString(getText(node, ptr), getTextStyler());

        if (parent != null && (node.isObject() || node.isArray())) {

            TypeDefinition definition = model.getTypes().get(ptr);

            String label = null;
            if (definition != null && definition.asJson() != null) {
                if (definition.asJson().has("title")) {
                    label = definition.asJson().get("title").asText();
                } else if (definition.getContainingProperty() != null) {
                    label = definition.getContainingProperty();
                }
            }

            if (label != null) {
                styledString.append(" ");
                styledString.append(label, getTagStyler());
            }

        } else if (parent == null) {
            if (model.getPath() != null) {
                styledString.append(" ");
                styledString.append(model.getPath().toString(), getTagStyler());
            }
        }
        return styledString;
    }

    public String getText(JsonNode node, JsonPointer ptr) {
        if (node.isObject() || node.isArray()) {
            return ptr.getMatchingProperty();
        } else {
            return ptr.getMatchingProperty() + " : " + node.asText();
        }
    }

    protected Icons getIcon(JsonModel model, JsonPointer ptr) {
        JsonNode parent = ptr.head() == null ? null : model.getContent().at(ptr.head());
        JsonNode node = model.getContent().at(ptr);

        if (parent == null) {
            return Icons.outline_document;
        }

        if (parent.isObject()) {
            if (!node.elements().hasNext()) {
                return Icons.outline_mapping_scalar;
            } else {
                return Icons.outline_mapping;
            }
        } else if (parent.isArray()) {
            if (!node.elements().hasNext()) {
                return Icons.outline_scalar;
            } else {
                return Icons.outline_sequence;
            }
        } else {
            return Icons.outline_scalar;
        }
    }

    protected Color getColor(RGB rgb) {
        return new Color(Display.getCurrent(), rgb);
    }

    protected Image getImage(Icons icon) {
        return activator.getImage(icon);
    }

}
