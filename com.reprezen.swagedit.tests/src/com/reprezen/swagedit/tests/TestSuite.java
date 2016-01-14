package com.reprezen.swagedit.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	SwaggerContentAssistProcessorTest.class,
	SwaggerDocumentTest.class,
	SwaggerProposalProviderTest.class,
	SwaggerSchemaTest.class,
	ValidationMessageTest.class,
	ValidatorTest.class
})
public class TestSuite {}
