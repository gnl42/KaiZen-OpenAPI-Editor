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
package com.reprezen.swagedit.validation;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.nodes.Node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reprezen.swagedit.core.validation.ErrorProcessor;
import com.reprezen.swagedit.core.validation.SwaggerError;

public class ErrorProcessorTest {

    private ErrorProcessor processor;
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        Node document = null;
        processor = new ErrorProcessor(document);
    }

    @Test
    public void testProcessNode_WithSingleError() throws Exception {
        JsonNode fixture = mapper.readTree(Paths.get("resources", "error-1.json").toFile());
        Set<SwaggerError> errors = processor.processMessageNode(fixture);

        assertEquals(1, errors.size());
        assertTrue(getOnlyElement(errors) instanceof SwaggerError);
    }

    @Test
    public void testProcessNode_WithOneOfError() throws Exception {
        JsonNode fixture = mapper.readTree(Paths.get("resources", "error-2.json").toFile());
        Set<SwaggerError> errors = processor.processMessageNode(fixture);

        assertEquals(1, errors.size());
        assertTrue(getOnlyElement(errors) instanceof SwaggerError.MultipleSwaggerError);
    }

}
