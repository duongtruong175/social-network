package vn.hust.socialnetwork.utils;

import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeExtension {
    /**
     * Format time string to display time notification.
     * Calculator difference time to now.
     *
     * @param time a UTC ISO-8601 datetime string (yyyy-MM-dd'T'HH:mm:ss.SSS'Z')
     * @return time ago
     */
    public static String formatTimeNotification(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date date = sdf.parse(time);
            long t = date.getTime();
            long now = System.currentTimeMillis();
            CharSequence ago;
            if (now - t > 13 * DateUtils.WEEK_IN_MILLIS) {
                ago = DateUtils.getRelativeTimeSpanString(t, now, DateUtils.MINUTE_IN_MILLIS);
            } else if (now - t > (4 * DateUtils.WEEK_IN_MILLIS + 2 * DateUtils.DAY_IN_MILLIS)) {
                ago = (now - t) / (30 * DateUtils.DAY_IN_MILLIS) + " tháng trước";
            } else if (now - t > DateUtils.WEEK_IN_MILLIS) {
                ago = DateUtils.getRelativeTimeSpanString(t, now, DateUtils.WEEK_IN_MILLIS);
            } else if (now - t > DateUtils.DAY_IN_MILLIS) {
                ago = (now - t) / DateUtils.DAY_IN_MILLIS + " ngày trước";
            } else if (now - t > DateUtils.HOUR_IN_MILLIS) {
                ago = DateUtils.getRelativeTimeSpanString(t, now, DateUtils.HOUR_IN_MILLIS);
            } else if (now - t > DateUtils.MINUTE_IN_MILLIS) {
                ago = DateUtils.getRelativeTimeSpanString(t, now, DateUtils.MINUTE_IN_MILLIS);
            } else {
                ago = "Vừa xong";
            }
            return (String) ago;
        } catch (ParseException e) {
            return "";
        }
    }

    /**
     * Format time string to display time post.
     * Calculator difference time to now.
     *
     * @param time a UTC ISO-8601 datetime string (yyyy-MM-dd'T'HH:mm:ss.SSS'Z')
     * @return time ago
     */
    public static String formatTimePost(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date date = sdf.parse(time);
            long t = date.getTime();
            long now = System.currentTimeMillis();
            CharSequence ago;
            if (now - t > 13 * DateUtils.WEEK_IN_MILLIS) {
                ago = DateUtils.getRelativeTimeSpanString(t, now, DateUtils.MINUTE_IN_MILLIS);
            } else if (now - t > (4 * DateUtils.WEEK_IN_MILLIS + 2 * DateUtils.DAY_IN_MILLIS)) {
                ago = (now - t) / (30 * DateUtils.DAY_IN_MILLIS) + " tháng trước";
            } else if (now - t > DateUtils.WEEK_IN_MILLIS) {
                ago = DateUtils.getRelativeTimeSpanString(t, now, DateUtils.WEEK_IN_MILLIS);
            } else if (now - t > DateUtils.DAY_IN_MILLIS) {
                ago = (now - t) / DateUtils.DAY_IN_MILLIS + " ngày trước";
            } else if (now - t > DateUtils.HOUR_IN_MILLIS) {
                ago = DateUtils.getRelativeTimeSpanString(t, now, DateUtils.HOUR_IN_MILLIS);
            } else if (now - t > DateUtils.MINUTE_IN_MILLIS) {
                ago = DateUtils.getRelativeTimeSpanString(t, now, DateUtils.MINUTE_IN_MILLIS);
            } else {
                ago = "Vừa xong";
            }
            return (String) ago;
        } catch (ParseException e) {
            return "";
        }
    }

    /**
     * Format time string to display time comment.
     * Calculator difference time to now.
     *
     * @param time a UTC ISO-8601 datetime string (yyyy-MM-dd'T'HH:mm:ss.SSS'Z')
     * @return time ago (WEEK_IN_MILLIS)
     */
    public static String formatTimeComment(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date date = sdf.parse(time);
            long t = date.getTime();
            long now = System.currentTimeMillis();
            CharSequence ago;
            if (now - t > DateUtils.YEAR_IN_MILLIS) {
                ago = DateUtils.getRelativeTimeSpanString(t, now, DateUtils.YEAR_IN_MILLIS);
            } else if (now - t > DateUtils.WEEK_IN_MILLIS) {
                ago = DateUtils.getRelativeTimeSpanString(t, now, DateUtils.WEEK_IN_MILLIS);
            } else if (now - t > DateUtils.DAY_IN_MILLIS) {
                ago = (now - t) / DateUtils.DAY_IN_MILLIS + " ngày trước";
            } else if (now - t > DateUtils.HOUR_IN_MILLIS) {
                ago = DateUtils.getRelativeTimeSpanString(t, now, DateUtils.HOUR_IN_MILLIS);
            } else if (now - t > DateUtils.MINUTE_IN_MILLIS) {
                ago = DateUtils.getRelativeTimeSpanString(t, now, DateUtils.MINUTE_IN_MILLIS);
            } else {
                ago = "Vừa xong";
            }
            return (String) ago;
        } catch (ParseException e) {
            return "";
        }
    }

    /**
     * Format time string to display time relation.
     * Calculator difference time to now.
     *
     * @param time a UTC ISO-8601 datetime string (yyyy-MM-dd'T'HH:mm:ss.SSS'Z')
     * @return time ago
     */
    public static String formatTimeRelation(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date date = sdf.parse(time);
            long t = date.getTime();
            long now = System.currentTimeMillis();
            CharSequence ago;
            if (now - t > DateUtils.YEAR_IN_MILLIS) {
                ago = DateUtils.getRelativeTimeSpanString(t, now, DateUtils.MINUTE_IN_MILLIS);
            } else if (now - t > (4 * DateUtils.WEEK_IN_MILLIS + 2 * DateUtils.DAY_IN_MILLIS)) {
                ago = (now - t) / (30 * DateUtils.DAY_IN_MILLIS) + " tháng trước";
            } else if (now - t > DateUtils.WEEK_IN_MILLIS) {
                ago = DateUtils.getRelativeTimeSpanString(t, now, DateUtils.WEEK_IN_MILLIS);
            } else if (now - t > DateUtils.DAY_IN_MILLIS) {
                ago = (now - t) / DateUtils.DAY_IN_MILLIS + " ngày trước";
            } else if (now - t > DateUtils.HOUR_IN_MILLIS) {
                ago = DateUtils.getRelativeTimeSpanString(t, now, DateUtils.HOUR_IN_MILLIS);
            } else if (now - t > DateUtils.MINUTE_IN_MILLIS) {
                ago = DateUtils.getRelativeTimeSpanString(t, now, DateUtils.MINUTE_IN_MILLIS);
            } else {
                ago = "Vừa xong";
            }
            return (String) ago;
        } catch (ParseException e) {
            return "";
        }
    }

    /**
     * Format time string to display time join in.
     *
     * @param time a UTC ISO-8601 datetime string (yyyy-MM-dd'T'HH:mm:ss.SSS'Z')
     * @return Tháng xx năm xxxx
     */
    public static String formatTimeJoinIn(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date date = sdf.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            // Add one to month {0 - 11}
            int month = calendar.get(Calendar.MONTH) + 1;
            return "Tháng " + month + " năm " + year;
        } catch (ParseException e) {
            return "";
        }
    }

    /**
     * Display Zodiac sign for given date of birth
     * <p>+ aquarius (Bảo bình): 20/01 - 19/02
     * <p>+ pisces (Song ngư): 20/02 - 20/03
     * <p>+ aries (Bạch dương): 21/03 - 19/04
     * <p>+ taurus (Kim ngưu): 20/04 - 20/05
     * <p>+ gemini (Song tử): 21/05 - 20/06
     * <p>+ cancer (Cự giải): 21/06 - 22/07
     * <p>+ leo (Sư tử): 23/07 - 22/08
     * <p>+ virgo (Xử nữ): 23/08 - 22/09
     * <p>+ libra (Thiên bình): 23/09 - 22/10
     * <p>+ scorpio (Thiên yết): 23/10 - 22/11
     * <p>+ sagittarius (Nhân mã): 23/11 - 21/12
     * <p>+ capricorn (Ma kết): 22/12- 19/01
     *
     * @param date a string date (yyyy-MM-dd)
     * @return zodiac sign
     */
    public static String getZodiac(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date d = sdf.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(d);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            // Add one to month {0 - 11}
            int month = calendar.get(Calendar.MONTH) + 1;
            switch (month) {
                case 1:
                    if (day < 20) {
                        return "capricorn";
                    } else {
                        return "aquarius";
                    }
                case 2:
                    if (day < 18) {
                        return "aquarius";
                    } else {
                        return "pisces";
                    }
                case 3:
                    if (day < 21) {
                        return "pisces";
                    } else {
                        return "aries";
                    }
                case 4:
                    if (day < 20) {
                        return "aries";
                    } else {
                        return "taurus";
                    }
                case 5:
                    if (day < 21) {
                        return "taurus";
                    } else {
                        return "gemini";
                    }
                case 6:
                    if (day < 21) {
                        return "gemini";
                    } else {
                        return "cancer";
                    }
                case 7:
                    if (day < 23) {
                        return "cancer";
                    } else {
                        return "leo";
                    }
                case 8:
                    if (day < 23) {
                        return "leo";
                    } else {
                        return "virgo";
                    }
                case 9:
                    if (day < 23) {
                        return "virgo";
                    } else {
                        return "libra";
                    }
                case 10:
                    if (day < 23) {
                        return "libra";
                    } else {
                        return "scorpio";
                    }
                case 11:
                    if (day < 22) {
                        return "scorpio";
                    } else {
                        return "sagittarius";
                    }
                case 12:
                    if (day < 22) {
                        return "sagittarius";
                    } else {
                        return "capricorn";
                    }
                default:
                    return "";
            }
        } catch (ParseException e) {
            return "";
        }
    }
}
