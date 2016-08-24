package com.reprezen.swagedit.mocks;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.ITextViewer;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.editor.hyperlinks.JsonReferenceHyperlinkDetector;
import com.reprezen.swagedit.json.JsonDocumentManager;
import com.reprezen.swagedit.json.references.JsonReference;
import com.reprezen.swagedit.json.references.JsonReferenceFactory;

public class Mocks {

    public static JsonReferenceHyperlinkDetector mockHyperlinkDetector(final URI uri, final JsonNode document) {
        final JsonDocumentManager manager = mock(JsonDocumentManager.class);
        final IFile file = mock(IFile.class);

        when(file.exists()).thenReturn(true);
        when(manager.getDocument(Mockito.any(URI.class))).thenReturn(document);
        when(manager.getFile(Mockito.any(URI.class))).thenReturn(file);

        return new JsonReferenceHyperlinkDetector() {
            // allow running tests as non plugin tests
            protected URI getBaseURI() {
                return uri;
            }

            protected JsonReferenceFactory getFactory() {
                return new JsonReferenceFactory() {
                    public JsonReference create(JsonNode node) {
                        JsonReference ref = super.create(node);
                        ref.setDocumentManager(manager);
                        return ref;
                    };
                };
            }
        };
    }

    public static JsonReferenceFactory mockJsonReferenceFactory(final Map<URI, JsonNode> entries) {
        final IFile file = mock(IFile.class);
        when(file.exists()).thenReturn(true);

        return new JsonReferenceFactory() {
            public JsonReference create(org.yaml.snakeyaml.nodes.ScalarNode node) {
                JsonReference ref = super.create(node);
                ref.setDocumentManager(new JsonDocumentManager() {
                    @Override
                    public IFile getFile(URI uri) {
                        return file;
                    }

                    @Override
                    public JsonNode getDocument(URI uri) {
                        return entries.get(uri);
                    }
                });
                return ref;
            };
        };
    }

    public static IFile mockJsonReferenceProposalFile() {
        IFile file = mock(IFile.class);
        IContainer parent = mock(IContainer.class);
        IPath parentPath = mock(IPath.class);

        when(file.getParent()).thenReturn(parent);
        when(parent.getFullPath()).thenReturn(parentPath);

        return file;
    }

    public static IPath mockPath(String path) {
        IPath mock = mock(IPath.class);
        when(mock.toString()).thenReturn(path);
        return mock;
    }

    public static ITextViewer mockTextViewer() {
        ITextViewer viewer = mock(ITextViewer.class);
        return viewer;
    }

}
