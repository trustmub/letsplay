package com.trustathanas.letsplay.utilities

import android.os.Build

const val EXTRA_CONNECTION_DETAILS = "device_service_info"
const val SERVICE_TYPE = "_http._tcp."
val SERVICE_NAME = getDeviceName()


private fun getDeviceName(): String {
    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL
    return if (model.startsWith(manufacturer)) {
        model.toUpperCase()
    } else manufacturer.toUpperCase() + " " + model
}