package edu.ucd.comp2013j.lab.web;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FormData {
    private final Map<String, String> values;

    private FormData(Map<String, String> values) {
        this.values = values;
    }

    public static FormData from(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return new FormData(parse(body));
    }

    public static FormData fromQuery(String query) {
        return new FormData(parse(query));
    }

    public String get(String key) {
        return values.getOrDefault(key, "");
    }

    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public Integer getOptionalInt(String key) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            return null;
        }
        return Integer.parseInt(value);
    }

    private static Map<String, String> parse(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.isBlank()) {
            return map;
        }
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            int equals = pair.indexOf('=');
            String key = equals >= 0 ? pair.substring(0, equals) : pair;
            String value = equals >= 0 ? pair.substring(equals + 1) : "";
            map.put(decode(key), decode(value));
        }
        return map;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
