package com.reprezen.swagedit.core.validation;

import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.parser.ParserException;

import com.fasterxml.jackson.dataformat.yaml.JacksonYAMLParseException;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.error.MarkedYAMLException;

public class YamlErrorProcessor {

    public String rewriteMessage(Exception exception) {
        if (exception instanceof JacksonYAMLParseException) {
            return rewriteMessage((JacksonYAMLParseException) exception);
        }
        if (exception instanceof YAMLException) {
            return rewriteMessage((YAMLException) exception);
        }
        return exception != null ? exception.getLocalizedMessage() : null;
    }

    public String rewriteMessage(JacksonYAMLParseException exception) {
        if (exception instanceof MarkedYAMLException) {
            if ("while parsing a block mapping".equals(((MarkedYAMLException) exception).getContext())) {
                return Messages.error_yaml_parser_indentation;
            }
        }
        return exception != null ? exception.getLocalizedMessage() : null;
    }

    public String rewriteMessage(YAMLException exception) {
        if (exception instanceof ParserException) {
            if ("while parsing a block mapping".equals(((ParserException) exception).getContext())) {
                return Messages.error_yaml_parser_indentation;
            }
        }
        return exception != null ? exception.getLocalizedMessage() : null;
    }

}
