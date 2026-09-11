package papercheck;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PaperSimilarityScorerTest {

    private final PaperSimilarityScorer scorer = new PaperSimilarityScorer();

    @Test
    public void identicalChineseDocumentHasFullScore() {
        String text = "今天是星期天，天气晴，今天晚上我要去看电影。";
        assertEquals(1.0, scorer.score(text, text), 1.0e-9);
    }

    @Test
    public void identicalEnglishDocumentIgnoresCasePunctuationAndSpaces() {
        String original = "Hello, World! This is a Java project.";
        String copied = "hello world this is a JAVA project";
        assertEquals(1.0, scorer.score(original, copied), 1.0e-9);
    }

    @Test
    public void unrelatedChineseDocumentsHaveLowScore() {
        String original = "人工智能正在改变教育领域。";
        String copied = "苹果香蕉牛奶为早餐提供能量。";
        double score = scorer.score(original, copied);
        assertTrue("unrelated text score should be low: " + score, score < 0.30);
    }

    @Test
    public void synonymChangesStillKeepMostContent() {
        String original = "今天是星期天，天气晴，今天晚上我要去看电影。";
        String copied = "今天是周天，天气晴朗，我晚上要去看电影。";
        double score = scorer.score(original, copied);
        assertTrue("modified sample should keep a high score: " + score, score > 0.55);
        assertEquals(0.8095238095, score, 0.001);
    }

    @Test
    public void partialDeletionKeepsStrongSimilarity() {
        String original = "第一段介绍论文背景，第二段给出算法，第三段说明实验，第四段总结。";
        String copied = "第一段介绍论文背景，第三段说明实验，第四段总结。";
        double score = scorer.score(original, copied);
        assertTrue("deletion should still be similar: " + score, score > 0.60);
    }

    @Test
    public void appendedContentKeepsHighScoreButDoesNotReachOne() {
        String original = "查重算法需要能够处理中文长文本和英文论文。";
        String copied = original + "这是完全新加入的后续研究内容，用于测试增量内容对结果的影响。";
        double score = scorer.score(original, copied);
        assertTrue("added content should lower score: " + score, score < 1.0);
        assertTrue("most original shingles should remain: " + score, score > 0.55);
    }

    @Test
    public void paragraphReorderingRetainsMostShingles() {
        String original = "第一段详细描述论文背景和已有工作。第二段介绍核心算法与实现细节。"
                + "第三段展示实验结果并分析误差。第四段总结全文贡献和未来方向。";
        String copied = "第三段展示实验结果并分析误差。第一段详细描述论文背景和已有工作。"
                + "第四段总结全文贡献和未来方向。第二段介绍核心算法与实现细节。";
        double score = scorer.score(original, copied);
        assertTrue("paragraph reordering should keep high score: " + score, score > 0.65);
    }

    @Test
    public void emptyOriginalHasZeroRepeatRate() {
        assertEquals(0.0, scorer.score("", "非空抄袭文本"), 1.0e-9);
    }

    @Test
    public void emptyCopiedTextHasZeroRepeatRate() {
        assertEquals(0.0, scorer.score("非空原文", ""), 1.0e-9);
    }

    @Test
    public void returnedScoreAlwaysStaysInUnitInterval() {
        String[] originalTexts = {"太阳从东方升起。", "The quick brown fox.", "", "算法性能测试"};
        String[] copiedTexts = {"太阳从东方升起。", "The slow brown dog.", "任意新增文字", "完全无关的内容"};

        for (String original : originalTexts) {
            for (String copied : copiedTexts) {
                double score = scorer.score(original, copied);
                assertTrue("score out of range: " + score, score >= 0.0 && score <= 1.0);
            }
        }
    }

    @Test
    public void veryShortSameWordAndDifferentWordAreDistinguished() {
        assertEquals(1.0, scorer.score("word", "word"), 1.0e-9);
        assertEquals(0.0, scorer.score("word", "text"), 1.0e-9);
    }
}
