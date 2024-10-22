package eu.depau.etchdroid.plugins.telemetry

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TelemetryTraced(
    tag: String,
    modifier: Modifier = Modifier,
    enableUserInteractionTracing: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    Telemetry.TelemetryTracedImpl(tag, modifier, enableUserInteractionTracing, content)
}
