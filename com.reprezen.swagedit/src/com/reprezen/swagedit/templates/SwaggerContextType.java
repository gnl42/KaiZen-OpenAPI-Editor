package com.reprezen.swagedit.templates;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;

import com.google.common.collect.Lists;

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
		System.out.println("Context Type " + path);
		if (path != null && path.endsWith(":")) {
			path = path.substring(0, path.length() - 1);
		}
		if (path == null || path.isEmpty() || ":".equals(path)) {
			return RootContextType.ROOT_CONTENT_TYPE;
		}
		if (path.equals(":securityDefinitions")) {
			return SecurityDefContextType.SECURITY_DEF_CONTENT_TYPE;
		}
		if (path.equals(":paths")) {
			return PathsContextType.PATHS_CONTENT_TYPE;
		}
		if (path.matches(":paths:(/[^:]*)+")) { // /paths/[pathItem]/
			return PathItemContextType.PATH_ITEM_CONTENT_TYPE;
		}
		if (path.equals(":responses") || path.matches(":paths:/[^:]*:[^:]*:responses")) {
			return ResponsesContextType.CONTENT_TYPE;
		}
		return null;
	}

	public static Collection<String> allContextTypes() {
		return Collections.unmodifiableList(Lists.newArrayList(//
				RootContextType.ROOT_CONTENT_TYPE, //
				SecurityDefContextType.SECURITY_DEF_CONTENT_TYPE, //
				PathsContextType.PATHS_CONTENT_TYPE, //
				PathItemContextType.PATH_ITEM_CONTENT_TYPE, //
				ResponsesContextType.CONTENT_TYPE));
	}

	public static class PathItemContextType extends SwaggerContextType {
		public static final String PATH_ITEM_CONTENT_TYPE = "com.reprezen.swagedit.templates.swagger.path_item";
	}

	public static class SecurityDefContextType extends SwaggerContextType {
		public static final String SECURITY_DEF_CONTENT_TYPE = "com.reprezen.swagedit.templates.swagger.security_def";
	}

	public static class RootContextType extends SwaggerContextType {
		public static final String ROOT_CONTENT_TYPE = "com.reprezen.swagedit.templates.swagger.root";
	}

	public static class PathsContextType extends SwaggerContextType {
		public static final String PATHS_CONTENT_TYPE = "com.reprezen.swagedit.templates.swagger.paths";

	}

	public static class ResponsesContextType extends SwaggerContextType {
		public static final String CONTENT_TYPE = "com.reprezen.swagedit.templates.swagger.responses";
	}

}
