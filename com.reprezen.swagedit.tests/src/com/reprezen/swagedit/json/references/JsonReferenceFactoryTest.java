package com.reprezen.swagedit.json.references;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonPointer;

public class JsonReferenceFactoryTest {

    @Test
    public void test() throws URISyntaxException {
        JsonReferenceFactory factory = new JsonReferenceFactory();

        JsonReference result = factory.doCreate("#/definitions/Foo", null);

        assertEquals(new URI(null, null, "/definitions/Foo"), result.getUri());
        assertTrue(result.isLocal());
        assertFalse(result.isAbsolute());
        assertEquals(JsonPointer.compile("/definitions/Foo"), result.getPointer());
    }

    @Test
    public void test2() throws URISyntaxException {
        JsonReferenceFactory factory = new JsonReferenceFactory();

        JsonReference result = factory.doCreate("doc.yaml#/definitions/Foo", null);

        assertEquals(new URI(null, "doc.yaml", "/definitions/Foo"), result.getUri());
        assertFalse(result.isLocal());
        assertFalse(result.isAbsolute());
        assertEquals(JsonPointer.compile("/definitions/Foo"), result.getPointer());
    }

    @Test
    public void test3() throws URISyntaxException {
        JsonReferenceFactory factory = new JsonReferenceFactory();

        JsonReference result = factory.doCreate("../doc.yaml#/definitions/Foo", null);

        assertEquals(new URI(null, "../doc.yaml", "/definitions/Foo"), result.getUri());
        assertFalse(result.isLocal());
        assertFalse(result.isAbsolute());
        assertEquals(JsonPointer.compile("/definitions/Foo"), result.getPointer());
    }

    @Test
    public void test4() throws URISyntaxException {
        JsonReferenceFactory factory = new JsonReferenceFactory();

        JsonReference result = factory.doCreate("file://path/to/file/doc.yaml#/definitions/Foo", null);

        assertEquals(URI.create("file://path/to/file/doc.yaml#/definitions/Foo"), result.getUri());
        assertFalse(result.isLocal());
        assertTrue(result.isAbsolute());
        assertEquals(JsonPointer.compile("/definitions/Foo"), result.getPointer());
    }

    @Test
    public void test5() throws URISyntaxException {
        JsonReferenceFactory factory = new JsonReferenceFactory();

        JsonReference result = factory.doCreate("file://path/to/file/doc.yaml#", null);

        assertEquals(URI.create("file://path/to/file/doc.yaml#"), result.getUri());
        assertFalse(result.isLocal());
        assertTrue(result.isAbsolute());
        assertEquals(JsonPointer.compile(""), result.getPointer());
    }

    @Test
    public void test6() throws URISyntaxException {
        JsonReferenceFactory factory = new JsonReferenceFactory();

        JsonReference result = factory.doCreate("file://path/to/file/doc.yaml", null);

        assertEquals(URI.create("file://path/to/file/doc.yaml"), result.getUri());
        assertFalse(result.isLocal());
        assertTrue(result.isAbsolute());
        assertEquals(JsonPointer.compile(""), result.getPointer());
    }

}
