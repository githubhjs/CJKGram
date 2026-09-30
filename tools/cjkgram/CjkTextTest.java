import org.telegram.messenger.CjkText;

public final class CjkTextTest {
    private static int count;
    private static void check(String text, String query, boolean expected) {
        boolean actual = CjkText.matches(text, CjkText.normalize(query));
        if (actual != expected) throw new AssertionError("Unexpected match for " + query);
        count++;
    }
    public static void main(String[] args) {
        check("季後賽到了就推出 #絕境山本", "山本", true);
        check("山本", "山", true);
        check("阿蘭神歐", "豆拉", false);
        check("カタカナ", "ｶﾀｶﾅ", true);
        check("プロ野球スピリッツ", "野球", true);
        check("한국어 검색", "검색", true);
        check("한", "한", true);
        check("ＡＢＣ１２３", "abc123", true);
        check("100%_literal", "%_", true);
        check("ordinary text", "%", false);
        check("𠮷野家", "𠮷野", true);
        check("text", "", false);
        check(null, "山本", false);
        // Traditional/Simplified equivalence is intentionally not implicit.
        check("臺灣", "台湾", false);
        System.out.println("Passed " + count + " CJK substring cases");
    }
}
