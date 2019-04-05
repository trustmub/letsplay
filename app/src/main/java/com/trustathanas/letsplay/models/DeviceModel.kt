package com.trustathanas.letsplay.models

import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.Payload

data class DeviceModel(val deviceId: String, val deviceInfo: DiscoveredEndpointInfo)

data class PayloadModel(val endpintId: String, val payload: Payload)