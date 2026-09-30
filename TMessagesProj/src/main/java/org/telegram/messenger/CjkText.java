package org.telegram.messenger;

import java.text.Normalizer;
import java.util.Locale;

/** Literal substring matching, independent of Telegram's server tokenizer. */
public final class CjkText {
    private CjkText() { }

    public static String normalize(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);
    }

    public static boolean matches(String text, String normalizedQuery) {
        return !normalizedQuery.isEmpty() && normalize(text).contains(normalizedQuery);
    }
}
