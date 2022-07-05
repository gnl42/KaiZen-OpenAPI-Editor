package com.reprezen.swagedit.editor.hyperlinks

import com.fasterxml.jackson.databind.JsonNode
import com.reprezen.swagedit.editor.SwaggerDocument
import com.reprezen.swagedit.mocks.Mocks
import java.net.URI
import java.net.URISyntaxException
import java.util.Arrays
import org.eclipse.jface.text.BadLocationException
import org.eclipse.jface.text.Region
import org.junit.Before
import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.core.IsCollectionContaining.hasItem
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import com.reprezen.swagedit.core.hyperlinks.JsonReferenceHyperlinkDetector
import com.reprezen.swagedit.core.hyperlinks.SwaggerHyperlink

class JsonReferenceHyperlinkDetectorTest {

    var URI uri;

    protected def JsonReferenceHyperlinkDetector detector(JsonNode document) {
        Mocks.mockHyperlinkDetector(uri, document);
    }

    @Before
    def void setUp() throws URISyntaxException {
        uri = new URI(null, null, null)
    }

    @Test
    def void testShouldCreateHyperlink_ForJsonReference() throws BadLocationException {
        val document = new SwaggerDocument()
        val viewer = Mocks.mockTextViewer(document)

        val text = '''
        	schema:
        	  $ref: '#/definitions/User'
        	definitions:
        	  User:
        	    type: object
       '''

        document.set(text)       

        // region that includes `$ref: '#/definitions/User'`
        val region = new Region("schema:\n  $ref: '#/definitions".length(), 1)
        val hyperlinks = detector(document.asJson()).detectHyperlinks(viewer, region, false)

        assertNotNull(hyperlinks)

        // expected region
        val linkRegion = new Region(document.getLineOffset(1) + "  $ref: ".length(),
                "'#/definitions/User'".length())
        val targetRegion = new Region(50, 8)

        assertThat(Arrays.asList(hyperlinks), 
        	hasItem(new SwaggerHyperlink("/definitions/User", viewer, linkRegion,
                targetRegion)))
    }

	@Test
    def void testShouldCreateHyperlink_ForSimpleReference() throws BadLocationException {
        val document = new SwaggerDocument()
        val viewer = Mocks.mockTextViewer(document)

        val text = '''
        	schema:
        	  $ref: User
        	definitions:
        	  User:
        	    type: object
       '''

        document.set(text)       

        // region that includes `$ref: User`
        val region = new Region("schema:\n  $ref: U".length(), 1)
        val hyperlinks = detector(document.asJson()).detectHyperlinks(viewer, region, false)

        assertNotNull(hyperlinks)

        // expected region
        val linkRegion = new Region(document.getLineOffset(1) + "  $ref: ".length(), "User".length())
        val targetRegion = new Region(34, 8)

        assertThat(Arrays.asList(hyperlinks), 
        	hasItem(new SwaggerHyperlink("/definitions/User", viewer, linkRegion, targetRegion)))
    }

    @Test
    def void testShould_Not_CreateHyperlink_For_Invalid_JsonReference() throws BadLocationException {
        val document = new SwaggerDocument()
        val viewer = Mocks.mockTextViewer(document)

        val text = '''
        	schema:
        	  $ref: '#/definitions/Invalid'
        	definitions:  
        	  User:
        	    type: object"
       	'''

        document.set(text)

        // region that includes `$ref: '#/definitions/User'`
        val region = new Region("schema:\n  $ref: '#/definitions".length(), 1)
        val hyperlinks = detector(document.asJson()).detectHyperlinks(viewer, region, false)

        assertNull(hyperlinks)
    }

    @Test
    def void testShouldCreateHyperlink_ForPathReference() throws BadLocationException {
        val document = new SwaggerDocument()
        val viewer = Mocks.mockTextViewer(document)

        val text = '''
          schema:
            $ref: '#/paths/~1foo~1{bar}'
          paths:
            /foo/{bar}:
              get:
        '''

        document.set(text);

        // region that includes `$ref: '#/paths/~1foo~1{bar}'`
        val region = new Region("schema:\n  $ref: '#/paths/~1foo".length(), 1)
        val hyperlinks = detector(document.asJson()).detectHyperlinks(viewer, region, false)

        assertNotNull(hyperlinks)

        // expected region
        val linkRegion = new Region(document.getLineOffset(1) + "  $ref: ".length(),
                "'#/paths/~1foo~1{bar}'".length())
        val targetRegion = new Region(46, 14)

        assertThat(Arrays.asList(hyperlinks), hasItem(new SwaggerHyperlink("/paths/~1foo~1{bar}", viewer, linkRegion,
                targetRegion)))
    }

    @Test
    def void testShould_Not_CreateHyperlink_For_Invalid_PathReference() throws BadLocationException {
        val document = new SwaggerDocument()
        val viewer = Mocks.mockTextViewer(document)

        val text = '''
          schema:
            $ref: '#/paths/~1foo'
          paths:
            /foo/{bar}:
              get:
        '''

        document.set(text);

        // region that includes `$ref: '#/paths/~1foo~1{bar}'`
        val region = new Region("schema:\n  $ref: '#/paths/~1foo".length(), 1)
        val hyperlinks = detector(document.asJson()).detectHyperlinks(viewer, region, false)

        assertNull(hyperlinks)
    }

}