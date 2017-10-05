package com.reprezen.swagedit.openapi3.hyperlinks;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.fasterxml.jackson.core.JsonPointer;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.hyperlinks.AbstractJsonHyperlinkDetector;
import com.reprezen.swagedit.core.hyperlinks.SwaggerHyperlink;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.model.Model;

public class LinkOperationHyperlinkDetector extends AbstractJsonHyperlinkDetector {

    @Override
    protected boolean canDetect(JsonPointer pointer) {
        return pointer != null && pointer.toString().matches("/components/links/(\\w+)/operationId");
    }

    @Override
    protected IHyperlink[] doDetect(JsonDocument doc, ITextViewer viewer, HyperlinkInfo info, JsonPointer pointer) {
        Model model = doc.getModel();
        AbstractNode node = model.find(pointer);
        List<AbstractNode> nodes = model.findByType(JsonPointer.compile("/definitions/operation"));
        Iterator<AbstractNode> it = nodes.iterator();

        AbstractNode found = null;
        while (it.hasNext() && found == null) {
            AbstractNode current = it.next();
            AbstractNode value = current.get("operationId");

            if (value != null && Objects.equals(node.asValue().getValue(), value.asValue().getValue())) {
                found = value;
            }
        }

        if (found != null) {
            IRegion target = doc.getRegion(found.getPointer());
            if (target != null) {
                return new IHyperlink[] { new SwaggerHyperlink(info.text, viewer, info.region, target) };
            }
        }

        return null;
    }

}
