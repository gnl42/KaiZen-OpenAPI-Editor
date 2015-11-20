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

	private IDocument document;
	private SwaggerEditor editor;

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
		if (!(document instanceof SwaggerDocument))
			return;

		final Node yaml = ((SwaggerDocument) document).getYaml();
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

	public void setEditor(SwaggerEditor editor) {
		this.editor = editor;
	}

}
