package org.paidaki.pogofinder.util;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class Util {

    private Util() {
        super();
    }

    public static <T> boolean isSorted(List<T> list, Comparator<T> comparator) {
        if (list == null) return false;
        if (list.size() < 2) return true;

        int order = 0;
        T prevItem = list.get(0);

        for (T item : list) {
            int comp = comparator.compare(prevItem, item);

            if (order != 0 && order != comp) {
                return false;
            }
            order = comp;
            prevItem = item;
        }
        return true;
    }

    public static double haversine(double lat1, double lng1, double lat2, double lng2) {
        double R = 6372.8 * 1000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }

    public static void setThreadPoolSize(ScheduledExecutorService executor, int threads) {
        ((ThreadPoolExecutor) executor).setCorePoolSize(threads);
    }

    public static int getCurrentSeconds() {
        return getSeconds(System.currentTimeMillis());
    }

    public static int getSeconds(long time) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(time);
        return calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.SECOND);
    }

    public static <T extends Comparable<Number>> int binarySearch(List<T> list, Number key) {
        int first = 0;
        int last = list.size() - 1;

        while (first < last) {
            int mid = (first + last) / 2;

            if (list.get(mid).compareTo(key) < 0) {
                first = mid + 1;
            } else {
                last = mid;
            }
        }
        return first;
    }
}
