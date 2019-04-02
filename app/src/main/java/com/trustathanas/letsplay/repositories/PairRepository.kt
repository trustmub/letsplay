package com.trustathanas.letsplay.repositories

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.trustathanas.letsplay.models.ElementModel
import com.trustathanas.letsplay.utilities.SERVICE_NAME
import com.trustathanas.letsplay.utilities.SERVICE_TYPE
import kotlinx.android.synthetic.main.fragment_pair.*

class PairRepository {

    private var removeElement: MutableLiveData<ElementModel> = MutableLiveData()
    private lateinit var discoveryListener: NsdManager.DiscoveryListener
    private lateinit var registrationListener: NsdManager.RegistrationListener
    private var discoveredServices: MutableLiveData<ArrayList<NsdServiceInfo>>? = MutableLiveData()
    private var discoveryState: MutableLiveData<Boolean> = MutableLiveData()
    private var registrationState: MutableLiveData<Boolean> = MutableLiveData()

    init {
        initializeRegistrationListener()
        initializeDiscoveryListener()
    }

    /**
     * Discovery methods section
     */
    fun getDiscoveredServices(): MutableLiveData<ArrayList<NsdServiceInfo>>? {
        return discoveredServices
    }

    fun getDiscoveryListener(): NsdManager.DiscoveryListener {
        return discoveryListener
    }

    fun getDiscoveryState(): LiveData<Boolean> {
        return discoveryState
    }

    fun getElementToRemove(): LiveData<ElementModel> {
        return removeElement
    }

    /**
     * Registration methods section
     */
    fun getRegistrationListener(): NsdManager.RegistrationListener {
        return registrationListener
    }

    fun getRegistrationState(): LiveData<Boolean> {
        return registrationState
    }


    /**
     * initialisation of the Netowk countdownService Discovery Registration listener
     */
    private fun initializeRegistrationListener() {

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                println("NSD: onUnregistrationFailed ${serviceInfo} Error: $errorCode")

            }

            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {
                println("NSD: onServiceUnregistered ${serviceInfo} ")
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                println("NSD: onRegistrationFailed ${serviceInfo} Error: $errorCode")
                registrationState.postValue(false)
            }

            override fun onServiceRegistered(serviceInfo: NsdServiceInfo?) {
                println("NSD: onServiceRegistered ${serviceInfo}")
                registrationState.postValue(true)
            }

        }
    }


    /**
     * Initialisation of the Network countdownService Discovery Discovery Listener
     */
    private fun initializeDiscoveryListener() {
        discoveryListener = object : NsdManager.DiscoveryListener {

            override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
                serviceInfo?.let { service ->
                    if (!service.serviceType.equals(SERVICE_TYPE)) {
                        println("NSD: Unknown Service Type")
                    } else if (service.serviceName.equals(SERVICE_NAME)) {
                        println("NSD: Same machine")
                    } else {
                        println("NSD: countdownService added: $service")
                        discoveredServices!!.postValue(arrayListOf(service))
                    }
                }
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                println("NSD: Discovery Failed ${errorCode}")
            }

            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                println("NSD: onStartDiscoveryFailed ErrorCode: ${errorCode}, Service Type ${serviceType}")
                discoveryState.value = false
            }

            override fun onDiscoveryStarted(serviceType: String?) {
                println("NSD: onDiscoveryStarted ${serviceType}")
            }

            override fun onDiscoveryStopped(serviceType: String?) {
                println("NSD: onDiscoveryStopped ${serviceType}")
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
                println("NSD: onServiceLost ${serviceInfo}")
                discoveredServices
                removeElement.postValue(ElementModel(serviceInfo!!))
            }

        }
    }
}