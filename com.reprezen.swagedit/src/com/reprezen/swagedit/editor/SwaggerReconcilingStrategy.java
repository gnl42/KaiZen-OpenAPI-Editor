package com.reprezen.swagedit.editor;

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

public class SwaggerReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	private SwaggerDocument document;
	private SwaggerEditor editor;

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {}

	@Override
	public void initialReconcile() {
		calculatePositions();
		editor.redrawViewer();
	}

	@Override
	public void setDocument(IDocument document) {
		this.document = (SwaggerDocument) document;
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
		final Node yaml = document.getYaml();
		if (!(yaml instanceof MappingNode)) { 
			return;
		}

		final List<Position> fPositions = new ArrayList<>();
		final MappingNode mapping = (MappingNode) yaml;

		int start;
		int end = -1;
		NodeTuple previous = null;
		for (NodeTuple tuple: mapping.getValue()) {

			if (previous != null) {
				start = previous.getKeyNode().getStartMark().getLine();
				end = tuple.getKeyNode().getStartMark().getLine();

				if ((end - start) > 1) {
					try {
						int startOffset = document.getLineOffset(start);
						int endOffset = document.getLineOffset(end);

						fPositions.add(new Position(startOffset, (endOffset - startOffset)));
					} catch (BadLocationException e) {}
				}
			}

			previous = tuple;
		}

		// handle the last element
		if (previous != null) {
			start = previous.getKeyNode().getStartMark().getLine();
			end = document.getNumberOfLines();

			if ((end - start) > 1) {
				int startOffset = -1, endOffset = -1;

				try {
					startOffset = document.getLineOffset(start);
					endOffset = document.getLineOffset(end);
				} catch (BadLocationException e) {
					// does not have no line at end of document
					try {
						startOffset = document.getLineOffset(start);
						endOffset = document.getLineOffset(end - 1);
					} catch (BadLocationException e1) {
						// forget it
						startOffset = -1;
						endOffset = -1;
					}
				}

				if (startOffset > -1 && endOffset > -1) {
					try {
						fPositions.add(new Position(startOffset, (endOffset - startOffset)));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				editor.updateFoldingStructure(fPositions);
			}
		});
	}

	public void setEditor(SwaggerEditor editor) {
		this.editor = editor;
	}

}
