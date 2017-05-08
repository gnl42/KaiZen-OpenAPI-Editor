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
package com.reprezen.swagedit.core.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;

public class JsonReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

    private IDocument document;
    private JsonEditor editor;

    @Override
    public void setProgressMonitor(IProgressMonitor monitor) {
    }

    @Override
    public void initialReconcile() {
        calculatePositions();
        if (editor != null) {
            editor.redrawViewer();
        }
    }

    @Override
    public void setDocument(IDocument document) {
        this.document = document;
    }

    @Override
    public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
        initialReconcile();
    }

    @Override
    public void reconcile(IRegion partition) {
        initialReconcile();
    }

    protected void calculatePositions() {
        if (!(document instanceof JsonDocument))
            return;

        final Node yaml = ((JsonDocument) document).getYaml();
        if (!(yaml instanceof MappingNode)) {
            return;
        }

        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                editor.updateFoldingStructure(calculatePositions((MappingNode) yaml));
            }
        });
    }

    protected List<Position> calculatePositions(MappingNode mapping) {
        List<Position> positions = new ArrayList<>();
        int start;
        int end = -1;

        for (NodeTuple tuple : mapping.getValue()) {
            start = tuple.getKeyNode().getStartMark().getLine();
            end = tuple.getValueNode().getEndMark().getLine();

            if ((end - start) > 0) {
                try {
                    int startOffset = document.getLineOffset(start);
                    int endOffset = document.getLineOffset(end);

                    positions.add(new Position(startOffset, (endOffset - startOffset)));
                } catch (BadLocationException e) {
                }
            }

            if (tuple.getValueNode() instanceof MappingNode) {
                positions.addAll(calculatePositions((MappingNode) tuple.getValueNode()));
            }
        }

        return positions;
    }

    public void setEditor(JsonEditor editor) {
        this.editor = editor;
    }

}
