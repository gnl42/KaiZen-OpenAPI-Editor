package com.reprezen.swagedit.tests;

import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.validation.SwaggerError;
import com.reprezen.swagedit.validation.Validator;
import java.util.List;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests as documentation for #9 - User-friendly validation messages
 * The " #validation error marker" are placed right above the place where we expect to see a validation error.
 * It's for human convenience only and will be ignored by the test.
 */
@SuppressWarnings("all")
public class ValidationMessageTest {
  private final Validator validator = new Validator();
  
  private final SwaggerDocument document = new SwaggerDocument();
  
  public void assertModelHasValidationError(final String expectedMessage, final String modelText) {
    this.document.set(modelText);
    final List<SwaggerError> errors = this.validator.validate(this.document);
    int _size = errors.size();
    Assert.assertEquals(1, _size);
    final SwaggerError error = errors.get(0);
    String _message = error.getMessage();
    Assert.assertEquals(expectedMessage, _message);
  }
  
  @Test
  public void testMessage_additionalItems_notAllowed() {
    String expected = "instance type (integer) does not match any allowed primitive type (allowed: [\"array\"])";
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("swagger: \'2.0\'");
    _builder.newLine();
    _builder.append("info:");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("version: 0.0.0");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("title: MyModel");
    _builder.newLine();
    _builder.append("paths:");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("/p:");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("get:");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("#validation error marker");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("parameters: 2        ");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("responses:");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("\'200\':");
    _builder.newLine();
    _builder.append("          ");
    _builder.append("description: OK");
    _builder.newLine();
    final String content = _builder.toString();
    this.assertModelHasValidationError(expected, content);
  }
  
  @Test
  public void testMessage_typeNoMatch() {
    String expected = "instance type (integer) does not match any allowed primitive type (allowed: [\"object\"])";
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("swagger: \'2.0\'");
    _builder.newLine();
    _builder.append("info:");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("version: 0.0.0");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("title: MyModel");
    _builder.newLine();
    _builder.append("paths:");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("/p:");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("get:     ");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("#validation error marker");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("responses: 2");
    _builder.newLine();
    final String content = _builder.toString();
    this.assertModelHasValidationError(expected, content);
  }
  
  @Test
  public void testMessage_notInEnum() {
    final String expected = "instance value (\"foo\") not found in enum (possible values: [\"http\",\"https\",\"ws\",\"wss\"])";
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("swagger: \'2.0\'");
    _builder.newLine();
    _builder.append("info:");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("version: 0.0.0");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("title: Simple API");
    _builder.newLine();
    _builder.append("#validation error marker");
    _builder.newLine();
    _builder.append("schemes:");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("- http");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("- foo");
    _builder.newLine();
    _builder.append("paths:");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("/:");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("get:");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("responses:");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("\'200\':");
    _builder.newLine();
    _builder.append("          ");
    _builder.append("description: OK");
    _builder.newLine();
    final String content = _builder.toString();
    this.assertModelHasValidationError(expected, content);
  }
  
  @Test
  public void testMessage_oneOf_fail() {
    final String expected = "instance failed to match exactly one schema (matched 0 out of 2)";
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("swagger: \'2.0\'");
    _builder.newLine();
    _builder.append("info:");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("version: 0.0.0");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("title: MyModel");
    _builder.newLine();
    _builder.append("paths:");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("/p:");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("get:");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("responses:");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("\'200\':");
    _builder.newLine();
    _builder.append("          ");
    _builder.append("#validation error marker");
    _builder.newLine();
    _builder.append("          ");
    _builder.append("description: 200");
    _builder.newLine();
    final String content = _builder.toString();
    this.assertModelHasValidationError(expected, content);
  }
  
  @Test
  public void testMessage_additionalProperties_notAllowed() {
    final String expected = "object instance has properties which are not allowed by the schema: [\"description\"]";
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("swagger: \'2.0\'");
    _builder.newLine();
    _builder.append("info:");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("version: 0.0.0");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("title: MyModel");
    _builder.newLine();
    _builder.append("paths:");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("/p:");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("get:");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("responses:");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("#validation error marker");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("\'200\':");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("description: OK");
    _builder.newLine();
    final String content = _builder.toString();
    this.assertModelHasValidationError(expected, content);
  }
}
