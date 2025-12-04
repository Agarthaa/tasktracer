package com.example.tasktracker.util.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.toFormattedString(pattern: String = "MMM dd, yyyy"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(this)
}

fun Long.toFormattedDateString(pattern: String = "MMM dd, yyyy"): String {
    return Date(this).toFormattedString(pattern)
}

fun String.toDate(pattern: String = "MMM dd, yyyy"): Date? {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).parse(this)
    } catch (e: Exception) {
        null
    }
}