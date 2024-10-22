package eu.depau.etchdroid.plugins.telemetry

interface ITelemetryScope {
    var logLevel: TelemetryLevel?

    fun addBreadcrumb(breadcrumb: TelemetryBreadcrumb)
    fun clearBreadcrumbs()
    fun clear()
    fun setTag(key: String, value: String)
    fun removeTag(key: String)
    fun setExtra(key: String, value: String)
    fun removeExtra(key: String)
}