package com.trustathanas.letsplay.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.trustathanas.letsplay.R
import com.trustathanas.letsplay.activities.common.BaseFragment
import com.trustathanas.letsplay.utilities.SERVICE_ID
import com.trustathanas.letsplay.utilities.SERVICE_NAME
import com.trustathanas.letsplay.viewModels.NearbyViewModel
import kotlinx.android.synthetic.main.fragment_nearby.*
import kotlinx.android.synthetic.main.fragment_nearby.view.*
import org.koin.android.viewmodel.ext.android.viewModel

class NearbyFragment : BaseFragment() {
    val TAG = this.javaClass.name

    private val nearbyViewModel: NearbyViewModel by viewModel()
    private var connectionLifecycleCallback: ConnectionLifecycleCallback? = null
    private var endpointDiscoveryCallback: EndpointDiscoveryCallback? = null
    private lateinit var payloadCallback: PayloadCallback


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_nearby, container, false)
        initialiseConnectionLifecycleCallback()
        initialiseEndpointDiscoveryCallback()

        if (!checkForPermission()) {
            requestPermission()
        }
        view.btn_advertise.setOnClickListener {
            startAdvertising()
        }

        view.btn_discover.setOnClickListener {
            val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
            Nearby.getConnectionsClient(activity!!)
                .startDiscovery(SERVICE_ID, endpointDiscoveryCallback!!, discoveryOptions)
            println("Ednpint descovery Endpoint: $endpointDiscoveryCallback")
        }
        return view
    }

    private fun initialiseConnectionLifecycleCallback() {
        connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
            override fun onConnectionResult(endpointId: String, conntetionResolution: ConnectionResolution) {
                Log.d(TAG, "onConnectionResult: Success : $endpointId")
                println("onConnectionResult: Success : $endpointId")
            }

            override fun onDisconnected(endpointId: String) {
                Log.d(TAG, "onDisconnected: Lost a device $endpointId")
                println("onDisconnected: Lost a device $endpointId")
            }

            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                // Automatically accept the connection on both sides.
                Log.d(TAG, "startAdvertising: Connected to $endpointId")
                Nearby.getConnectionsClient(activity!!).acceptConnection(endpointId, payloadCallback)
            }

        }
    }

    private fun initialisePayloadCallback() {
        payloadCallback = object : PayloadCallback() {
            override fun onPayloadReceived(p0: String, p1: Payload) {
            }

            override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun startAdvertising() {

        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        Nearby.getConnectionsClient(activity!!)
            .startAdvertising(SERVICE_NAME, SERVICE_ID, connectionLifecycleCallback!!, advertisingOptions).addOnSuccessListener {
                tv_results.text = "Started"
            }
            .addOnFailureListener {
                tv_results.text = "failed: $it"
            }
        println("Connection Lifesycler: $connectionLifecycleCallback")

    }

    private fun initialiseEndpointDiscoveryCallback() {
        endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, endpointInfo: DiscoveredEndpointInfo) {
                // requesting a connection to the first found device
                connectionLifecycleCallback?.let {
                    Log.d(TAG, "Connection lifesycle found endpoint")
                    println("Connection lifesycle found endpoint\"")
                    Nearby.getConnectionsClient(activity!!)
                        .requestConnection(SERVICE_NAME, endpointId, it)
                        .addOnCanceledListener { }
                        .addOnFailureListener { }
                }

            }

            override fun onEndpointLost(endpointId: String) {
                println("connection to device lost $endpointId")
            }

        }
    }


    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            activity!!,
            arrayOf(
                Manifest.permission.INTERNET,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1
        )
    }

    private fun checkForPermission(): Boolean {
        val internet = ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.INTERNET)
        val bluetooth = ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.BLUETOOTH)
        val bluetoothAdmin =
            ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.BLUETOOTH_ADMIN)
        val accessWifiSate =
            ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.ACCESS_WIFI_STATE)
        val changeWifiSate =
            ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.CHANGE_WIFI_STATE)
        val accessCoarseLocation =
            ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION)

        return internet == PackageManager.PERMISSION_GRANTED &&
                bluetooth == PackageManager.PERMISSION_GRANTED &&
                bluetoothAdmin == PackageManager.PERMISSION_GRANTED &&
                accessWifiSate == PackageManager.PERMISSION_GRANTED &&
                changeWifiSate == PackageManager.PERMISSION_GRANTED &&
                accessCoarseLocation == PackageManager.PERMISSION_GRANTED
    }
}