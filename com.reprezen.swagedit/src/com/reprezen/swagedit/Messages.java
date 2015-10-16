package com.reprezen.swagedit;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.reprezen.swagedit.messages";

	public static String swagger;
	public static String wizard_description;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {}

}
