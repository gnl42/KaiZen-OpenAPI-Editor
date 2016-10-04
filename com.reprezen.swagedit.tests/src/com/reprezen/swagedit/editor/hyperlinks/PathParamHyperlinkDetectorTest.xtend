package com.reprezen.swagedit.editor.hyperlinks

import com.reprezen.swagedit.editor.SwaggerDocument
import com.reprezen.swagedit.mocks.Mocks
import java.util.Arrays
import java.util.HashSet
import org.eclipse.jface.text.Region
import org.junit.Test

import static org.hamcrest.core.IsCollectionContaining.hasItem
import static org.hamcrest.core.IsCollectionContaining.hasItems
import static org.junit.Assert.assertThat

class PathParamHyperlinkDetectorTest {

	val detector = new PathParamHyperlinkDetector

	@Test
    def void testShouldCreateHyperLink_FromPathParameter_ToParameterDefinition() throws Exception {
        val document = new SwaggerDocument()
        val viewer = Mocks.mockTextViewer(document)

        val text = '''
          paths:
            /{id}:
              get:
                parameters:
                  - name: id
                    type: number
                    required: true
                    in: path
		'''

        document.set(text);
        // region that includes `/{id}:`
        val region = new Region(11, 1)
        val hyperlinks = detector.detectHyperlinks(viewer, region, false)

        // expected region
        val linkRegion = new Region(document.getLineOffset(1) + "  /".length(), "{id}".length())
        val targetRegion = new Region(43, 86)

        assertThat(Arrays.asList(hyperlinks), hasItem(new SwaggerHyperlink("id", viewer, linkRegion, targetRegion)));
    }

    @Test
    def void test_match_paramter() {
        val text = "/path/{id}"

        val matcher = PathParamHyperlinkDetector.PARAMETER_PATTERN.matcher(text)
        val groups = new HashSet()
        while (matcher.find()) {
            groups.add(matcher.group(1))
        }

        assertThat(groups, hasItems("id"))
    }

    @Test
    def void test_match_second_paramter() {
        val text = "/path/{id}/other/{foo}"

        val matcher = PathParamHyperlinkDetector.PARAMETER_PATTERN.matcher(text)
        val groups = new HashSet()
        while (matcher.find()) {
            groups.add(matcher.group(1))
        }

        assertThat(groups, hasItems("id", "foo"))
    }

}