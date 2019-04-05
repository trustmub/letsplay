package com.trustathanas.letsplay.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.trustathanas.letsplay.models.DeviceModel
import com.trustathanas.letsplay.models.PayloadModel
import com.trustathanas.letsplay.repositories.NearbyRepository

class NearbyViewModel(private val repository: NearbyRepository) : ViewModel() {

    private lateinit var connectionClient: ConnectionsClient
    private lateinit var deviceId: MutableLiveData<DeviceModel>
    private lateinit var payloadReceived: MutableLiveData<PayloadModel>

    init {
        this.setConnectionClient()
        this.setDeviceId()
        this.setReceivedPayload()

    }

    fun startAdvertisingService() = repository.startAdvertisingService()
    fun startDiscoveryService() = repository.startDiscoveryService()

    fun stopAdvertisingService() = repository.stopAdvertisingService()
    fun stopDiscoveryService() = repository.stopDiscoveryService()

    fun getConnectionStatus() = repository.getConnectionStatus()
    fun getConnectionClient() = this.connectionClient

    fun getDeviceId() = deviceId
    fun getReceivedPayload() = payloadReceived

    private fun setConnectionClient() {
        connectionClient = repository.getConnectionClient()
    }

    private fun setDeviceId() {
        deviceId = repository.getDeviceId() as MutableLiveData<DeviceModel>
    }

    private fun setReceivedPayload() {
        payloadReceived = repository.getReceivedPayload() as MutableLiveData<PayloadModel>
    }

}