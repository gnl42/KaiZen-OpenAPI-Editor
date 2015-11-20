package com.reprezen.swagedit.templates;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;

public abstract class SwaggerContextType extends TemplateContextType {

	public SwaggerContextType() {
		addGlobalResolvers();
	}

	private void addGlobalResolvers() {
        addResolver(new GlobalTemplateVariables.Cursor());
        addResolver(new GlobalTemplateVariables.WordSelection());
        addResolver(new GlobalTemplateVariables.LineSelection());
        addResolver(new GlobalTemplateVariables.Dollar());
        addResolver(new GlobalTemplateVariables.Date());
        addResolver(new GlobalTemplateVariables.Year());
        addResolver(new GlobalTemplateVariables.Time());
        addResolver(new GlobalTemplateVariables.User());
    }

	public static String getContentType(String path) {
		if (path == null || path.isEmpty() || ":".equals(path))
			return RootContextType.ROOT_CONTENT_TYPE;

		if (path.matches(":paths:/")) {
			return PathContextType.PATH_CONTENT_TYPE;
		}

		return null;
	}

}
