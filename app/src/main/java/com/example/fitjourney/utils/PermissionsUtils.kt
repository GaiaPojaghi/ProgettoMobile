package com.example.fitjourney.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionsUtils {
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
