package com.trustathanas.letsplay.activities

import android.annotation.SuppressLint
import android.net.nsd.NsdManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.trustathanas.letsplay.R
import com.trustathanas.letsplay.activities.common.BaseFragment
import com.trustathanas.letsplay.models.ConnectionModel
import com.trustathanas.letsplay.utilities.EXTRA_CONNECTION_DETAILS
import com.trustathanas.letsplay.utilities.roundByTwoDecimals
import com.trustathanas.letsplay.viewModels.TiltViewModel
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class MainFragment : BaseFragment() {

    private lateinit var resolveListener: NsdManager.ResolveListener
    private val tiltViewModel: TiltViewModel by viewModel()
    var connectionValues: ConnectionModel? = null
    var socket: Socket? = null
    val countDown: MutableLiveData<Int> = MutableLiveData()
    val service = Executors.newSingleThreadScheduledExecutor()
    var initialCount = 0
//    val socket =  IO.socket

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        this.initializeTiltViewModel(view)

        view.tv_direction.visibility = View.GONE
        view.tv_show_xy.visibility = View.GONE

        connectionValues = activity!!.intent.getParcelableExtra(EXTRA_CONNECTION_DETAILS)
        connectionValues?.let {
            val host = "http://${it.ipAddress}:${it.port}"
            socket = IO.socket(host)
            initialiseSocketListener()
        }

        startCountDown(null)

        socket?.on("event") {
            println("Event: $it")
        }

        countDown.observe(this, Observer {
            view.tv_counter.text = initialCount.toString()
            initialCount += it

            if (initialCount > 4) {
                service.shutdown()
                view.tv_counter.visibility = View.GONE
                view.tv_direction.visibility = View.VISIBLE
                view.tv_show_xy.visibility = View.VISIBLE
            }
        })

        return view
    }

    private fun startCountDown(count: Int?) {
        val runnable = Runnable {
            countDown.postValue(1)
        }
        service.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS)
    }


    @SuppressLint("SetTextI18n")
    private fun initializeTiltViewModel(view: View) {
        tiltViewModel.getAccelerometerData().observe(this, Observer {
            view.tv_show_xy.text = "x : ${it.x.roundByTwoDecimals()} y : ${it.y.roundByTwoDecimals()}"

            val x = it.x.roundByTwoDecimals()
            val y = it.y.roundByTwoDecimals()

            if (x in -4.0..4.0 && y in -4.0..4.0) {
                tv_direction.text = "Neutral"
                sendEvent("Neutral")
            }

            if (x > 4.0) {
                tv_direction.text = "Left"
                sendEvent("Left")
            }
            if (x < -4.0) {
                tv_direction.text = "Right"
                sendEvent("Right")
            }

            if (y < -4.00) {
                tv_direction.text = "Forward"
                sendEvent("Forward")
            }
            if (y > 4.00) tv_direction.text = "Back"
        })
    }


    private fun sendEvent(data: String) {
        socket?.let {
            it.emit("event", data)
        }
    }

    private fun initialiseSocketListener() {
        if (socket!!.connected()) {
            println(" Event Connect")
        }
        socket!!.on(Socket.EVENT_CONNECT) {
            println("Socket EVENT_CONNECT: $it")
        }.on("event") {
            println("SOCKET event received: $it")
        }.on(Socket.EVENT_DISCONNECT) {
            println("Socket: disconnected")
        }
        socket?.connect()
    }


    override fun onDestroy() {
        super.onDestroy()
        socket?.disconnect()
    }

    private fun displayInRandomSeconds() {
        val directionList = listOf<String>("Left", "Right", "Forward", "Back")
        val timer: Timer = Timer(true)
//        timer.schedule(object : TimerTask() {
//            override fun run() {
//            }
//        }, 2000..10000)

    }
}
