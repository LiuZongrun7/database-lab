package edu.ucd.comp2013j.lab.web;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Json {
    private Json() {
    }

    public static String object(Map<String, ?> values) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            if (!first) {
                json.append(',');
            }
            json.append(quote(entry.getKey())).append(':').append(value(entry.getValue()));
            first = false;
        }
        return json.append('}').toString();
    }

    public static String array(Iterable<? extends Map<String, ?>> rows) {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (Map<String, ?> row : rows) {
            if (!first) {
                json.append(',');
            }
            json.append(object(row));
            first = false;
        }
        return json.append(']').toString();
    }

    public static Map<String, Object> row(Object... pairs) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            row.put(String.valueOf(pairs[i]), pairs[i + 1]);
        }
        return row;
    }

    public static String ok(String message) {
        return object(row("ok", true, "message", message));
    }

    public static String error(String message) {
        return object(row("ok", false, "message", message));
    }

    private static String value(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return quote(String.valueOf(value));
    }

    private static String quote(String text) {
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> out.append(c);
            }
        }
        return out.append('"').toString();
    }
}
