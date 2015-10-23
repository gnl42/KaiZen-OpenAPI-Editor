package com.reprezen.swagedit.tests;

import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.validation.SwaggerError;
import com.reprezen.swagedit.validation.Validator;
import java.io.IOException;
import java.util.List;
import org.eclipse.core.resources.IMarker;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class ValidatorTest {
  private final Validator validator = new Validator();
  
  private final SwaggerDocument document = new SwaggerDocument();
  
  @Test
  public void shouldNotReturnErrorsIfDocumentIsValid() throws IOException {
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
    this.document.set(content);
    final List<SwaggerError> errors = this.validator.validate(this.document);
    int _size = errors.size();
    Assert.assertEquals(0, _size);
  }
  
  @Test
  public void shouldReturnSingleErrorIfMissingRootProperty() throws IOException {
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
    final String content = _builder.toString();
    this.document.set(content);
    final List<SwaggerError> errors = this.validator.validate(this.document);
    int _size = errors.size();
    Assert.assertEquals(1, _size);
    final SwaggerError error = errors.get(0);
    int _level = error.getLevel();
    Assert.assertEquals(IMarker.SEVERITY_ERROR, _level);
    int _line = error.getLine();
    Assert.assertEquals(1, _line);
  }
  
  @Test
  public void shouldReturnSingleErrorIfTypeOfPropertyIsIncorrect() throws IOException {
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
    _builder.append("paths: \'Hello\'");
    _builder.newLine();
    final String content = _builder.toString();
    this.document.set(content);
    final List<SwaggerError> errors = this.validator.validate(this.document);
    int _size = errors.size();
    Assert.assertEquals(1, _size);
    final SwaggerError error = errors.get(0);
    int _level = error.getLevel();
    Assert.assertEquals(IMarker.SEVERITY_ERROR, _level);
    int _line = error.getLine();
    Assert.assertEquals(5, _line);
  }
  
  @Test
  public void shouldReturnSingleErrorIfTypeOfDeepPropertyIsIncorrect() throws IOException {
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
    _builder.append("paths:");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("/:");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("get:");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("responses: \'Hello\'");
    _builder.newLine();
    final String content = _builder.toString();
    this.document.set(content);
    final List<SwaggerError> errors = this.validator.validate(this.document);
    int _size = errors.size();
    Assert.assertEquals(1, _size);
    final SwaggerError error = errors.get(0);
    int _level = error.getLevel();
    Assert.assertEquals(IMarker.SEVERITY_ERROR, _level);
    int _line = error.getLine();
    Assert.assertEquals(8, _line);
  }
  
  @Test
  public void shouldReturnSingleErrorIfInvalidResponseCode() throws IOException {
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
    _builder.append("\'0\':");
    _builder.newLine();
    _builder.append("          ");
    _builder.append("description: OK");
    _builder.newLine();
    final String content = _builder.toString();
    this.document.set(content);
    final List<SwaggerError> errors = this.validator.validate(this.document);
    int _size = errors.size();
    Assert.assertEquals(1, _size);
    final SwaggerError error = errors.get(0);
    int _level = error.getLevel();
    Assert.assertEquals(IMarker.SEVERITY_ERROR, _level);
    int _line = error.getLine();
    Assert.assertEquals(9, _line);
  }
  
  @Test
  public void shouldReturnErrorForInvalidScheme() throws IOException {
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
    this.document.set(content);
    final List<SwaggerError> errors = this.validator.validate(this.document);
    int _size = errors.size();
    Assert.assertEquals(1, _size);
    final SwaggerError error = errors.get(0);
    int _level = error.getLevel();
    Assert.assertEquals(IMarker.SEVERITY_ERROR, _level);
    int _line = error.getLine();
    Assert.assertEquals(5, _line);
  }
}
