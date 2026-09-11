package papercheck;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Computes a repeat rate from ordered LCS similarity and unordered
 * character n-gram similarity.
 *
 * <p>LCS catches local additions, deletions and synonym changes; character
 * shingles catch paragraph reordering. Both measures are language-neutral and
 * do not need an external Chinese tokenizer.</p>
 */
public final class PaperSimilarityScorer {

    private static final int HASH_BASE = 31;
    private static final int[] SHINGLE_SIZES = {3, 4, 5, 6};
    private static final double[] SHINGLE_WEIGHTS = {0.20, 0.30, 0.30, 0.20};
    private static final long MAX_LCS_CELLS = 25_000_000L;

    public double score(CharSequence originalText, CharSequence copiedText) {
        int[] original = TextNormalizer.normalize(originalText).codePoints().toArray();
        int[] copied = TextNormalizer.normalize(copiedText).codePoints().toArray();

        if (original.length == 0 || copied.length == 0) {
            return 0.0;
        }

        int[] originalWithLayout = TextNormalizer.normalizeKeepingLayout(originalText).codePoints().toArray();
        int[] copiedWithLayout = TextNormalizer.normalizeKeepingLayout(copiedText).codePoints().toArray();
        double orderedScore = orderedLcsScore(originalWithLayout, copiedWithLayout);
        double unorderedScore = unorderedShingleScore(original, copied);
        return Math.max(orderedScore, unorderedScore);
    }

    private double orderedLcsScore(int[] original, int[] copied) {
        long cellCount = (long) original.length * copied.length;
        if (cellCount > MAX_LCS_CELLS || Arrays.equals(original, copied)) {
            return Arrays.equals(original, copied) ? 1.0 : 0.0;
        }

        int shorterLength = Math.min(original.length, copied.length);
        int[] previousRow = new int[shorterLength + 1];
        int[] currentRow = new int[shorterLength + 1];

        int[] longer = original.length >= copied.length ? original : copied;
        int[] shorter = longer == original ? copied : original;

        for (int longerIndex = 1; longerIndex <= longer.length; longerIndex++) {
            currentRow[0] = 0;
            for (int shorterIndex = 1; shorterIndex <= shorter.length; shorterIndex++) {
                if (longer[longerIndex - 1] == shorter[shorterIndex - 1]) {
                    currentRow[shorterIndex] = previousRow[shorterIndex - 1] + 1;
                } else {
                    currentRow[shorterIndex] = Math.max(
                            previousRow[shorterIndex],
                            currentRow[shorterIndex - 1]
                    );
                }
            }
            int[] swap = previousRow;
            previousRow = currentRow;
            currentRow = swap;
        }

        int lcsLength = previousRow[shorterLength];
        return 2.0 * lcsLength / (original.length + copied.length);
    }

    private double unorderedShingleScore(int[] original, int[] copied) {
        int minLength = Math.min(original.length, copied.length);
        double weightedScore = 0.0;
        double weightSum = 0.0;

        for (int i = 0; i < SHINGLE_SIZES.length; i++) {
            int size = SHINGLE_SIZES[i];
            if (minLength < size) {
                continue;
            }

            double dice = diceCoefficient(shingleSet(original, size), shingleSet(copied, size));
            weightedScore += SHINGLE_WEIGHTS[i] * dice;
            weightSum += SHINGLE_WEIGHTS[i];
        }

        if (weightSum == 0.0) {
            return 0.0;
        }
        return weightedScore / weightSum;
    }

    private double diceCoefficient(Set<Long> originalShingles, Set<Long> copiedShingles) {
        Set<Long> smaller = originalShingles.size() <= copiedShingles.size()
                ? originalShingles : copiedShingles;
        Set<Long> larger = smaller == originalShingles ? copiedShingles : originalShingles;

        int intersectionSize = 0;
        for (Long shingle : smaller) {
            if (larger.contains(shingle)) {
                intersectionSize++;
            }
        }

        int totalSize = originalShingles.size() + copiedShingles.size();
        if (totalSize == 0) {
            return 0.0;
        }
        return 2.0 * intersectionSize / totalSize;
    }

    private Set<Long> shingleSet(int[] codePoints, int size) {
        int count = codePoints.length - size + 1;
        Set<Long> shingles = new HashSet<>(Math.max(16, count * 4 / 3 + 1));

        long rollingHash = 0;
        long removalFactor = 1;
        for (int i = 0; i < size; i++) {
            rollingHash = rollingHash * HASH_BASE + codePoints[i];
            removalFactor *= HASH_BASE;
        }
        shingles.add(rollingHash);

        for (int i = size; i < codePoints.length; i++) {
            rollingHash = rollingHash * HASH_BASE + codePoints[i];
            rollingHash -= removalFactor * codePoints[i - size];
            shingles.add(rollingHash);
        }

        return shingles;
    }
}
