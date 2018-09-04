/*******************************************************************************
 *  Copyright (c) 2016 ModelSolv, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *     ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.openapi3;

import java.io.IOException;
import java.util.Arrays;

import org.dadacoalition.yedit.YEditLog;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.reprezen.swagedit.core.preferences.KaiZenPreferencesUtils;
import com.reprezen.swagedit.openapi3.schema.OpenApi3Schema;
import com.reprezen.swagedit.openapi3.templates.OpenApi3ContextTypeProvider;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.reprezen.swagedit.openapi3"; //$NON-NLS-1$
    public static final String TEMPLATE_STORE_ID = PLUGIN_ID + ".templates"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private OpenApi3Schema schema;
    private ContextTypeRegistry contextTypeRegistry;
    private ContributionTemplateStore templateStore;
    private OpenApi3ContextTypeProvider openApi3ContextTypes;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
    public OpenApi3Schema getSchema() {
        if (schema == null) {
            schema = new OpenApi3Schema();
        }
        return schema;
    }

    public ContextTypeRegistry getContextTypeRegistry() {
        if (contextTypeRegistry == null) {
            contextTypeRegistry = new ContextTypeRegistry();
            for (TemplateContextType contextType : getOpenApi3ContextTypeProvider().allContextTypes()) {
                contextTypeRegistry.addContextType(contextType);
            }
        }
        return contextTypeRegistry;
    }
    
    public OpenApi3ContextTypeProvider getOpenApi3ContextTypeProvider() {
        if (openApi3ContextTypes == null) {
            openApi3ContextTypes = new OpenApi3ContextTypeProvider();
        }
        return openApi3ContextTypes;
    }

    public TemplateStore getTemplateStore() {
        if (templateStore == null) {
            templateStore = new ContributionTemplateStore(getContextTypeRegistry(), getDefault().getPreferenceStore(),
                    TEMPLATE_STORE_ID);
            try {
                templateStore.load();
            } catch (IOException e) {
                YEditLog.logException(e);
            }
            addNamedSchemaTemplatesInSchemas();
            addNamedSchemaTemplatesInSchemaProperties();
        }
        return templateStore;
    }

    private void addNamedSchemaTemplatesInSchemas() {
        addNamedTemplates("com.reprezen.swagedit.openapi3.templates.schema",
                "com.reprezen.swagedit.openapi3.templates.schemas", "schema");
    }

    private void addNamedSchemaTemplatesInSchemaProperties() {
        addNamedTemplates("com.reprezen.swagedit.openapi3.templates.schema",
                "com.reprezen.swagedit.openapi3.templates.properties", "property");
    }

    private void addNamedTemplates(String inlineContextId, String namedContextId, String key) {
        Template[] schemaTemplates = templateStore.getTemplates(inlineContextId);
        for (int i = 0; i < schemaTemplates.length; i++) {
            Template schemaTemplate = schemaTemplates[i];
            Template template = createNamedTemplate(schemaTemplate, namedContextId, key);
            templateStore.add(new TemplatePersistenceData(template, true));
        }
    }

    private Template createNamedTemplate(Template inlineTemplate, String newTemplateId, String key) {
        char[] indentChars = new char[getTabWidth()];
        Arrays.fill(indentChars, ' ');
        String indent = String.valueOf(indentChars);
        
        String newPattern = inlineTemplate.getPattern().replaceAll("\n", "\n" + indent);
        String pattern = String.format("${element_name:element_name('(%s name)')}:\n%s%s", key, indent, newPattern);
        Template template = new Template(inlineTemplate.getName(), //
                inlineTemplate.getDescription(), //
                newTemplateId, //
                pattern, //
                inlineTemplate.isAutoInsertable());
        return template;
    }

    protected int getTabWidth() {
       return KaiZenPreferencesUtils.getTabWidth();
    }

}
