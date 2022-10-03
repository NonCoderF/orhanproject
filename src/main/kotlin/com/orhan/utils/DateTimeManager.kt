package com.orhan.utils

import java.text.SimpleDateFormat
import java.util.*

object DateTimeManager {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    val timeFormatSecs = SimpleDateFormat("hh:mm:ss", Locale.ENGLISH)
    val dateFormat24 = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH)
    val dateFormatReverse24 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

    fun convertDateObject(date: String?, inputFormat: SimpleDateFormat, outputFormat: SimpleDateFormat): String? {
        try {
            val inputDate = inputFormat.parse(date)
            return outputFormat.format(inputDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun convertDateObject(timeStamp: Long, outputFormat: SimpleDateFormat): String? {
        try {
            val inputDate = Date(timeStamp)
            return outputFormat.format(inputDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun convertStringToDate(dateString: String?, format: SimpleDateFormat): Date? {
        if(dateString == null)
            return null
        return try {
            format.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun getTomeZoneOffset(): Int {
        val timeZone = TimeZone.getDefault()
        timeZone.useDaylightTime()
        val now = Date()
        val offset = timeZone.getOffset(now.time) / (15 * 60 * 1000.0)
        return offset.toInt()
    }

    fun getTimeDifferenceFromNow(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -year)
        calendar.add(Calendar.MONTH, -month)
        calendar.add(Calendar.DAY_OF_MONTH, -day)
        return calendar.timeInMillis
    }
}