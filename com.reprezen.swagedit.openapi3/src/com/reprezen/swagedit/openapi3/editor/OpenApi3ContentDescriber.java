package com.reprezen.swagedit.openapi3.editor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;

public class OpenApi3ContentDescriber implements IContentDescriber {

    @Override
    public int describe(InputStream contents, IContentDescription description) throws IOException {
        String content = toString(contents);
        if (content.trim().isEmpty()) {
            return INDETERMINATE;
        }

        return content.contains("openapi: \"3.0.") || content.contains("openapi: '3.0") ? VALID : INVALID;
    }

    @Override
    public QualifiedName[] getSupportedOptions() {
        return null;
    }

    protected String toString(InputStream contents) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = contents.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

}
