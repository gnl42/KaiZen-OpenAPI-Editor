package com.reprezen.swagedit.openapi3.templates;

import java.util.List;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;

import com.google.common.collect.Lists;

public class OpenApi3ContextType extends TemplateContextType {

    private static final String PATH_ITEM_REGEX = "/paths/~1[^/]+";

    public OpenApi3ContextType() {
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

    public static class RootContextType extends OpenApi3ContextType {
        public static final String CONTEXT_ID = "com.reprezen.swagedit.openapi3.templates.root";
    }

    public static class ContactContextType extends OpenApi3ContextType {
        public static final String CONTEXT_ID = "com.reprezen.swagedit.openapi3.templates.info.contact";
    }

    public static class PathsContextType extends OpenApi3ContextType {
        public static final String CONTEXT_ID = "com.reprezen.swagedit.openapi3.templates.paths";
    }

    public static class PathItemContextType extends OpenApi3ContextType {
        public static final String CONTEXT_ID = "com.reprezen.swagedit.openapi3.templates.path_item";
    }

    public static class SchemaContextType extends OpenApi3ContextType {
        public static final String CONTEXT_ID = "com.reprezen.swagedit.openapi3.templates.components.schemas";
    }

    public static List<String> allContextTypes() {
        return Lists.newArrayList( //
                RootContextType.CONTEXT_ID, //
                ContactContextType.CONTEXT_ID, //
                PathsContextType.CONTEXT_ID, //
                PathItemContextType.CONTEXT_ID, //
                SchemaContextType.CONTEXT_ID);
    }

    public static String getContextType(String path) {
        if (path != null && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return RootContextType.CONTEXT_ID;
        }
        if (path.matches("/info/contact")) {
            return ContactContextType.CONTEXT_ID;
        }
        if (path.matches("/paths")) {
            return PathsContextType.CONTEXT_ID;
        }
        if (path.matches(PATH_ITEM_REGEX + "$")) { // /paths/[pathItem]/
            return PathItemContextType.CONTEXT_ID;
        }
        System.out.println(path);
        if (path.matches("/components/schemas")) {
            return SchemaContextType.CONTEXT_ID;
        }
        return null;
    }

}
