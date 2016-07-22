package com.reprezen.swagedit.editor.outline;

import org.eclipse.jface.viewers.LabelProvider;

public class OutlineStyledLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        if (element instanceof OutlineElement) {
            return ((OutlineElement) element).getText();
        }
        return super.getText(element);
    }

}
