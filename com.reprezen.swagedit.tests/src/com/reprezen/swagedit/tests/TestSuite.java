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
