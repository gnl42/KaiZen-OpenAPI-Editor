package com.reprezen.swagedit.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StringUtils {

    public static enum QuoteStyle {
        SINGLE("'"), //
        DOUBLE("\""), //
        INVALID(null);

        private final String value;

        QuoteStyle(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
        
        public boolean isValid() {
            return this != INVALID;
        }

        public static QuoteStyle parse(char c) {
            if (c == '"') {
                return DOUBLE;
            } else if (c == '\'') {
                return SINGLE;
            } else {
                return INVALID;
            }
        }
    }

    public static boolean isQuoted(String string) {
        return emptyToNull(string) != null && (string.charAt(0) == '"' || string.charAt(0) == '\'');
    }
    
    public static QuoteStyle tryGetQuotes(String string) {
        return isQuoted(string) ? QuoteStyle.parse(string.charAt(0)): QuoteStyle.INVALID;
    }
    
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }
    
    public static String emptyToNull(String string) {
        return (string == null || string.isEmpty()) ? null : string;
    }

    public static String nullToEmpty(String string) {
        return string == null ? "" : string;
    }
    
    public static String toString(InputStream contents) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = contents.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

}
