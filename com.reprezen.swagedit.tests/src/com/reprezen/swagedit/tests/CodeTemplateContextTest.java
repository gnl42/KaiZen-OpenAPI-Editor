package com.reprezen.swagedit.tests;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static com.reprezen.swagedit.templates.SwaggerContextType.getContentType;

import org.junit.Test;

import com.reprezen.swagedit.templates.SwaggerContextType.PathItemContextType;
import com.reprezen.swagedit.templates.SwaggerContextType.SecurityDefContextType;;

public class CodeTemplateContextTest {

	@Test
	public void testPathItem() throws Exception {
		assertThat(getContentType(":paths:/petstore"), equalTo(PathItemContextType.PATH_ITEM_CONTENT_TYPE));
		assertThat(getContentType(":paths:/pet-store"), equalTo(PathItemContextType.PATH_ITEM_CONTENT_TYPE));
		assertThat(getContentType(":paths:/pets/{id}"), equalTo(PathItemContextType.PATH_ITEM_CONTENT_TYPE));
		assertThat(getContentType(":paths:/pets/{pet-id}"), equalTo(PathItemContextType.PATH_ITEM_CONTENT_TYPE));
		assertThat(getContentType(":paths:/my-pets/{pet-id}"), equalTo(PathItemContextType.PATH_ITEM_CONTENT_TYPE));
		assertThat(getContentType(":paths:/my-pets/v1/{pet-id}"), equalTo(PathItemContextType.PATH_ITEM_CONTENT_TYPE));
		assertThat(getContentType(":paths:/pets"), equalTo(PathItemContextType.PATH_ITEM_CONTENT_TYPE));

		assertThat(getContentType(":paths:"), not(equalTo(PathItemContextType.PATH_ITEM_CONTENT_TYPE)));
		assertThat(getContentType(":paths:/pets:get"), not(equalTo(PathItemContextType.PATH_ITEM_CONTENT_TYPE)));
	}

	@Test
	public void testSecurityDef() throws Exception {
		assertThat(getContentType(":securityDefinitions"), equalTo(SecurityDefContextType.SECURITY_DEF_CONTENT_TYPE));
	}

}
