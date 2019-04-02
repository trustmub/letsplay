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
import androidx.lifecycle.Observer
import com.skyfishjy.library.RippleBackground
import com.trustathanas.letsplay.R
import com.trustathanas.letsplay.activities.common.BaseFragment
import com.trustathanas.letsplay.models.ConnectionModel
import com.trustathanas.letsplay.utilities.EXTRA_CONNECTION_DETAILS
import com.trustathanas.letsplay.utilities.SERVICE_TYPE
import com.trustathanas.letsplay.utilities.dialogs.DialogUtils
import com.trustathanas.letsplay.viewModels.PairViewModel
import kotlinx.android.synthetic.main.fragment_pair.*
import kotlinx.android.synthetic.main.fragment_pair.view.*
import org.koin.android.viewmodel.ext.android.viewModel


class PairFragment : BaseFragment() {

    private val pairViewModel: PairViewModel by viewModel()
    var discoveredServices: ArrayList<NsdServiceInfo> = ArrayList()
    lateinit var discoveryListener: NsdManager.DiscoveryListener
    lateinit var registrationListener: NsdManager.RegistrationListener

    lateinit var nsdManager: NsdManager

    lateinit var resolveListener: NsdManager.ResolveListener
    lateinit var serviceInfoExtra: NsdServiceInfo


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pair, container, false)

        view.img_discovered_device_1.visibility = View.GONE
        view.tv_guest_name.visibility = View.GONE
        discoveryListener = pairViewModel.getDiscoveryListener()
        registrationListener = pairViewModel.getRegistrationListener()

        view.img_discovered_device_1.setOnClickListener {
            nsdManager.resolveService(serviceInfoExtra, resolveListener)
//            val deviceIntent = Intent(activity!!, MainActivity::class.java)
//            deviceIntent.putExtra(SERVICE_INFO_EXTRA, NsdServiceIInfoParcelable(serviceInfoExtra))
//            startActivity(deviceIntent)
        }

        val rippleBackground = view.findViewById(R.id.content) as RippleBackground
        initialiseListenerStateObservers(rippleBackground)

        /**
         * check if device has required permision before initialising NsdManager
         */
        if (checkForPermission()) {
            registerService()
            initializeDiscoveryService()
            initialiseResolveListener()
        } else {
            requestPermission()
        }

        pairViewModel.getElementToRemove().observe(this, Observer {
            discoveredServices.remove(it.element)
            activity!!.runOnUiThread {
                img_discovered_device_1.visibility = View.GONE
                tv_guest_name.visibility = View.GONE
            }
        })

        initialiseDiscoveryServicesObserver()

        view.btn_show_dialog.setOnClickListener {
            showDialog()
        }


        return view
    }

    /**
     * This method initialises an observer which subscribes to an array of NsdService?Info.
     *
     */
    private fun initialiseDiscoveryServicesObserver() {
        pairViewModel.getDiscoveredServices()?.observe(this, Observer {
            it.forEach {
                println("NSD: discovered : ${it.serviceName}")
                discoveredServices.add(it)
                activity!!.runOnUiThread {
                    img_discovered_device_1.visibility = View.VISIBLE
                    tv_guest_name.visibility = View.VISIBLE
                    tv_guest_name.text = it.serviceName
                    serviceInfoExtra = it
                }
            }
        })
    }

    /**
     * This method initialises the state observers for registration and discovery listeners to determine of the
     * listeners started successfully or not.
     * if there is a failed start up, a dialog alert to start the game in single player mode is presented
     */
    private fun initialiseListenerStateObservers(rippleBackground: RippleBackground) {
        pairViewModel.getRegistrationState().observe(this, Observer {
            if (it) {
                tv_search_devices.text = getString(R.string.service_started)
                rippleBackground.startRippleAnimation()
            } else {
                showDialogForSinglePlayer(
                    getString(R.string.title_failed),
                    getString(R.string.message_registration_failed)
                )
            }
        })

        pairViewModel.getDiscoveryState().observe(this, Observer {
            if (!it) {
                showDialogForSinglePlayer(
                    getString(R.string.title_failed),
                    getString(R.string.message_discovery_failed)
                )
            }
        })
    }

    /**
     * register service on local network
     */
    private fun registerService() {
        val serviceInfo = pairViewModel.getServiceInfoForLocalNet()
        nsdManager = activity!!.getSystemService(Context.NSD_SERVICE) as NsdManager
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    /**
     * initialise the network service discovery service
     */
    private fun initializeDiscoveryService() {
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    private fun initialiseResolveListener() {
        resolveListener = object : NsdManager.ResolveListener {

            override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
                println("NSD: Resolved service: ${serviceInfo}")
                val cinnectionDetails: ConnectionModel =
                    ConnectionModel(serviceInfo!!.host!!.hostAddress, serviceInfo!!.port)

                DialogUtils.createAlertDialogWithTwoButtons(activity!!,
                    "All Set",
                    "Click Continue to play with ${serviceInfo?.host?.hostName} port: ${serviceInfo?.port}",
                    "Continue",
                    DialogInterface.OnClickListener { dialog, which ->
                        val intent = Intent(activity!!, MainActivity::class.java)
                        intent.putExtra(EXTRA_CONNECTION_DETAILS, cinnectionDetails)
                        startActivity(intent)
                    },
                    "Cancel",
                    DialogInterface.OnClickListener { dialog, which ->
                        nsdManager.stopServiceDiscovery(discoveryListener)
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
        val internet = ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.INTERNET)
        val accessNetworkSate =
            ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.ACCESS_NETWORK_STATE)
        val multicastState = ContextCompat.checkSelfPermission(
            activity!!.applicationContext, Manifest.permission.CHANGE_WIFI_MULTICAST_STATE
        )

        return internet == PackageManager.PERMISSION_GRANTED && accessNetworkSate == PackageManager.PERMISSION_GRANTED && multicastState == PackageManager.PERMISSION_GRANTED
    }

    private fun showDialog() {
        DialogUtils.createAlertDialogWithTwoButtons(
            activity!!,
            "Client/Single Payer",
            "Click Continue for single player Mode or connect to Host.Click Try to search again or Continue to Single player Mode",
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

    /**
     * Dialog method to present a single player option
     *
     * @param title
     * @param message
     */
    private fun showDialogForSinglePlayer(title: String, message: String) {
        DialogUtils.createAlertDialogWithTwoButtons(activity!!,
            title,
            message,
            "Continue",
            DialogInterface.OnClickListener { dialog, which ->
                startActivity(Intent(activity!!, MainActivity::class.java))
            },
            "Cancel",
            DialogInterface.OnClickListener { dialog, which ->
                try {
                    nsdManager.stopServiceDiscovery(discoveryListener)
                    nsdManager.unregisterService(registrationListener)
                } catch (e: IllegalArgumentException) {
                    println("NSD: Exception Error: $e")
                }
                registerService()
                initializeDiscoveryService()

                dialog.dismiss()
            }).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        nsdManager.stopServiceDiscovery(discoveryListener)
        nsdManager.unregisterService(registrationListener)
    }
}