package org.meteoinfo.global.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class JDateUtil {
    /**
     * Convert OA date to date
     *
     * @param oaDate OA date
     * @return Date
     */
    public static LocalDateTime fromOADate(double oaDate) {
        long t = (long) BigDecimalUtil.mul(oaDate, 1000000);
        Instant instant = Instant.ofEpochMilli(t);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    /**
     * Convert date to OA date
     *
     * @param ldt Local date time
     * @return OA date
     */
    public static double toOADate(LocalDateTime ldt) {
        double oaDate = ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        oaDate = BigDecimalUtil.div(oaDate, 1000000);

        return oaDate;
    }
}
