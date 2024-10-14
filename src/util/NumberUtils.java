package util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtils {

    private static final String MILLION_UNIT = "万";
    private static final String BILLION_UNIT = "亿";
    private static final BigDecimal ONE_HUNDRED_THOUSAND = new BigDecimal(100000);
    private static final BigDecimal ONE_HUNDRED_MILLION = new BigDecimal(100000000);
    private static final BigDecimal TEN_THOUSAND = new BigDecimal(10000);

    /**
     * 10      * 将数字转换成以万为单位或者以亿为单位，因为在前端数字太大显示有问题
     * 11      * @param amount
     * 12      * @return
     * 13
     */
    public static String amountConversion(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        if (amount.abs().compareTo(ONE_HUNDRED_THOUSAND) < 0) {
            //如果小于10万
            return amount.stripTrailingZeros().toPlainString();
        }
        if (amount.abs().compareTo(ONE_HUNDRED_MILLION) < 0) {
            //如果大于10万小于1亿
            return amount.divide(TEN_THOUSAND, 4, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + MILLION_UNIT;
        }
        return amount.divide(ONE_HUNDRED_MILLION, 4, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + BILLION_UNIT;
    }

    /**
     * 30      * 将数字转换成以亿为单位
     * 31      * @param amount
     * 32      * @return
     * 33
     */
//    public static Double amountConversionBillion(BigDecimal amount) {
//        if (amount == null) {
//            return null;
//        }
//        return amount.divide(ONE_HUNDRED_MILLION, 2, RoundingMode.HALF_UP).stripTrailingZeros().doubleValue();
//    }

}