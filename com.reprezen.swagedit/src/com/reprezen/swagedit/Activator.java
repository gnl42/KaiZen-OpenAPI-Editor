/*******************************************************************************
 * Copyright (c) 2016 ModelSolv, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit;

import java.io.IOException;

import org.dadacoalition.yedit.YEditLog;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.reprezen.swagedit.schema.SwaggerSchema;
import com.reprezen.swagedit.templates.SwaggerContextType;

public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "com.reprezen.swagedit";
    public static final String TEMPLATE_STORE_ID = PLUGIN_ID + ".templates";

    private static Activator plugin;

    private ContributionTemplateStore templateStore;

    private ContributionContextTypeRegistry contextTypeRegistry;
    private SwaggerSchema schema;

    /**
     * Bundle icons
     * 
     * Enumeration of images that are registered in this bundle image registry at startup.
     * 
     */
    public enum Icons {
        assist_item, template_item, outline_document, outline_scalar, outline_mapping, outline_sequence, outline_mapping_scalar
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework. BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        if (Display.getCurrent() != null && PlatformUI.isWorkbenchRunning()) {
            Bundle bundle = Platform.getBundle(PLUGIN_ID);
            addImage(bundle, Icons.assist_item.name(), "icons/assist_item_16.png");
            addImage(bundle, Icons.template_item.name(), "icons/template_item_16.png");

            // for quick outline, add icons from YEdit
            bundle = Platform.getBundle(org.dadacoalition.yedit.Activator.PLUGIN_ID);
            addImage(bundle, Icons.outline_document.name(), "icons/outline_document.gif");
            addImage(bundle, Icons.outline_mapping.name(), "icons/outline_mapping.gif");
            addImage(bundle, Icons.outline_scalar.name(), "icons/outline_scalar.gif");
            addImage(bundle, Icons.outline_mapping_scalar.name(), "icons/outline_mappingscalar.gif");
            addImage(bundle, Icons.outline_sequence.name(), "icons/outline_sequence.png");
        }
    }

    protected void addImage(Bundle bundle, String key, String path) {
        ImageDescriptor imageDescriptor = getImageDescriptor(bundle, path);
        if (imageDescriptor != null) {
            getImageRegistry().put(key, imageDescriptor);
        }
    }

    /**
     * Returns image present in this bundle image registry under the icon's name.
     * 
     * @param icon
     * @return image
     */
    public Image getImage(Icons icon) {
        return getImageRegistry().get(icon.name());
    }

    private ImageDescriptor getImageDescriptor(Bundle bundle, String pathName) {
        Path path = new Path(pathName);
        return ImageDescriptor.createFromURL(FileLocator.find(bundle, path, null));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework. BundleContext)
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

    public TemplateStore getTemplateStore() {
        if (templateStore == null) {
            templateStore = new ContributionTemplateStore(getContextTypeRegistry(), getDefault().getPreferenceStore(),
                    TEMPLATE_STORE_ID);

            try {
                templateStore.load();
            } catch (IOException e) {

                YEditLog.logException(e);
            }
        }
        return templateStore;
    }

    public ContextTypeRegistry getContextTypeRegistry() {
        if (contextTypeRegistry == null) {
            contextTypeRegistry = new ContributionContextTypeRegistry();
            for (String contextType : SwaggerContextType.allContextTypes()) {
                contextTypeRegistry.addContextType(contextType);
            }
        }
        return contextTypeRegistry;
    }

    public SwaggerSchema getSchema() {
        if (schema == null) {
            schema = new SwaggerSchema();
        }

        return schema;
    }
}
