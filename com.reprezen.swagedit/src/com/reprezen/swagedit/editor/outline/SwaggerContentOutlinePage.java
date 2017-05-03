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

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.toArray;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import com.google.common.base.Predicate;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.model.Model;
import com.reprezen.swagedit.editor.SwaggerDocument;

public class SwaggerContentOutlinePage extends ContentOutlinePage {

    private final IDocumentProvider documentProvider;
    private final IShowInTarget showInTarget;

    private Object currentInput;

    public SwaggerContentOutlinePage(IDocumentProvider documentProvider, IShowInTarget showInTarget) {
        super();
        this.documentProvider = documentProvider;
        this.showInTarget = showInTarget;
    }

    public void setInput(Object input) {
        this.currentInput = input;
        update();
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        TreeViewer viewer = getTreeViewer();
        viewer.setContentProvider(new OutlineContentProvider());
        viewer.setLabelProvider(new OutlineStyledLabelProvider());
        viewer.addSelectionChangedListener(this);
        viewer.setAutoExpandLevel(2);
        viewer.setUseHashlookup(true);

        if (currentInput != null) {
            setInput(currentInput);
        }
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        super.selectionChanged(event);

        showInTarget.show(new ShowInContext(null, event.getSelection()));
    }

    protected void update() {
        final IDocument document = documentProvider.getDocument(currentInput);

        if (document instanceof SwaggerDocument) {
            final Model model = ((SwaggerDocument) document).getModel();
            if (model == null) {
                return;
            }

            final TreeViewer viewer = getTreeViewer();

            if (viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed()) {
                // we keep all elements that have been previously expanded
                // so the tree stay in the same state between updates.
                final Object[] expandedElements = viewer.getExpandedElements();
                final List<Object> elements = expandedElements != null ? Arrays.asList(expandedElements) : null;

                viewer.setInput(model);

                if (elements != null && !elements.isEmpty()) {
                    Iterable<AbstractNode> newElements = filter(model.allNodes(), new Predicate<AbstractNode>() {
                        @Override
                        public boolean apply(AbstractNode node) {
                            return elements.contains(node);
                        }
                    });
                    viewer.setExpandedElements(toArray(newElements, AbstractNode.class));
                }
            }
        }
    }
}
