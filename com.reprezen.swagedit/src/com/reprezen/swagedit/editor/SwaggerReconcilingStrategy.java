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
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;

public class SwaggerReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	private IDocument document;
	private int fOffset;
	private int fRangeEnd;
	private List<Position> fPositions = new ArrayList<>();
	private int cNextPos;
	private SwaggerEditor editor;

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
	}

	@Override
	public void initialReconcile() {
		fOffset = 0;
		fRangeEnd = document.getLength();
		calculatePositions();

		editor.redrawViewer();

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
		fPositions.clear();
		cNextPos = fOffset;

		final Node node = ((SwaggerDocument) document).getYaml();
		if (node instanceof MappingNode) {
			final MappingNode mappingNode = (MappingNode) node;

			for (NodeTuple tuple : mappingNode.getValue()) {
				final Mark startMark = tuple.getKeyNode().getStartMark();
				final Mark endMark = tuple.getKeyNode().getEndMark();

				try {
					int startOffset = document.getLineOffset(startMark.getLine()) + startMark.getColumn();
					int endOffset = document.getLineOffset(endMark.getLine()) + endMark.getColumn();

					Position position = new Position(startOffset, (endOffset - startOffset));
					fPositions.add(position);
				} catch (BadLocationException e) {
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
