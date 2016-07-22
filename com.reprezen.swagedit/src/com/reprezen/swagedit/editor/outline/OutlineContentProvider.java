package com.reprezen.swagedit.editor.outline;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class OutlineContentProvider implements ITreeContentProvider {

    private List<OutlineElement> nodes;

    @Override
    public void dispose() {
        // ignore
        System.out.println("dispose");
        // nodes = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // ignore
        System.out.println("change " + newInput);
        if (newInput != null) {
            System.out.println("change type " + newInput.getClass());
            this.nodes = (List<OutlineElement>) newInput;
        }
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (nodes == null)
            return null;

        return nodes.toArray();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        // ignore
        if (parentElement instanceof OutlineElement) {
            System.out.println(((OutlineElement) parentElement).getChildren());
            return ((OutlineElement) parentElement).getChildren().toArray();
        }
        return null;
    }

    @Override
    public Object getParent(Object element) {
        // ignore
        if (element instanceof OutlineElement) {
            OutlineElement parent = ((OutlineElement) element).getParent();

            if (parent != element) {
                return parent;
            }
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        // ignore
        if (element instanceof OutlineElement) {
            return !((OutlineElement) element).getChildren().isEmpty();
        }
        return false;
    }

}
