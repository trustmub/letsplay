package com.trustathanas.letsplay.activities

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.skyfishjy.library.RippleBackground
import com.trustathanas.letsplay.R
import com.trustathanas.letsplay.activities.common.BaseFragment
import com.trustathanas.letsplay.utilities.dialogs.DialogUtils
import com.trustathanas.letsplay.viewModels.NearbyViewModel
import kotlinx.android.synthetic.main.fragment_nearby.view.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*


class NearbyFragment : BaseFragment() {
    val TAG = this.javaClass.name

    private val nearbyViewModel: NearbyViewModel by viewModel()
    private lateinit var payloadCallback: PayloadCallback
    private lateinit var id: String


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_nearby, container, false)

        val connectionClient = nearbyViewModel.getConnectionClient()

        nearbyViewModel.startAdvertisingService()
        nearbyViewModel.startDiscoveryService()

        val rippleBackground = view.findViewById(R.id.content_2) as RippleBackground
        rippleBackground.startRippleAnimation()

        if (!checkForPermission()) {
            requestPermission()
        }

        nearbyViewModel.getReceivedPayload().observe(this, Observer {
            view.tv_results.text = it.payload.asBytes().toString()
        })

        nearbyViewModel.getConnectionStatus().observe(this, Observer {
            if (it) {
                view.tv_results.text = "Connection Established"
                DialogUtils.createAlertDialogWithTwoButtons(
                    activity!!,
                    "Connection Established",
                    "Please click continue when you ready to play.",
                    "Continue",
                    DialogInterface.OnClickListener { dialog, _ ->
                        startActivity(Intent(activity!!, MainActivity::class.java))
                    },
                    "Cancel",
                    DialogInterface.OnClickListener { dialog, _ ->
                        dialog.dismiss()
                    }
                ).show()

                println("Nearby: Countdoen must start heare")

            }
        })

        nearbyViewModel.getDeviceId().observe(this, Observer {
            id = it.deviceId
            connectionClient.sendPayload(it.deviceId, Payload.fromBytes("this is it".toByteArray()))

        })
        return view
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