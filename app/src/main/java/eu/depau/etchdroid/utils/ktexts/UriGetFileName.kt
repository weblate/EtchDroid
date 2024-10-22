package eu.depau.etchdroid.utils.ktexts

import android.content.Context
import android.net.Uri

fun Uri.getFileName(context: Context): String? = this.getDisplayName(context)
    ?: this.getFilePath(context)
        ?.split("/")
        ?.last()
