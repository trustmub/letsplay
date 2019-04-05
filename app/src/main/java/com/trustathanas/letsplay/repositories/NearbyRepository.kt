package com.trustathanas.letsplay.repositories

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.trustathanas.letsplay.utilities.SERVICE_ID
import com.trustathanas.letsplay.utilities.SERVICE_NAME
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.trustathanas.letsplay.models.DeviceModel
import com.trustathanas.letsplay.models.PayloadModel
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.text.Charsets.UTF_8

class NearbyRepository(private val context: Context) {

    val TAG = this.javaClass.name

    val advertisingStatus: MutableLiveData<Boolean> = MutableLiveData()
    val discoveryStatus: MutableLiveData<Boolean> = MutableLiveData()
    val connectionInitiated: MutableLiveData<Boolean> = MutableLiveData()
    val deviceIdModel: MutableLiveData<DeviceModel> = MutableLiveData()
    val payloadReceived: MutableLiveData<PayloadModel> = MutableLiveData()
    private lateinit var connectionsClient: ConnectionsClient


    private lateinit var connectionLifecycleCallback: ConnectionLifecycleCallback
    private lateinit var endpointDiscoveryCallback: EndpointDiscoveryCallback
    private lateinit var payloadCallback: PayloadCallback

    init {
        initialiseConnectionLifecycleCallback()
        initialiseEndpointDiscoveryCallback()
        initializePayloadCallback()
        connectionsClient = Nearby.getConnectionsClient(context)
    }

    fun getDeviceId(): LiveData<DeviceModel> {
        return deviceIdModel
    }

    fun getReceivedPayload(): LiveData<PayloadModel> = payloadReceived

    fun getConnectionClient(): ConnectionsClient {
        return connectionsClient
    }

    fun getConnectionStatus(): LiveData<Boolean> {
        return connectionInitiated

    }

    fun startAdvertisingService() {
        startAdvertising()
    }

    fun stopAdvertisingService() {
        Nearby.getConnectionsClient(context).stopAdvertising()
    }

    fun startDiscoveryService() {
        startDiscovery()
    }

    fun stopDiscoveryService() {
        Nearby.getConnectionsClient(context).stopDiscovery()
    }


    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionLifecycleCallback?.let {
            Nearby.getConnectionsClient(context)
                .startAdvertising(
                    SERVICE_NAME, SERVICE_ID, it, advertisingOptions
                )
                .addOnSuccessListener {
                    // We're advertising!
                    println("Nearby: Repository: Advertising started")
                    Log.d(TAG, "startAdvertising: Success")
                }
                .addOnFailureListener { e: Exception ->
                    // We were unable to start advertising.
                    Log.d(TAG, "startAdvertising: Failed: $e")
                    println("Nearby: Repository: Advertising Failed: $e")

                }
        }

    }

    private fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        Nearby.getConnectionsClient(context)
            .startDiscovery(SERVICE_ID, endpointDiscoveryCallback!!, discoveryOptions)
            .addOnSuccessListener {
                println("Nearby: Repository: Discovery started")
            }
            .addOnFailureListener {
                println("Nearby: Repository: Discovery started")
            }
    }

    private fun initialiseConnectionLifecycleCallback() {
        connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
            override fun onConnectionResult(endpointId: String, conntetionResolution: ConnectionResolution) {
                Log.d(TAG, "Nearby: onConnectionResult: Success endpoint Id: $endpointId")

            }

            override fun onDisconnected(endpointId: String) {
                Log.d(TAG, "Nearby: startAdvertising: Success endpoint Id: $endpointId\"")
            }

            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                // Automatically accept the connection on both sides.
                Nearby.getConnectionsClient(context).acceptConnection(endpointId, payloadCallback)
                println("Nearby: Connection initiated $endpointId ConnectionInfo: $connectionInfo")
                connectionInitiated.postValue(true)
            }

        }
    }

    private fun initialiseEndpointDiscoveryCallback() {
        endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, endpointInfo: DiscoveredEndpointInfo) {
                // requesting a connection to the first found device
                connectionLifecycleCallback?.let {
                    println("Nearby: Endpoint ID: $endpointId, Endpoint Info: $endpointInfo")
                    deviceIdModel.postValue(DeviceModel(endpointId, endpointInfo))

                    Nearby.getConnectionsClient(context)
                        .requestConnection(SERVICE_NAME, endpointId, it)
                        .addOnSuccessListener { }
                        .addOnCanceledListener { }
                        .addOnFailureListener { }
                }
            }

            override fun onEndpointLost(endpointId: String) {
                println("Nearby: EDisconnected : $endpointId")
            }

        }
    }

    private fun initializePayloadCallback() {
        payloadCallback = object : PayloadCallback() {
            override fun onPayloadReceived(endpointId: String, payload: Payload) {
                payloadReceived.postValue(PayloadModel(endpointId, payload))

            }

            override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
                println("Nearby: endpoint: $endpointId, Payload: $update")

            }

        }
    }


}