package com.trustathanas.letsplay.activities

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.trustathanas.letsplay.R
import com.trustathanas.letsplay.activities.common.BaseFragment
import com.trustathanas.letsplay.utilities.dialogs.DialogUtils
import kotlinx.android.synthetic.main.fragment_pair.*
import kotlinx.android.synthetic.main.fragment_pair.view.*
import java.net.ServerSocket


class PairFragment : BaseFragment() {
    val SERVICE_TYPE = "_http._tcp."
    val SERVICE_NAME = "Trust's Phone"
    lateinit var nsdManager: NsdManager
    lateinit var serverSocket: ServerSocket
    lateinit var registrationListener: NsdManager.RegistrationListener
    lateinit var discovertListener: NsdManager.DiscoveryListener
    lateinit var resolveListener: NsdManager.ResolveListener
    var discoveredServices: ArrayList<NsdServiceInfo> = arrayListOf()
    lateinit var serviceName: String
    var localPort: Int = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pair, container, false)
        if (checkForPermission()) {
            initializeServerSocket()
            initializeRegistrationListener()
            registerService()

            initializeDiscoveryListener()
            discoverService()

        } else {
            requestPermission()
        }


        view.btn_show_dialog.setOnClickListener {
            showDialog()
        }
        return view
    }


    /**
     * initialise a server socket on the next available port
     */
    private fun initializeServerSocket() {
        serverSocket = ServerSocket(0)
        localPort = serverSocket.localPort
    }

    /**
     * register service on local network
     */
    private fun registerService() {
        val serviceInfo: NsdServiceInfo = NsdServiceInfo()
        serviceInfo.serviceName = SERVICE_NAME
        serviceInfo.serviceType = SERVICE_TYPE
        serviceInfo.port = localPort
        println("NSD: ServiceName: ${serviceInfo.serviceName}")
        println("NSD: LocalPort: ${localPort}")
        println("NSD: Registration Listener: ${registrationListener}")

        nsdManager = activity!!.getSystemService(Context.NSD_SERVICE) as NsdManager

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

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

            }

            override fun onServiceRegistered(serviceInfo: NsdServiceInfo?) {
                println("NSD: onServiceRegistered ${serviceInfo}")

                discoveredServices?.let {
                    it.forEach {
                        println("NSD: Discovered items : ${it.host}")
                    }
                }

                activity!!.runOnUiThread(Runnable {
                    tv_search_devices.text = "Service Started"
                })

                serviceInfo?.let {
                    serviceName = it.serviceName
                }
            }

        }
    }

    private fun discoverService() {
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discovertListener)
    }

    private fun initializeDiscoveryListener() {
        discovertListener = object : NsdManager.DiscoveryListener {

            override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
                serviceInfo?.let { service ->
                    if (!service.serviceType.equals(SERVICE_TYPE)) {
                        println("NSD: Unknown Service Type")
                    } else if (service.serviceName.equals(SERVICE_NAME)) {
                        println("NSD: Same machine")
                        activity!!.runOnUiThread {
                            tv_search_devices.text = "Same machine: ${service.serviceName}"
                        }
                    } else {
                        println("NSD: service added: $service")
                        activity!!.runOnUiThread {
                            tv_search_devices.text = "Other ${service.serviceName}"
                        }

                        discoveredServices.add(service)
                    }
                }
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                println("NSD: Discovery Failed ${errorCode}")
            }

            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                println("NSD: onStartDiscoveryFailed ErrorCode: ${errorCode}, Service Type ${serviceType}")

                DialogUtils.createAlertDialogWithTwoButtons(activity!!,
                    "Failed",
                    "Discovery Failed for Service Type ${serviceType} with error code : $errorCode",
                    "Continue",
                    DialogInterface.OnClickListener { dialog, which ->
                        startActivity(Intent(activity!!, MainActivity::class.java))
                    },
                    "Cancel",
                    DialogInterface.OnClickListener { dialog, which ->
                        try {
                            nsdManager.stopServiceDiscovery(discovertListener)
                            nsdManager.unregisterService(registrationListener)
                        } catch (e: IllegalArgumentException) {
                            println("NSD: Exception Error: $e")
                        }
                        dialog.dismiss()
                    }).show()
            }

            override fun onDiscoveryStarted(serviceType: String?) {
                println("NSD: onDiscoveryStarted ${serviceType}")
                activity!!.runOnUiThread {
                    tv_search_devices.text = "Discovery Started"
                }
            }

            override fun onDiscoveryStopped(serviceType: String?) {
                println("NSD: onDiscoveryStopped ${serviceType}")
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
                discoveredServices.remove(serviceInfo)
                println("NSD: onDiscoveryStarted ${serviceInfo}")
            }

        }
    }


    private fun initialiseResolveListener() {
        resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {

            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
                DialogUtils.createAlertDialogWithTwoButtons(activity!!,
                    "All Set",
                    "Click Continue to play with ${serviceInfo?.host?.hostName}",
                    "Continue",
                    DialogInterface.OnClickListener { dialog, which ->
                        startActivity(Intent(activity!!, MainActivity::class.java))
                    },
                    "Cancel",
                    DialogInterface.OnClickListener { dialog, which ->
                        nsdManager.stopServiceDiscovery(discovertListener)
                        nsdManager.unregisterService(registrationListener)
                        dialog.dismiss()
                    }).show()
            }

        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            activity!!,
            arrayOf(
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.CHANGE_WIFI_MULTICAST_STATE
            ),
            1
        )
    }

    private fun checkForPermission(): Boolean {
        val externalStorage = ContextCompat.checkSelfPermission(
            activity!!.applicationContext,
            Manifest.permission.INTERNET
        )
        val cameraPermission =
            ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.ACCESS_NETWORK_STATE)
        val wifi_multicast = ContextCompat.checkSelfPermission(
            activity!!.applicationContext,
            Manifest.permission.CHANGE_WIFI_MULTICAST_STATE
        )

        return externalStorage == PackageManager.PERMISSION_GRANTED && cameraPermission == PackageManager.PERMISSION_GRANTED && wifi_multicast == PackageManager.PERMISSION_GRANTED
    }


    private fun showDialog() {
        DialogUtils.createAlertDialogWithTwoButtons(
            activity!!,
            "Ooops",
            "Seems there is no device to Pair.Click Try to search again or Continue to Single player Mode",
            "Continue",
            DialogInterface.OnClickListener { _, _ ->
                startActivity(Intent(activity, MainActivity::class.java))
            },
            "Try",
            DialogInterface.OnClickListener { dialog, _ ->
                dialog.dismiss()
            }
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        nsdManager.stopServiceDiscovery(discovertListener)
        nsdManager.unregisterService(registrationListener)
    }
}