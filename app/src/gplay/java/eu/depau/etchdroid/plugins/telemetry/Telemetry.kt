package eu.depau.etchdroid.plugins.telemetry

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import eu.depau.etchdroid.BuildConfig
import io.sentry.Breadcrumb
import io.sentry.IScope
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.compose.SentryModifier.sentryTag
import io.sentry.compose.SentryTraced

internal const val SENTRY_DSN =
    "https://39a6e220c97c585acd25ced5a6855b4d@o4508123221590016.ingest.de.sentry.io/4508123222704209"

internal const val DEBUG_SAMPLE_RATE = 1.0
internal const val DEBUG_ERROR_SAMPLE_RATE = 1.0
internal const val PROD_SAMPLE_RATE = 0.01
internal const val PROD_ERROR_SAMPLE_RATE = 0.1

internal const val PREFS_NAME = "telemetry"
internal const val PREFS_ENABLED_KEY = "telemetry_enabled"

internal fun TelemetryLevel.toSentry(): SentryLevel = when (this) {
    TelemetryLevel.DEBUG -> SentryLevel.DEBUG
    TelemetryLevel.INFO -> SentryLevel.INFO
    TelemetryLevel.WARNING -> SentryLevel.WARNING
    TelemetryLevel.ERROR -> SentryLevel.ERROR
    TelemetryLevel.FATAL -> SentryLevel.FATAL
}

internal fun SentryLevel.toWrapper(): TelemetryLevel = when (this) {
    SentryLevel.DEBUG -> TelemetryLevel.DEBUG
    SentryLevel.INFO -> TelemetryLevel.INFO
    SentryLevel.WARNING -> TelemetryLevel.WARNING
    SentryLevel.ERROR -> TelemetryLevel.ERROR
    SentryLevel.FATAL -> TelemetryLevel.FATAL
}

internal fun TelemetryBreadcrumb.toSentry(): Breadcrumb {
    val sentry = if (timestamp != null) {
        Breadcrumb(timestamp)
    } else {
        Breadcrumb()
    }

    sentry.message = message
    sentry.category = category
    sentry.level = level?.toSentry()
    sentry.type = type
    @Suppress("UnstableApiUsage")
    sentry.data.putAll(data)
    sentry.origin = origin

    return sentry
}

internal fun sentryScopeAdapter(scope: IScope): ITelemetryScope {
    return object : ITelemetryScope {
        override var logLevel: TelemetryLevel?
            get() = scope.level?.toWrapper()
            set(value) {
                scope.level = value?.toSentry()
            }

        override fun addBreadcrumb(breadcrumb: TelemetryBreadcrumb) {
            breadcrumb.log()
            scope.addBreadcrumb(breadcrumb.toSentry())
        }

        override fun clearBreadcrumbs() {
            scope.clearBreadcrumbs()
        }

        override fun clear() {
            scope.clear()
        }

        override fun setTag(key: String, value: String) {
            scope.setTag(key, value)
        }

        override fun removeTag(key: String) {
            scope.removeTag(key)
        }

        override fun setExtra(key: String, value: String) {
            scope.setExtra(key, value)
        }

        override fun removeExtra(key: String) {
            scope.removeExtra(key)
        }
    }
}

object Telemetry : ITelemetry {
    private lateinit var sharedPrefs: SharedPreferences
    private var _enabled: Boolean = false

    override val isStub: Boolean
        get() = false

    override var enabled: Boolean
        get() = _enabled
        set(value) {
            sharedPrefs.edit().putBoolean(PREFS_ENABLED_KEY, value).apply()
            reinit(value)
        }

    private fun reinit(enabled: Boolean) {
        if (_enabled == enabled)
            return
        _enabled = enabled
        Log.i("Telemetry", "Enabled: $enabled")

        if (!enabled) {
            Sentry.close()
        } else {
            Sentry.init {
                it.dsn = SENTRY_DSN
                it.isEnableUserInteractionTracing = true

                if (BuildConfig.DEBUG) {
                    it.environment = "debug"
                    it.tracesSampleRate = DEBUG_SAMPLE_RATE
                    it.profilesSampleRate = DEBUG_SAMPLE_RATE
                    it.experimental.sessionReplay.sessionSampleRate = DEBUG_SAMPLE_RATE
                    it.experimental.sessionReplay.onErrorSampleRate = DEBUG_ERROR_SAMPLE_RATE
                } else {
                    it.environment = "production"
                    it.tracesSampleRate = PROD_SAMPLE_RATE
                    it.profilesSampleRate = PROD_SAMPLE_RATE
                    it.experimental.sessionReplay.sessionSampleRate = PROD_SAMPLE_RATE
                    it.experimental.sessionReplay.onErrorSampleRate = PROD_ERROR_SAMPLE_RATE
                }
            }
        }
    }

    override fun init(context: Context) {
        sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        Log.i("Telemetry", "Enabled: ${sharedPrefs.getBoolean(PREFS_ENABLED_KEY, true)}")
        reinit(sharedPrefs.getBoolean(PREFS_ENABLED_KEY, true))
    }

    override fun Modifier.telemetryTag(tag: String): Modifier {
        return sentryTag(tag)
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun TelemetryTracedImpl(
        tag: String,
        modifier: Modifier,
        enableUserInteractionTracing: Boolean,
        content: @Composable (BoxScope.() -> Unit),
    ) {
        SentryTraced(
                tag = tag,
                modifier = modifier,
                enableUserInteractionTracing = enableUserInteractionTracing,
                content = content
        )
    }

    override fun configureScope(callback: ITelemetryScope.() -> Unit) {
        Sentry.configureScope {
            sentryScopeAdapter(it).callback()
        }
    }

    override fun captureException(throwable: Throwable): String {
        return Sentry.captureException(throwable).toString()
    }

    override fun captureException(
        throwable: Throwable,
        callback: ITelemetryScope.() -> Unit,
    ): String {
        return Sentry.captureException(throwable) {
            sentryScopeAdapter(it).callback()
        }.toString()
    }

    override fun addBreadcrumb(breadcrumb: TelemetryBreadcrumb) {
        breadcrumb.log()
        Sentry.addBreadcrumb(breadcrumb.toSentry())
    }

    override fun addBreadcrumb(message: String) {
        addBreadcrumb(TelemetryBreadcrumb.info(message))
    }

    override fun addBreadcrumb(message: String, category: String) {
        addBreadcrumb(TelemetryBreadcrumb.info(message, category))
    }

    override fun addBreadcrumb(scope: TelemetryBreadcrumb.() -> Unit) {
        val breadcrumb = TelemetryBreadcrumb()
        breadcrumb.scope()
        addBreadcrumb(breadcrumb)
    }

    override fun captureMessage(message: String): String {
        TelemetryBreadcrumb.error(message).log()
        return Sentry.captureMessage(message).toString()
    }

    override fun captureMessage(
        message: String,
        callback: ITelemetryScope.() -> Unit,
    ): String {
        TelemetryBreadcrumb.error(message).log()
        return Sentry.captureMessage(message) {
            sentryScopeAdapter(it).callback()
        }.toString()
    }

    override fun captureMessage(
        message: String,
        level: TelemetryLevel,
    ): String {
        TelemetryBreadcrumb(level = level, message = message).log()
        return Sentry.captureMessage(message, level.toSentry()).toString()
    }

    override fun captureMessage(
        message: String,
        level: TelemetryLevel,
        callback: ITelemetryScope.() -> Unit,
    ): String {
        TelemetryBreadcrumb(level = level, message = message).log()
        return Sentry.captureMessage(message, level.toSentry()) {
            sentryScopeAdapter(it).callback()
        }.toString()
    }
}
