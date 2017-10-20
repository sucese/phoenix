package com.guoxiaoxing.phoenix.picker.util

import java.text.SimpleDateFormat

object DateUtils {

    private val msFormat = SimpleDateFormat("mm:ss")

    /**
     * MS turn every minute

     * @param duration Millisecond
     * *
     * @return Every minute
     */
    fun timeParse(duration: Long): String {
        var time = ""
        if (duration > 1000) {
            time = timeParseMinute(duration)
        } else {
            val minute = duration / 60000
            val seconds = duration % 60000
            val second = Math.round(seconds.toFloat() / 1000).toLong()
            if (minute < 10) {
                time += "0"
            }
            time += minute.toString() + ":"
            if (second < 10) {
                time += "0"
            }
            time += second
        }
        return time
    }

    /**
     * MS turn every minute

     * @param duration Millisecond
     * *
     * @return Every minute
     */
    fun timeParseMinute(duration: Long): String {
        try {
            return msFormat.format(duration)
        } catch (e: Exception) {
            e.printStackTrace()
            return "0:00"
        }

    }

    /**
     * 判断两个时间戳相差多少秒

     * @param d
     * *
     * @return
     */
    fun dateDiffer(d: Long): Int {
        try {
            val l1 = java.lang.Long.parseLong(System.currentTimeMillis().toString().substring(0, 10))
            val interval = l1 - d
            return Math.abs(interval).toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }

    }
}
