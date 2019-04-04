package com.trustathanas.letsplay.viewModels

import androidx.lifecycle.ViewModel
import com.trustathanas.letsplay.repositories.NearbyRepository

class NearbyViewModel(private val repository: NearbyRepository) : ViewModel() {

    fun startAdvertisingService() = repository.startAdvertisingService()
    fun startDiscoveryService() = repository.startDiscoveryService()

    fun stopAdvertisingService() = repository.stopAdvertisingService()
    fun stopDiscoveryService() = repository.stopDiscoveryService()

}