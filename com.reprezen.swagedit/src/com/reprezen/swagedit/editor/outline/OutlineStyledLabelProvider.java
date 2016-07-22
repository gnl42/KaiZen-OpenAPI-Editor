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
package com.reprezen.swagedit.editor.outline;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.yaml.snakeyaml.nodes.NodeId;

import com.reprezen.swagedit.Activator;
import com.reprezen.swagedit.Activator.Icons;

public class OutlineStyledLabelProvider extends StyledCellLabelProvider {

    private final Styler TAG_STYLER;
    private final Styler TEXT_STYLER;

    public OutlineStyledLabelProvider() {
        final Color c = new Color(Display.getCurrent(), new RGB(149, 125, 71));

        TAG_STYLER = new Styler() {
            public void applyStyles(TextStyle textStyle) {
                textStyle.foreground = c;
            }
        };

        TEXT_STYLER = new Styler() {
            public void applyStyles(TextStyle textStyle) {
                // do not apply any extra styles
            }
        };
    }

    @Override
    public void update(ViewerCell cell) {
        Object element = cell.getElement();
        if (element instanceof OutlineElement) {
            update(cell, (OutlineElement) element);
        }
    }

    protected void update(ViewerCell cell, OutlineElement element) {
        StyledString styledString = new StyledString(element.getText(), TEXT_STYLER);
        styledString.append(" " + element.getNode().getNodeId(), TAG_STYLER);

        cell.setText(styledString.toString());
        cell.setStyleRanges(styledString.getStyleRanges());
        cell.setImage(getImage(element));
    }

    protected Image getImage(OutlineElement element) {
        Activator activator = Activator.getDefault();
        OutlineElement parent = element.getParent();
        if (parent.getNode().getNodeId() == NodeId.mapping) {

            if (element.getChildren().isEmpty()) {
                return activator.getImage(Icons.outline_mapping_scalar);
            } else {
                return activator.getImage(Icons.outline_mapping);
            }

        } else if (parent.getNode().getNodeId() == NodeId.sequence) {

            if (element.getChildren().isEmpty()) {
                return activator.getImage(Icons.outline_scalar);
            } else {
                return activator.getImage(Icons.outline_sequence);
            }

        } else {
            return activator.getImage(Icons.outline_scalar);
        }
    }
}
