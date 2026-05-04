package j.core.security;

import j.util.JUtilString;

public final class CharsInspector {
    public static final String[] CHARS_HTML = new String[]{"<", ">", "&gt;", "&lt;"};
    public static final String[] CHARS_QUOT = new String[]{"'", "\"", "‘", "“"};
    public static final String[] CHARS_PUNCTUATION = new String[]{",", ";", ".", "，", "；", "。"};
    public static final String[] CHARS_ESCAPE = new String[]{"\r", "\n", "\b", "\t", "\f"};

    /**
     * 是否存在HTML代码常用字符
     * @param s
     * @return
     */
    public static boolean codingCharsExits(String s){
        return JUtilString.existsIgnoreCase(s, CHARS_HTML);
    }

    /**
     * 是否存在单、双引号
     * @param s
     * @return
     */
    public static boolean quotCharsExits(String s){
        return JUtilString.existsIgnoreCase(s, CHARS_QUOT);
    }

    /**
     * 是否存在常用标点符号
     * @param s
     * @return
     */
    public static boolean punctuationCharsExits(String s){
        return JUtilString.existsIgnoreCase(s, CHARS_PUNCTUATION);
    }

    /**
     * 是否存在转义符
     * @param s
     * @return
     */
    public static boolean escapeCharsExits(String s){
        return JUtilString.existsIgnoreCase(s, CHARS_ESCAPE);
    }
}
