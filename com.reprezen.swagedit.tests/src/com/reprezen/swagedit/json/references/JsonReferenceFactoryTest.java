package com.reprezen.swagedit.json.references;

import org.junit.Test;

public class JsonReferenceFactoryTest {

	@Test
	public void test() {
		JsonReferenceFactory factory = new JsonReferenceFactory();

		JsonReference result = factory.doCreate("#/definitions/Foo", null);
		System.out.println(result.getUri());
		System.out.println(result.isLocal());
		System.out.println(result.isAbsolute());
		System.out.println(result.getPointer());
	}

	@Test
	public void test2() {
		JsonReferenceFactory factory = new JsonReferenceFactory();

		JsonReference result = factory.doCreate("doc.yaml#/definitions/Foo", null);
		System.out.println(result.getUri());
		System.out.println(result.isLocal());
		System.out.println(result.isAbsolute());
		System.out.println(result.getPointer());
	}

	@Test
	public void test3() {
		JsonReferenceFactory factory = new JsonReferenceFactory();

		JsonReference result = factory.doCreate("../doc.yaml#/definitions/Foo", null);
		System.out.println(result.getUri());
		System.out.println(result.isLocal());
		System.out.println(result.isAbsolute());
		System.out.println(result.getPointer());
	}

	@Test
	public void test4() {
		JsonReferenceFactory factory = new JsonReferenceFactory();

		JsonReference result = factory.doCreate("file://path/to/file/doc.yaml#/definitions/Foo", null);
		System.out.println(result.getUri());
		System.out.println(result.isLocal());
		System.out.println(result.isAbsolute());
		System.out.println(result.getPointer());
	}

	@Test
	public void test5() {
		JsonReferenceFactory factory = new JsonReferenceFactory();

		JsonReference result = factory.doCreate("file://path/to/file/doc.yaml#", null);
		System.out.println(result.getUri());
		System.out.println(result.isLocal());
		System.out.println(result.isAbsolute());
		System.out.println(result.getPointer());
	}

	@Test
	public void test6() {
		JsonReferenceFactory factory = new JsonReferenceFactory();

		JsonReference result = factory.doCreate("file://path/to/file/doc.yaml", null);
		System.out.println(result.getUri());
		System.out.println(result.isLocal());
		System.out.println(result.isAbsolute());
		System.out.println(result.getPointer());
	}

}
