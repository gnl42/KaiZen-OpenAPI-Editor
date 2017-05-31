package com.reprezen.swagedit.core.utils;

import com.google.common.base.Strings;

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
        return Strings.emptyToNull(string) != null && (string.startsWith("\"") || string.startsWith("'"));
    }

}
