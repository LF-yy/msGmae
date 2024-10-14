package tools;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;

public class StringUtil
{
    public static String getLeftPaddedStr(final String in, final char padchar, final int length) {
        final StringBuilder builder = new StringBuilder(length);
        for (int x = in.getBytes().length; x < length; ++x) {
            builder.append(padchar);
        }
        builder.append(in);
        return builder.toString();
    }
    
    public static int getlength(final String str) {
        final byte[] bt = str.getBytes(Charset.forName("BIG5"));
        return bt.length;
    }
    
    public static String getRightPaddedStr(final String in, final char padchar, final int length) {
        final StringBuilder builder = new StringBuilder(in);
        for (int x = in.getBytes().length; x < length; ++x) {
            builder.append(padchar);
        }
        return builder.toString();
    }
    
    public static String joinStringFrom(final String[] arr, final int start) {
        return joinStringFrom(arr, start, " ");
    }
    
    public static String joinStringFrom(final String[] arr, final int start, final String sep) {
        final StringBuilder builder = new StringBuilder();
        for (int i = start; i < arr.length; ++i) {
            builder.append(arr[i]);
            if (i != arr.length - 1) {
                builder.append(sep);
            }
        }
        return builder.toString();
    }
    
    public static String makeEnumHumanReadable(final String enumName) {
        final StringBuilder builder = new StringBuilder(enumName.length() + 1);
        for (final String word : enumName.split("_")) {
            if (word.length() <= 2) {
                builder.append(word);
            }
            else {
                builder.append(word.charAt(0));
                builder.append(word.substring(1).toLowerCase());
            }
            builder.append(' ');
        }
        return builder.substring(0, enumName.length());
    }
    
    public static int countCharacters(final String str, final char chr) {
        int ret = 0;
        for (int i = 0; i < str.getBytes().length; ++i) {
            if (str.charAt(i) == chr) {
                ++ret;
            }
        }
        return ret;
    }
    
    public static String getReadableMillis(final long startMillis, final long endMillis) {
        final StringBuilder sb = new StringBuilder();
        final double elapsedSeconds = (double)(endMillis - startMillis) / 1000.0;
        final int elapsedSecs = (int)elapsedSeconds % 60;
        final int elapsedMinutes = (int)(elapsedSeconds / 60.0);
        final int elapsedMins = elapsedMinutes % 60;
        final int elapsedHrs = elapsedMinutes / 60;
        final int elapsedHours = elapsedHrs % 24;
        final int elapsedDays = elapsedHrs / 24;
        if (elapsedDays > 0) {
            final boolean mins = elapsedHours > 0;
            sb.append(elapsedDays);
            sb.append(" 天" + ((elapsedDays > 1) ? "s" : "") + (mins ? ", " : "."));
            if (mins) {
                final boolean secs = elapsedMins > 0;
                if (!secs) {
                    sb.append("and ");
                }
                sb.append(elapsedHours);
                sb.append(" 時" + ((elapsedHours > 1) ? "s" : "") + (secs ? ", " : "."));
                if (secs) {
                    final boolean millis = elapsedSecs > 0;
                    if (!millis) {
                        sb.append("and ");
                    }
                    sb.append(elapsedMins);
                    sb.append(" 分" + ((elapsedMins > 1) ? "s" : "") + (millis ? ", " : "."));
                    if (millis) {
                        sb.append("and ");
                        sb.append(elapsedSecs);
                        sb.append(" 秒" + ((elapsedSecs > 1) ? "s" : "") + ".");
                    }
                }
            }
        }
        else if (elapsedHours > 0) {
            final boolean mins = elapsedMins > 0;
            sb.append(elapsedHours);
            sb.append(" 時" + ((elapsedHours > 1) ? "s" : "") + (mins ? ", " : "."));
            if (mins) {
                final boolean secs = elapsedSecs > 0;
                if (!secs) {
                    sb.append("and ");
                }
                sb.append(elapsedMins);
                sb.append(" 分" + ((elapsedMins > 1) ? "s" : "") + (secs ? ", " : "."));
                if (secs) {
                    sb.append("and ");
                    sb.append(elapsedSecs);
                    sb.append(" 秒" + ((elapsedSecs > 1) ? "s" : "") + ".");
                }
            }
        }
        else if (elapsedMinutes > 0) {
            final boolean secs2 = elapsedSecs > 0;
            sb.append(elapsedMinutes);
            sb.append(" 分" + ((elapsedMinutes > 1) ? "s" : "") + (secs2 ? " " : "."));
            if (secs2) {
                sb.append("and ");
                sb.append(elapsedSecs);
                sb.append(" 秒" + ((elapsedSecs > 1) ? "s" : "") + ".");
            }
        }
        else if (elapsedSeconds > 0.0) {
            sb.append((int)elapsedSeconds);
            sb.append(" 秒" + ((elapsedSeconds > 1.0) ? "s" : "") + ".");
        }
        else {
            sb.append("None.");
        }
        return sb.toString();
    }
    
    public static int getDaysAmount(final long startMillis, final long endMillis) {
        final double elapsedSeconds = (double)(endMillis - startMillis) / 1000.0;
        final int elapsedMinutes = (int)(elapsedSeconds / 60.0);
        final int elapsedHrs = elapsedMinutes / 60;
        final int elapsedDays = elapsedHrs / 24;
        return elapsedDays;
    }
    
    public static String codeString(final String fileName) throws FileNotFoundException {
        return codeString(new File(fileName));
    }
    
    public static String codeString(final File file) throws FileNotFoundException {
        final BufferedInputStream bin = new BufferedInputStream((InputStream)new FileInputStream(file));
        int p = 0;
        try {
            p = (bin.read() << 8) + bin.read();
            bin.close();
        }
        catch (IOException ex) {}
        String code = null;
        switch (p) {
            case 65534: {
                code = "Unicode";
                break;
            }
            case 65279: {
                code = "UTF-16BE";
                break;
            }
            default: {
                code = "UTF-8";
                break;
            }
        }
        return code;
    }
    
    public static String removeFourChar(String content) {
        final byte[] conbyte = content.getBytes();
        for (int i = 0; i < conbyte.length; ++i) {
            if ((conbyte[i] & 0xF8) == 0xF0) {
                for (int j = 0; j < 4; ++j) {
                    conbyte[i + j] = 48;
                }
                i += 3;
            }
        }
        content = new String(conbyte);
        return content.replaceAll("0000", "");
    }

    public static String formatNum(String num, Boolean kBool) {
        StringBuffer sb = new StringBuffer();
        if (!StringUtils.isNumeric(num)) {
            return "0";
        } else {
            if (kBool == null) {
                kBool = false;
            }

            BigDecimal b0 = new BigDecimal("1000");
            BigDecimal b1 = new BigDecimal("10000");
            BigDecimal b2 = new BigDecimal("100000000");
            BigDecimal b3 = new BigDecimal(num);
            String formatNumStr = "";
            String nuit = "";
            if (kBool) {
                return b3.compareTo(b0) != 0 && b3.compareTo(b0) != 1 ? num : "999+";
            } else {
                if (b3.compareTo(b1) == -1) {
                    sb.append(b3.toString());
                } else if ((b3.compareTo(b1) != 0 || b3.compareTo(b1) != 1) && b3.compareTo(b2) != -1) {
                    if (b3.compareTo(b2) == 0 || b3.compareTo(b2) == 1) {
                        formatNumStr = b3.divide(b2).toString();
                        nuit = "亿";
                    }
                } else {
                    formatNumStr = b3.divide(b1).toString();
                    nuit = "万";
                }

                if (!"".equals(formatNumStr)) {
                    int i = formatNumStr.indexOf(".");
                    if (i == -1) {
                        sb.append(formatNumStr).append(nuit);
                    } else {
                        ++i;
                        String v = formatNumStr.substring(i, i + 1);
                        if (!v.equals("0")) {
                            sb.append(formatNumStr.substring(0, i + 1)).append(nuit);
                        } else {
                            sb.append(formatNumStr.substring(0, i - 1)).append(nuit);
                        }
                    }
                }

                return sb.length() == 0 ? "0" : sb.toString();
            }
        }
    }
}
