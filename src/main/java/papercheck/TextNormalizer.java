package papercheck;

/**
 * Provides two comparison-friendly text forms: one that keeps layout
 * characters for ordered LCS matching, and one that keeps only letters and
 * digits for unordered shingle matching.
 */
final class TextNormalizer {

    private static final int FULL_WIDTH_ASCII_START = 0xFF01;
    private static final int FULL_WIDTH_ASCII_END = 0xFF5E;
    private static final int FULL_WIDTH_TO_ASCII_OFFSET = 0xFEE0;

    private TextNormalizer() {
    }

    static String normalize(CharSequence source) {
        return transform(source, false);
    }

    static String normalizeKeepingLayout(CharSequence source) {
        return transform(source, true);
    }

    private static String transform(CharSequence source, boolean keepLayoutCharacters) {
        String text = source.toString();
        StringBuilder builder = new StringBuilder(text.length());

        for (int offset = 0; offset < text.length(); ) {
            int codePoint = text.codePointAt(offset);
            offset += Character.charCount(codePoint);

            if (codePoint >= FULL_WIDTH_ASCII_START && codePoint <= FULL_WIDTH_ASCII_END) {
                codePoint -= FULL_WIDTH_TO_ASCII_OFFSET;
            }

            int lowerCodePoint = Character.toLowerCase(codePoint);
            boolean isSemanticCharacter = Character.isLetterOrDigit(lowerCodePoint);
            if (isSemanticCharacter || keepLayoutCharacters) {
                builder.appendCodePoint(lowerCodePoint);
            }
        }

        return builder.toString();
    }
}
