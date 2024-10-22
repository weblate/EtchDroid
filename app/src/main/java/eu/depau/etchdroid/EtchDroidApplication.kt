package eu.depau.etchdroid

import android.app.Application
import eu.depau.etchdroid.plugins.telemetry.Telemetry

class EtchDroidApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Telemetry.init(this)
    }
}
