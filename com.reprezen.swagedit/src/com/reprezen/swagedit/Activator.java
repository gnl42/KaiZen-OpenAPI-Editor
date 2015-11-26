package com.reprezen.swagedit;

import java.io.IOException;
import java.net.URL;

import org.dadacoalition.yedit.YEditLog;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.reprezen.swagedit.templates.PathContextType;
import com.reprezen.swagedit.templates.RootContextType;

public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "com.reprezen.swagedit";
	public static final String TEMPLATE_STORE_ID = PLUGIN_ID + ".templates";

	private static Activator plugin;

	private ContributionTemplateStore templateStore;

	private ContributionContextTypeRegistry contextTypeRegistry;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		Bundle bundle = Platform.getBundle(PLUGIN_ID);
		IPath path = new Path("icons/swagger_16.jpg");
		URL url = FileLocator.find(bundle, path, null);
		getImageRegistry().put("swagger_16", ImageDescriptor.createFromURL(url));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
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
			contextTypeRegistry.addContextType(RootContextType.ROOT_CONTENT_TYPE);
			contextTypeRegistry.addContextType(PathContextType.PATH_CONTENT_TYPE);
		}
		return contextTypeRegistry;
	}

}
