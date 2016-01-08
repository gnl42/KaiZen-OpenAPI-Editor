package com.reprezen.swagedit;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.reprezen.swagedit.messages";

	// UI
	public static String swagger;
	public static String wizard_description;

	// errors
	public static String error_typeNoMatch;
	public static String error_notInEnum;
	public static String error_additional_properties_not_allowed;
	public static String error_required_properties;

	// content assist
	public static String no_default_proposals;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {}

}
