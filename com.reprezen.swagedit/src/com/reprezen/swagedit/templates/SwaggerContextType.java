package com.reprezen.swagedit.templates;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;

import com.google.common.collect.Lists;

public abstract class SwaggerContextType extends TemplateContextType {

	private static final String PARAMETERS_SCHEMA_REGEX = ".*:parameters(:@\\d+):schema";
	private static final String PATH_ITEM_REGEX = ":paths:/[^:]+";

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

	public static String getContextType(String path) {
		// System.out.println("Context Type " + path);
		if (path != null && path.endsWith(":")) {
			path = path.substring(0, path.length() - 1);
		}
		if (path == null || path.isEmpty() || ":".equals(path)) {
			return RootContextType.CONTEXT_ID;
		}
		if (path.equals(":securityDefinitions")) {
			return SecurityDefContextType.CONTEXT_ID;
		}
		if (path.equals(":paths")) {
			return PathsContextType.CONTEXT_ID;
		}
		if (path.matches(PATH_ITEM_REGEX)) { // /paths/[pathItem]/
			return PathItemContextType.CONTEXT_ID;
		}
		if (path.equals(":responses")//
				|| path.matches(PATH_ITEM_REGEX + ":[^:]+:responses")) {
			return ResponsesContextType.CONTEXT_ID;
		}
		if (path.matches(PATH_ITEM_REGEX + ":[^:]*:parameters(:@\\d+)?")
				|| path.matches(PATH_ITEM_REGEX + ":parameters")) {
			return ParametersContextType.CONTEXT_ID;
		}
		if (path.equals(":parameters") //
				|| path.matches(":parameters:[^:]*")) {
			return ParameterContextType.CONTEXT_ID;
		}
		if (path.matches(":definitions:[^:]*") //
				|| path.matches(PARAMETERS_SCHEMA_REGEX)//
				|| path.matches(PARAMETERS_SCHEMA_REGEX + ":items")//
				|| path.matches(PARAMETERS_SCHEMA_REGEX + ":properties:[^:]+")//
				|| path.matches(".*:responses:[^:]*:schema")) {
			return SchemaContextType.CONTEXT_ID;
		}
		return null;
	}

	public static Collection<String> allContextTypes() {
		return Collections.unmodifiableList(Lists.newArrayList(//
				RootContextType.CONTEXT_ID, //
				SecurityDefContextType.CONTEXT_ID, //
				PathsContextType.CONTEXT_ID, //
				PathItemContextType.CONTEXT_ID, //
				ResponsesContextType.CONTEXT_ID, //
				ParametersContextType.CONTEXT_ID, //
				ParameterContextType.CONTEXT_ID, //
				SchemaContextType.CONTEXT_ID));
	}

	public static class PathItemContextType extends SwaggerContextType {
		public static final String CONTEXT_ID = "com.reprezen.swagedit.templates.swagger.path_item";
	}

	public static class SecurityDefContextType extends SwaggerContextType {
		public static final String CONTEXT_ID = "com.reprezen.swagedit.templates.swagger.security_def";
	}

	public static class RootContextType extends SwaggerContextType {
		public static final String CONTEXT_ID = "com.reprezen.swagedit.templates.swagger.root";
	}

	public static class PathsContextType extends SwaggerContextType {
		public static final String CONTEXT_ID = "com.reprezen.swagedit.templates.swagger.paths";

	}

	public static class ResponsesContextType extends SwaggerContextType {
		public static final String CONTEXT_ID = "com.reprezen.swagedit.templates.swagger.responses";
	}

	public static class ParametersContextType extends SwaggerContextType {
		public static final String CONTEXT_ID = "com.reprezen.swagedit.templates.swagger.parameters_list";
	}

	public static class ParameterContextType extends SwaggerContextType {
		public static final String CONTEXT_ID = "com.reprezen.swagedit.templates.swagger.parameter_single";
	}

	public static class SchemaContextType extends SwaggerContextType {
		public static final String CONTEXT_ID = "com.reprezen.swagedit.templates.swagger.schema";
	}
}
