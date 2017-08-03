package com.reprezen.swagedit.core.validation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.parser.ParserException;

//import com.fasterxml.jackson.dataformat.yaml.JacksonYAMLParseException;
//import com.fasterxml.jackson.dataformat.yaml.snakeyaml.error.MarkedYAMLException;

public class YamlErrorProcessor {

    public String rewriteMessage(Exception exception) {
        // JacksonYAMLParseException is not visible in OSGi, see
        // https://github.com/fasterxml/jackson-dataformat-yaml/issues/31
        // TODO remove it when we (Orbit) switch to a newer version of Jackson where this problem is fixed
        // if (exception instanceof JacksonYAMLParseException) {
        // return rewriteMessage((JacksonYAMLParseException) exception);
        // }
        if (exception instanceof YAMLException) {
            return rewriteMessage((YAMLException) exception);
        }
        // TODO remove hack when the fix for JacksonYAMLParseException is available, see comment above
        String context = hack_getContext(exception);
        if ("while parsing a block mapping".equals(context)) {
            return Messages.error_yaml_parser_indentation;
        }
        return exception != null ? exception.getLocalizedMessage() : null;
    }

    private String hack_getContext(Exception e) {
        try {
            Method method = e.getClass().getMethod("getContext", null);
            Object returnValue = method.invoke(e, null);
            if (returnValue instanceof String) {
                return (String) returnValue;
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e1) {
            return null;
        }
        return null;
    }

    // public String rewriteMessage(JacksonYAMLParseException exception) {
    // if (exception instanceof MarkedYAMLException) {
    // if ("while parsing a block mapping".equals(((MarkedYAMLException) exception).getContext())) {
    // return Messages.error_yaml_parser_indentation;
    // }
    // }
    // return exception != null ? exception.getLocalizedMessage() : null;
    // }

    public String rewriteMessage(YAMLException exception) {
        if (exception instanceof ParserException) {
            if ("while parsing a block mapping".equals(((ParserException) exception).getContext())) {
                return Messages.error_yaml_parser_indentation;
            }
        }
        return exception != null ? exception.getLocalizedMessage() : null;
    }

}
