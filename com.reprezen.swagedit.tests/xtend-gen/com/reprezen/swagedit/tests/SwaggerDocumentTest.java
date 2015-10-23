package com.reprezen.swagedit.tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reprezen.swagedit.editor.SwaggerDocument;
import io.swagger.util.Yaml;
import java.util.List;
import org.assertj.core.api.AbstractCharSequenceAssert;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.Assertions;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.junit.Test;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.ScalarEvent;

@SuppressWarnings("all")
public class SwaggerDocumentTest {
  private final SwaggerDocument document = new SwaggerDocument();
  
  @Test
  public void testGetCorrectListOfEvents() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("key: \'value\'");
    _builder.newLine();
    final String yaml = _builder.toString();
    this.document.set(yaml);
    final List<Event> events = this.document.getEvent(0);
    AbstractListAssert<?, ? extends List<? extends Event>, Event> _assertThat = Assertions.<Event>assertThat(events);
    _assertThat.hasSize(2);
    Event _get = events.get(0);
    final ScalarEvent e1 = ((ScalarEvent) _get);
    Event _get_1 = events.get(1);
    final ScalarEvent e2 = ((ScalarEvent) _get_1);
    String _value = e1.getValue();
    AbstractCharSequenceAssert<?, String> _assertThat_1 = Assertions.assertThat(_value);
    _assertThat_1.isEqualTo("key");
    String _value_1 = e2.getValue();
    AbstractCharSequenceAssert<?, String> _assertThat_2 = Assertions.assertThat(_value_1);
    _assertThat_2.isEqualTo("value");
  }
  
  @Test
  public void test() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("info:");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("description: \"Tax Blaster\"");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("version: \"1.0.0\"");
    _builder.newLine();
    final String yaml = _builder.toString();
    this.document.set(yaml);
    final List<Event> events = this.document.getEvent(1);
    AbstractListAssert<?, ? extends List<? extends Event>, Event> _assertThat = Assertions.<Event>assertThat(events);
    _assertThat.hasSize(2);
    Event _get = events.get(0);
    final ScalarEvent e1 = ((ScalarEvent) _get);
    Event _get_1 = events.get(1);
    final ScalarEvent e2 = ((ScalarEvent) _get_1);
    String _value = e1.getValue();
    AbstractCharSequenceAssert<?, String> _assertThat_1 = Assertions.assertThat(_value);
    _assertThat_1.isEqualTo("description");
    String _value_1 = e2.getValue();
    AbstractCharSequenceAssert<?, String> _assertThat_2 = Assertions.assertThat(_value_1);
    _assertThat_2.isEqualTo("Tax Blaster");
  }
  
  @Test
  public void testGetRootMapping() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("info:");
      _builder.newLine();
      _builder.append("  ");
      _builder.append("description: \"\"");
      _builder.newLine();
      _builder.append("tags:");
      _builder.newLine();
      _builder.append("  ");
      _builder.append("- foo: \"\"");
      _builder.newLine();
      _builder.append("  ");
      _builder.append("- bar: \"\"");
      _builder.newLine();
      final String yaml = _builder.toString();
      this.document.set(yaml);
      final List<String> path = this.document.getPath(1);
      InputOutput.<List<String>>println(path);
      List<String> _path = this.document.getPath(3);
      InputOutput.<List<String>>println(_path);
      ObjectMapper _mapper = Yaml.mapper();
      final JsonNode node = _mapper.readTree(yaml);
      InputOutput.<JsonNode>println(node);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
