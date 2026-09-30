package com.example.core.logging

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel {
    DEBUG, INFO, WARNING, ERROR, CRITICAL
}

data class LogEntry(
    val timestamp: Long,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val contextData: Map<String, String> = emptyMap()
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date(timestamp))
}

object StructuredLogger {
    private const val MAX_LOGS = 150
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    fun log(level: LogLevel, tag: String, message: String, context: Map<String, String> = emptyMap()) {
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = message,
            contextData = context
        )

        try {
            when (level) {
                LogLevel.DEBUG -> Log.d("ARTHROSCAN:$tag", message)
                LogLevel.INFO -> Log.i("ARTHROSCAN:$tag", message)
                LogLevel.WARNING -> Log.w("ARTHROSCAN:$tag", message)
                LogLevel.ERROR, LogLevel.CRITICAL -> Log.e("ARTHROSCAN:$tag", message)
            }
        } catch (_: Throwable) {
            // Local JVM unit test environment where android.util.Log is unmocked
            println("[${level.name}] ARTHROSCAN:$tag: $message")
        }

        val updated = (_logs.value + entry).takeLast(MAX_LOGS)
        _logs.value = updated
    }

    fun d(tag: String, msg: String, ctx: Map<String, String> = emptyMap()) = log(LogLevel.DEBUG, tag, msg, ctx)
    fun i(tag: String, msg: String, ctx: Map<String, String> = emptyMap()) = log(LogLevel.INFO, tag, msg, ctx)
    fun w(tag: String, msg: String, ctx: Map<String, String> = emptyMap()) = log(LogLevel.WARNING, tag, msg, ctx)
    fun e(tag: String, msg: String, ctx: Map<String, String> = emptyMap()) = log(LogLevel.ERROR, tag, msg, ctx)
}
