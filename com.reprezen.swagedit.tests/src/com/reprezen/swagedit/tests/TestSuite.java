package com.reprezen.swagedit.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.reprezen.swagedit.validation.ErrorProcessorTest;

@RunWith(Suite.class)
@SuiteClasses({
	SwaggerContentAssistProcessorTest.class,
	SwaggerDocumentTest.class,
	SwaggerProposalProviderTest.class,
	SwaggerSchemaTest.class,
	ValidationMessageTest.class,
	ValidatorTest.class,
	ErrorProcessorTest.class,
	CodeTemplateContextTest.class
})
public class TestSuite {}
