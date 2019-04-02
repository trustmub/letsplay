package com.trustathanas.letsplay.viewModels

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.lifecycle.ViewModel
import com.trustathanas.letsplay.repositories.PairRepository
import com.trustathanas.letsplay.utilities.SERVICE_NAME
import com.trustathanas.letsplay.utilities.SERVICE_TYPE
import java.net.ServerSocket

class PairViewModel(private val repository: PairRepository) : ViewModel() {

    private val localPort = ServerSocket(0).localPort
    private val nsdServiceInfo = registerService()


    fun getDiscoveredServices() = repository.getDiscoveredServices()

    fun getDiscoveryListener() = repository.getDiscoveryListener()

    fun getDiscoveryState() = repository.getDiscoveryState()

    fun getElementToRemove() = repository.getElementToRemove()

    fun getRegistrationListener() = repository.getRegistrationListener()

    fun getRegistrationState() = repository.getRegistrationState()

    fun getServiceInfoForLocalNet() = nsdServiceInfo


    /**
     * register countdownService on local network
     */
    private fun registerService(): NsdServiceInfo {
        val serviceInfo: NsdServiceInfo = NsdServiceInfo()
        serviceInfo.serviceName = SERVICE_NAME
        serviceInfo.serviceType = SERVICE_TYPE
        serviceInfo.port = localPort

        return serviceInfo

//        nsdManager = activity!!.getSystemService(Context.NSD_SERVICE) as NsdManager
//
//        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }


}