package eu.depau.etchdroid.plugins.telemetry

import android.util.Log
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class TelemetryBreadcrumb(
    val timestamp: Date? = null,
    val timestampMs: Long? = null,
    var message: String? = null,
    var type: String? = null,
    var data: MutableMap<String, Any> = ConcurrentHashMap(),
    var category: String? = null,
    var origin: String? = null,
    var level: TelemetryLevel? = null,
) {
    companion object {
        fun fromMap(map: Map<String, Any>): TelemetryBreadcrumb {
            var timestamp: Date = Date()
            var message: String? = null
            var type: String? = null
            val data: MutableMap<String, Any> = ConcurrentHashMap()
            var category: String? = null
            var origin: String? = null
            var level: TelemetryLevel? = null

            for ((key, value) in map) {
                @Suppress("UNCHECKED_CAST")
                when (key) {
                    "timestamp" -> if (value is String) timestamp = Date(value.toLong())
                    "message" -> if (value is String) message = value
                    "type" -> if (value is String) type = value
                    "data" -> if (value is Map<*, *>) data.putAll(value as Map<String, Any>)
                    "category" -> if (value is String) category = value
                    "origin" -> if (value is String) origin = value
                    "level" -> if (value is String) level = try {
                        TelemetryLevel.valueOf(value.uppercase(Locale.getDefault()))
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            return TelemetryBreadcrumb(
                    timestamp = timestamp,
                    message = message,
                    type = type,
                    data = data,
                    category = category,
                    origin = origin,
                    level = level
            )
        }

        fun debug(message: String, category: String? = null): TelemetryBreadcrumb {
            return TelemetryBreadcrumb(
                    type = "debug",
                    message = message,
                    category = category,
                    level = TelemetryLevel.DEBUG,
            )
        }

        fun error(message: String, category: String? = null): TelemetryBreadcrumb {
            return TelemetryBreadcrumb(
                    type = "error",
                    message = message,
                    category = category,
                    level = TelemetryLevel.ERROR,
            )
        }

        fun info(message: String, category: String? = null): TelemetryBreadcrumb {
            return TelemetryBreadcrumb(
                    type = "info",
                    message = message,
                    category = category,
                    level = TelemetryLevel.INFO,
            )
        }
    }
}

fun TelemetryBreadcrumb.log() {
    val logPrio = when (level) {
        TelemetryLevel.DEBUG -> Log.DEBUG
        TelemetryLevel.INFO -> Log.INFO
        TelemetryLevel.WARNING -> Log.WARN
        TelemetryLevel.ERROR -> Log.ERROR
        TelemetryLevel.FATAL -> Log.ERROR
        null -> Log.INFO
    }
    val category = category ?: "EtchDroid"
    val tag = if (Telemetry.isStub) "debug" else "tmtry"
    Log.println(logPrio, "$tag.$category", message ?: "[no message]")
}
