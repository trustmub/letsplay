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
import kotlin.random.Random
import kotlin.random.nextInt


class MainFragment : BaseFragment() {

    private lateinit var resolveListener: NsdManager.ResolveListener
    private val tiltViewModel: TiltViewModel by viewModel()
    var connectionValues: ConnectionModel? = null
    var socket: Socket? = null
    val countDown: MutableLiveData<Int> = MutableLiveData()
    val countdownService = Executors.newSingleThreadScheduledExecutor()
    val gameService = Executors.newSingleThreadScheduledExecutor()
    var initialCount = 0

    var randomArrowInterval = 0
    val startGameObserver: MutableLiveData<Int> = MutableLiveData()
    lateinit var arrowValue: String
    var totalScoreA = 0
    var totalScoreb = 0

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
                countdownService.shutdown()

                initialiseArrowValue()
                generateRandomSeconds()
                startGame(null)

                view.tv_counter.visibility = View.GONE
                view.tv_direction.visibility = View.VISIBLE
                view.tv_show_xy.visibility = View.VISIBLE
            }
        })

        startGameObserver.observe(this, Observer {
            println("Event randomInterval: $randomArrowInterval")
            println("Event It: $it")
            if (it == randomArrowInterval) {
                // display the arrow
                view.tv_arrow_value.text = arrowValue
                // set the arrow value
                gameService.shutdown()
            }
        })

        return view
    }

    private fun startCountDown(count: Int?) {
        val runnable = Runnable { countDown.postValue(1) }
        countdownService.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS)
    }


    @SuppressLint("SetTextI18n")
    private fun initializeTiltViewModel(view: View) {
        tiltViewModel.getAccelerometerData().observe(this, Observer {
            view.tv_show_xy.text = "x : ${it.x.roundByTwoDecimals()} y : ${it.y.roundByTwoDecimals()}"

            val x = it.x.roundByTwoDecimals()
            val y = it.y.roundByTwoDecimals()

            if (x in -4.0..4.0 && y in -4.0..4.0) {
                tv_direction.text = "Neutral"
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
        println("Event: $data")
        println("Event arrowValue: $arrowValue")
        if (data == arrowValue) setScoreFor("Passed") else setScoreFor("Failed")

        socket?.let {
            it.emit("event", data)
        }
    }

    private fun setScoreFor(result: String) {
        totalScoreA += if (result == "Passed") 1 else 0
        initialiseArrowValue()
        generateRandomSeconds()
        startGame(null)
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

    private fun startGame(count: Int?) {
        var internalCounter = 0
        val runnable = Runnable {
            internalCounter += 1
            startGameObserver.postValue(internalCounter)
        }
        gameService.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS)
    }

    private fun initialiseArrowValue(): String {
        // random arrow value
        //random global arrow value
        arrowValue = "Left"
        return arrowValue

    }

    private fun generateRandomSeconds() {
        val randInt = Random.nextInt(2..8)
        randomArrowInterval = randInt

        println("NextInt: $randInt")
        val directionList = listOf<String>("Left", "Right", "Forward", "Back")
        val timer: Timer = Timer(true)

    }
}
