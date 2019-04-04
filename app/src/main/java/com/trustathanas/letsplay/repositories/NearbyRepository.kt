package com.trustathanas.letsplay.repositories

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.trustathanas.letsplay.utilities.SERVICE_ID
import com.trustathanas.letsplay.utilities.SERVICE_NAME
import com.google.android.gms.nearby.connection.DiscoveryOptions


class NearbyRepository(private val context: Context) {
    val advertisingStatus: MutableLiveData<Boolean> = MutableLiveData()
    val discoveryStatus: MutableLiveData<Boolean> = MutableLiveData()

    private lateinit var connectionLifecycleCallback: ConnectionLifecycleCallback
    private lateinit var endpointDiscoveryCallback: EndpointDiscoveryCallback
    private lateinit var payloadCallback: PayloadCallback

    fun startAdvertisingService() {
        initialiseConnectionLifecycleCallback()
        startAdvertising()
    }

    fun stopAdvertisingService() {
        Nearby.getConnectionsClient(context).stopAdvertising()
    }

    fun startDiscoveryService() {
        initialiseEndpointDiscoveryCallback()
        startDiscovery()
    }

    fun stopDiscoveryService() {
        Nearby.getConnectionsClient(context).stopDiscovery()
    }


    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        Nearby.getConnectionsClient(context)
            .startAdvertising(
                SERVICE_NAME, SERVICE_ID, connectionLifecycleCallback, advertisingOptions
            )
            .addOnSuccessListener { unused: Void ->
                // We're advertising!
            }
            .addOnFailureListener { e: Exception ->
                // We were unable to start advertising.
            }
    }

    private fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        Nearby.getConnectionsClient(context)
            .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener { unused: Void ->
                // We're discovering!
            }
            .addOnFailureListener { e: Exception ->
                // We're unable to start discovering.
            }
    }

    private fun initialiseConnectionLifecycleCallback() {
        connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
            override fun onConnectionResult(endpointId: String, conntetionResolution: ConnectionResolution) {
            }

            override fun onDisconnected(endpointId: String) {
            }

            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                // Automatically accept the connection on both sides.
                Nearby.getConnectionsClient(context).acceptConnection(endpointId, payloadCallback)
            }

        }
    }

    private fun initialiseEndpointDiscoveryCallback() {
        endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, endpointInfo: DiscoveredEndpointInfo) {
                // requesting a connection to the first found device
                Nearby.getConnectionsClient(context)
                    .requestConnection(SERVICE_NAME, endpointId, connectionLifecycleCallback)
                    .addOnCanceledListener { }
                    .addOnFailureListener { }
            }

            override fun onEndpointLost(endpointId: String) {

            }

        }
    }

    private fun initializePayloadCallback(){}


}