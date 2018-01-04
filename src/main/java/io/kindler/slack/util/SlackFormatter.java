package io.kindler.slack.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

public class SlackFormatter {
    public static final String BOLD = "*";
    public static final String ITALICS = "_";
    public static final String STRIKE = "~";
    public static final String CODE = "`";
    public static final String CODE_BLOCK = "```";

    public static String link(String value, String href) {
        return String.format("<%s|%s>", href, value);
    }

    public static String emptyTo(String value, String defaultString) {
        return StringUtils.defaultIfBlank(value, defaultString);
    }

    public static String bold(Collection<String> arrText) {
        return bold(arrayJoin(arrText));
    }

    public static String bold(String text) {
        return arroundConcat(text, BOLD);
    }

    public static String italics(Collection<String> arrText) {
        return italics(arrayJoin(arrText));
    }

    public static String italics(String text) {
        return arroundConcat(text, ITALICS);
    }

    public static String strike(Collection<String> arrText) {
        return strike(arrayJoin(arrText));
    }

    public static String strike(String text) {
        return arroundConcat(text, STRIKE);
    }

    public static String code(Collection<String> arrText) {
        return code(arrayJoin(arrText));
    }

    public static String code(String text) {
        return arroundConcat(text, CODE);
    }

    public static String codeBock(Collection<String> arrText) {
        return codeBock(arrayJoin(arrText));
    }

    public static String codeBock(String text) {
        return arroundConcat(text, CODE_BLOCK);
    }

    public static String arrayJoin(Collection<String> arrText) {
        return arrayJoin(arrText, ",");
    }

    public static String arrayJoin(Collection<String> arrText, String delimiter) {
        return String.join(delimiter, arrText);
    }

    public static String escape(String text) {
        text = text.replaceAll("&", "&amp;");
        text = text.replaceAll("<", "&gt;");
        text = text.replaceAll(">", "&lt;");
        return text;
    }

    private static String arroundConcat(String text, String append) {
        return append + text + append;
    }
}
