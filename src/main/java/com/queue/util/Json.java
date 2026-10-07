package com.queue.util;

import java.util.Map;

public final class Json {
    private Json() {}
    public static String encode(Object value) {
        if (value == null) return "null";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof Map<?, ?> map) {
            StringBuilder out = new StringBuilder("{");
            for (var entry : map.entrySet()) { if (out.length() > 1) out.append(','); out.append(encode(entry.getKey().toString())).append(':').append(encode(entry.getValue())); }
            return out.append('}').toString();
        }
        if (value instanceof Iterable<?> list) {
            StringBuilder out = new StringBuilder("[");
            for (var item : list) { if (out.length() > 1) out.append(','); out.append(encode(item)); }
            return out.append(']').toString();
        }
        StringBuilder out = new StringBuilder("\"");
        for (char c : value.toString().toCharArray()) {
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default: if (c < 32) out.append(String.format("\\u%04x", (int)c)); else out.append(c);
            }
        }
        return out.append('"').toString();
    }
}
