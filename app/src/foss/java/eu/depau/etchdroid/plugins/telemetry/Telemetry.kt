package eu.depau.etchdroid.plugins.telemetry

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal object TelemetryScope : ITelemetryScope {
    override var logLevel: TelemetryLevel?
        get() = TelemetryLevel.DEBUG
        set(value) {}

    override fun addBreadcrumb(breadcrumb: TelemetryBreadcrumb) {
        breadcrumb.log()
    }

    override fun clearBreadcrumbs() {
    }

    override fun clear() {
    }

    override fun setTag(key: String, value: String) {
    }

    override fun removeTag(key: String) {
    }

    override fun setExtra(key: String, value: String) {
    }

    override fun removeExtra(key: String) {
    }
}

object Telemetry : ITelemetry {
    override val isStub: Boolean
        get() = true

    override fun Modifier.telemetryTag(tag: String): Modifier {
        return this
    }

    @Composable
    override fun TelemetryTracedImpl(
        tag: String,
        modifier: Modifier,
        enableUserInteractionTracing: Boolean,
        content: @Composable (BoxScope.() -> Unit),
    ) {
        Box {
            content()
        }
    }

    override fun configureScope(scope: ITelemetryScope.() -> Unit) {
        TelemetryScope.apply(scope)
    }

    override fun captureException(throwable: Throwable): String {
        Log.e("Telemetry", "Exception captured", throwable)
        return "0"
    }

    override fun captureException(
        throwable: Throwable,
        callback: ITelemetryScope.() -> Unit,
    ): String {
        captureException(throwable)
        TelemetryScope.apply(callback)
        return "0"
    }

    override fun addBreadcrumb(breadcrumb: TelemetryBreadcrumb) {
        breadcrumb.log()
    }

    override fun addBreadcrumb(message: String) {
        TelemetryBreadcrumb.info(message).log()
    }

    override fun addBreadcrumb(message: String, category: String) {
        TelemetryBreadcrumb.info(message, category).log()
    }

    override fun addBreadcrumb(scope: TelemetryBreadcrumb.() -> Unit) {
        TelemetryBreadcrumb().apply(scope).log()
    }

    override fun captureMessage(message: String): String {
        TelemetryBreadcrumb.error(message).log()
        return "0"
    }

    override fun captureMessage(
        message: String,
        callback: ITelemetryScope.() -> Unit,
    ): String {
        TelemetryBreadcrumb.error(message).log()
        TelemetryScope.apply(callback)
        return "0"
    }

    override fun captureMessage(
        message: String,
        level: TelemetryLevel,
    ): String {
        TelemetryBreadcrumb(level = level, message = message).log()
        return "0"
    }

    override fun captureMessage(
        message: String,
        level: TelemetryLevel,
        callback: ITelemetryScope.() -> Unit,
    ): String {
        TelemetryBreadcrumb(level = level, message = message).log()
        TelemetryScope.apply(callback)
        return "0"
    }
}
