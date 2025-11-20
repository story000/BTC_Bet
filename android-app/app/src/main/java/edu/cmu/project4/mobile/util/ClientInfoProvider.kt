package edu.cmu.project4.mobile.util

import android.content.Context
import android.os.Build
import android.provider.Settings

object ClientInfoProvider {
    fun buildClientId(context: Context): String {
        val brand = Build.BRAND.orEmpty()
        val model = Build.MODEL.orEmpty()
        val osVersion = Build.VERSION.RELEASE.orEmpty()
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return listOf(brand, model, osVersion, androidId ?: "unknown")
            .filter { it.isNotBlank() }
            .joinToString(separator = "-")
    }
}
