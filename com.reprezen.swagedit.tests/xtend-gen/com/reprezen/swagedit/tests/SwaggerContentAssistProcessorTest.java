package com.reprezen.swagedit.tests;

import com.reprezen.swagedit.assist.SwaggerCompletionProposal;
import com.reprezen.swagedit.assist.SwaggerContentAssistProcessor;
import com.reprezen.swagedit.assist.SwaggerProposal;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.validation.SwaggerSchema;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.AbstractCharSequenceAssert;
import org.assertj.core.api.AbstractObjectArrayAssert;
import org.assertj.core.api.Assertions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;

@SuppressWarnings("all")
public class SwaggerContentAssistProcessorTest {
  private final SwaggerSchema schema = new SwaggerSchema();
  
  private IDocument document;
  
  private IContentAssistProcessor processor;
  
  private ITextViewer viewer;
  
  @Before
  public void setUp() {
    SwaggerDocument _swaggerDocument = new SwaggerDocument();
    this.document = _swaggerDocument;
    SwaggerContentAssistProcessor _swaggerContentAssistProcessor = new SwaggerContentAssistProcessor();
    this.processor = _swaggerContentAssistProcessor;
    ITextViewer _mock = Mockito.<ITextViewer>mock(ITextViewer.class);
    this.viewer = _mock;
  }
  
  @Test
  public void shouldProvideAllKeywordsWhenDocIsEmpty() throws BadLocationException {
    StringConcatenation _builder = new StringConcatenation();
    final String yaml = _builder.toString();
    final int offset = 0;
    IDocument _document = this.viewer.getDocument();
    OngoingStubbing<IDocument> _when = Mockito.<IDocument>when(_document);
    _when.thenReturn(this.document);
    Point _selectedRange = this.viewer.getSelectedRange();
    OngoingStubbing<Point> _when_1 = Mockito.<Point>when(_selectedRange);
    Point _point = new Point(0, 0);
    _when_1.thenReturn(_point);
    this.document.set(yaml);
    final ICompletionProposal[] proposals = this.processor.computeCompletionProposals(this.viewer, offset);
    AbstractObjectArrayAssert<?, ICompletionProposal> _assertThat = Assertions.<ICompletionProposal>assertThat(proposals);
    Set<String> _keywords = this.schema.getKeywords();
    int _length = ((Object[])Conversions.unwrapArray(_keywords, Object.class)).length;
    _assertThat.hasSize(_length);
  }
  
  @Test
  public void shouldProvideEndOfWord() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("swa");
    final String yaml = _builder.toString();
    final int offset = 3;
    IDocument _document = this.viewer.getDocument();
    OngoingStubbing<IDocument> _when = Mockito.<IDocument>when(_document);
    _when.thenReturn(this.document);
    Point _selectedRange = this.viewer.getSelectedRange();
    OngoingStubbing<Point> _when_1 = Mockito.<Point>when(_selectedRange);
    Point _point = new Point(0, 0);
    _when_1.thenReturn(_point);
    this.document.set(yaml);
    final ICompletionProposal[] proposals = this.processor.computeCompletionProposals(this.viewer, offset);
    AbstractObjectArrayAssert<?, ICompletionProposal> _assertThat = Assertions.<ICompletionProposal>assertThat(proposals);
    _assertThat.hasSize(1);
    final ICompletionProposal proposal = proposals[0];
    proposal.apply(this.document);
    String _get = this.document.get();
    AbstractCharSequenceAssert<?, String> _assertThat_1 = Assertions.assertThat(_get);
    _assertThat_1.isEqualTo("swagger");
  }
  
  @Test
  public void test() {
    SwaggerCompletionProposal _swaggerCompletionProposal = new SwaggerCompletionProposal();
    SwaggerProposal.ObjectProposal _get = _swaggerCompletionProposal.get();
    final SwaggerProposal.ObjectProposal proposal = ((SwaggerProposal.ObjectProposal) _get);
    Map<String, SwaggerProposal> _properties = proposal.getProperties();
    final SwaggerProposal value = _properties.get("swagger");
    InputOutput.<SwaggerProposal>println(value);
    Assert.assertNotNull(value);
  }
}
