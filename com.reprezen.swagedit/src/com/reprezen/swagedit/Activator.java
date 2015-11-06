package com.reprezen.swagedit;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.reprezen.swagedit.validation.SwaggerSchema;

public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "com.reprezen.swagedit";
	
	private static Activator plugin;
	private final SwaggerSchema schema = new SwaggerSchema();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		// preload schema
		getSchema().getSchema();		
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

	/**
	 * Returns the swagger schema
	 * 
	 * @return schema
	 */
	public SwaggerSchema getSchema() {
		return schema;
	}
}
