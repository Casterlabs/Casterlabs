package co.casterlabs.caffeinated.util;

import java.util.stream.Collectors;

public class HtmlEscape {

    public static String escapeHtml(String str) {
        return str
            .codePoints()
            .mapToObj(c -> c > 127 || "\"'<>&".indexOf(c) != -1 ? "&#" + c + ";" : new String(Character.toChars(c)))
            .collect(Collectors.joining());
    }

}
