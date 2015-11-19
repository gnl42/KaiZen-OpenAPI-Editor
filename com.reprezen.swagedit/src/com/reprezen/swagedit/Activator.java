package com.reprezen.swagedit;

import java.io.IOException;

import org.dadacoalition.yedit.YEditLog;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.reprezen.swagedit.templates.SwaggerContentType;

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
			templateStore = new ContributionTemplateStore(
					getContextTypeRegistry(), 
					getDefault().getPreferenceStore(),
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
			contextTypeRegistry.addContextType(SwaggerContentType.SWAGGER_CONTENT_TYPE);
		}
		return contextTypeRegistry;
	}

}
