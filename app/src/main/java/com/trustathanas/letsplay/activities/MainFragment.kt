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
//        this.initializeTiltViewModel(view)

        view.tv_direction.visibility = View.GONE
        view.tv_show_xy.visibility = View.GONE
        view.btn_restart_game.visibility = View.GONE

        view.btn_restart_game.setOnClickListener {
            resetGame()
            it.visibility = View.GONE
        }

        connectionValues = activity!!.intent.getParcelableExtra(EXTRA_CONNECTION_DETAILS)
        connectionValues?.let {
            val host = "http://${it.ipAddress}:${it.port}"
            socket = IO.socket(host)
            initialiseSocketListener()
        }

        startCountDown()

        socket?.on("event") {
            println("Event: $it")
        }

        initialiseGameObservers()

        return view
    }

    private fun initialiseGameObservers() {
//        btn_restart_game.visibility = View.GONE

        countDown.observe(this, Observer {
            tv_counter.text = initialCount.toString()
            initialCount += it

            if (initialCount > 4) {

                initializeTiltViewModel()
                initialiseArrowValue()
                generateRandomSeconds()
                startGame()

                tv_counter.visibility = View.GONE
                tv_direction.visibility = View.VISIBLE
                tv_show_xy.visibility = View.VISIBLE
            }
        })

        startGameObserver.observe(this, Observer {
            if (it == randomArrowInterval) {
                // display the arrow
                tv_arrow_value.text = arrowValue
                // set the arrow value
                //                gameService.shutdown()
            }
        })
    }

    private fun startCountDown() {
        val runnable = Runnable { countDown.postValue(1) }
        countdownService.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS)
    }

    @SuppressLint("SetTextI18n")
    private fun initializeTiltViewModel() {
        tiltViewModel.getAccelerometerData().observe(this, Observer {
            tv_show_xy.text = "x : ${it.x.roundByTwoDecimals()} y : ${it.y.roundByTwoDecimals()}"

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
            if (y > 4.00) {
                tv_direction.text = "Back"
                sendEvent("Back")
            }
        })
    }

    private fun sendEvent(data: String) {
        if (data == arrowValue) setScoreFor("Passed") else setScoreFor("Failed")

        socket?.let {
            it.emit("event", data)
        }
    }

    private fun setScoreFor(result: String) {
        totalScoreA += if (result == "Passed") 1 else 0
        tv_a_score.text = totalScoreA.toString()
        if (totalScoreA == 10) {
            stopTiltObserver()
            btn_restart_game.visibility = View.VISIBLE
        } else {
            tv_arrow_value.text = "-"
            initialiseArrowValue()
            generateRandomSeconds()
            startGame()
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

    private fun startGame() {
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
        tv_arrow_value.text = "-"
        val directionList = listOf<String>("Left", "Right", "Back", "Forward")
        arrowValue = directionList.random()
        return arrowValue

    }

    private fun generateRandomSeconds() {
        val randInt = Random.nextInt(2..8)
        randomArrowInterval = randInt
    }

    private fun stopTiltObserver() {
        startGameObserver.removeObservers(this)
        countDown.removeObservers(this)
        tiltViewModel.getAccelerometerData().removeObservers(this)
    }

    private fun resetGame() {
        totalScoreA = 0
        tv_a_score.text = totalScoreA.toString()
        totalScoreb = 0
        tv_b_score.text = totalScoreb.toString()
        initialiseGameObservers()
    }
}
