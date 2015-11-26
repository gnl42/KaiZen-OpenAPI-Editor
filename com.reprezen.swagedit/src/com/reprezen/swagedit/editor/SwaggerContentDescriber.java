package com.reprezen.swagedit.editor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;

import com.google.common.io.CharStreams;

public class SwaggerContentDescriber implements IContentDescriber {

	@Override
	public int describe(InputStream contents, IContentDescription description) throws IOException {
		String content = CharStreams.toString(new InputStreamReader(contents));
		if (content.trim().isEmpty()) {
			return INDETERMINATE;
		}

		return content.contains("swagger") ? VALID : INVALID;
	}

	@Override
	public QualifiedName[] getSupportedOptions() {
		return null;
	}

}
