//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package tools;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class 处理字符串 {
    public 处理字符串() {
    }

    public static boolean isBlank(String input) {
        if (input != null && !"".equals(input)) {
            for(int i = 0; i < input.length(); ++i) {
                char c = input.charAt(i);
                if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        if (str.indexOf(".") > 0) {
            return str.indexOf(".") == str.lastIndexOf(".") && str.split("\\.").length == 2 ? pattern.matcher(str.replace(".", "")).matches() : false;
        } else {
            return pattern.matcher(str).matches();
        }
    }

    public static String doubleFormatInteger(double number, int jingdu) {
        String numberStr;
        if ((int)number * 1000 == (int)(number * 1000.0)) {
            numberStr = String.valueOf((int)number);
        } else {
            if (jingdu < 1) {
                return "";
            }

            String str = "######0.";

            for(int i = 1; i <= jingdu; ++i) {
                str = str + "0";
            }

            DecimalFormat df = new DecimalFormat(str);
            numberStr = df.format(number);
        }

        return numberStr;
    }

    private static boolean isSpecialCharacter(int b) {
        return b >= 32 && b <= 47 || b >= 58 && b <= 64 || b >= 91 && b <= 96 || b >= 123 && b <= 126 || b > 126;
    }

    public static boolean hasSpecialCharacter(String content) {
        Pattern p = Pattern.compile("[^a-zA-Z0-9一-\u9fff]");
        Matcher matcher = p.matcher(content);
        String source_new = matcher.replaceAll("*");
        return !content.equals(source_new);
    }

    public static String removeSpecialCharacters(String a) {
        StringBuffer s = new StringBuffer(a);
        int lenvar = s.length();
        String myString = "";

        for(int i = 0; i < s.length(); ++i) {
            if (!hasSpecialCharacter(s.charAt(i) + "")) {
                myString = myString + s.charAt(i);
            }
        }

        return myString;
    }

    public static String formatString(int targetWidth, char chr1, String content) throws UnsupportedEncodingException {
        String str = "";
        String cs = "";
        if (content.length() >= targetWidth) {
            str = content;
        } else {
            for(int i = 0; i < targetWidth - content.getBytes("GB2312").length; ++i) {
                cs = cs + chr1;
            }

            str = content + cs;
        }

        return str;
    }

    public static String formatString2(int targetWidth, char chr1, String content) throws UnsupportedEncodingException {
        String str = "";
        String cs = "";
        if (content.length() >= targetWidth) {
            str = content;
        } else {
            for(int i = 0; i < (targetWidth - content.getBytes("GB2312").length) / 2; ++i) {
                cs = cs + chr1;
            }

            str = cs + content + cs;
        }

        return str;
    }

    public static String convertEncodingFormat(String str, String formatFrom, String FormatTo) {
        String result = null;
        if (str != null && str.length() != 0) {
            try {
                result = new String(str.getBytes(formatFrom), FormatTo);
            } catch (UnsupportedEncodingException var5) {
                var5.printStackTrace();
            }
        }

        return result;
    }

    public static String removeSpecialCharacters2(String content) {
        Pattern p = Pattern.compile("[^a-zA-Z0-9一-\u9fff]");
        Matcher matcher = p.matcher(content);
        String source_new = matcher.replaceAll("*");
        return source_new;
    }

    public static String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();

        for(int i = 0; i < bs.length; ++i) {
            int bit = (bs[i] & 240) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 15;
            sb.append(chars[bit]);
        }

        return sb.toString().trim();
    }

    public static byte[] str2HexByte(String str) {
        byte[] bs = str.getBytes();
        return bs;
    }

    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];

        for(int i = 0; i < bytes.length; ++i) {
            int n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte)(n & 255);
        }

        return new String(bytes);
    }
}
