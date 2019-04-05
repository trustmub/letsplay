package com.trustathanas.letsplay.activities

import android.annotation.SuppressLint
import android.net.nsd.NsdManager
import android.os.Bundle
import android.util.ArraySet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.trustathanas.letsplay.R
import com.trustathanas.letsplay.activities.common.BaseFragment
import com.trustathanas.letsplay.models.ConnectionModel
import com.trustathanas.letsplay.utilities.EXTRA_CONNECTION_DETAILS
import com.trustathanas.letsplay.utilities.roundByTwoDecimals
import com.trustathanas.letsplay.viewModels.NearbyViewModel
import com.trustathanas.letsplay.viewModels.TiltViewModel
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlinx.android.synthetic.main.fragment_nearby.view.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.random.nextInt


class MainFragment : BaseFragment() {

    private val tiltViewModel: TiltViewModel by viewModel()
    private val nearbyViewModel: NearbyViewModel by viewModel()
    private lateinit var id: String
    private lateinit var connectionClient: ConnectionsClient


    private lateinit var resolveListener: NsdManager.ResolveListener
    var connectionValues: ConnectionModel? = null
    val countDown: MutableLiveData<Int> = MutableLiveData()
    val scoreObserver: MutableLiveData<Int> = MutableLiveData()
    val countdownService = Executors.newSingleThreadScheduledExecutor()
    val gameService = Executors.newSingleThreadScheduledExecutor()
    var initialCount = 0

    var randomArrowInterval = 0
    val startGameObserver: MutableLiveData<Int> = MutableLiveData()
    lateinit var arrowValue: String
    var totalScoreA = 0
    var totalScoreB = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        view.tv_direction.visibility = View.GONE
        view.tv_show_xy.visibility = View.GONE
        view.btn_restart_game.visibility = View.GONE

        connectionClient = nearbyViewModel.getConnectionClient()
        initialiseNearbyObservers()

        view.btn_restart_game.setOnClickListener {
            resetGame()
            it.visibility = View.GONE
        }


        startCountDown()

        initialiseGameObservers()

        return view
    }

    private fun initialiseNearbyObservers() {

        nearbyViewModel.getReceivedPayload().observe(this, Observer {
            Toast.makeText(activity!!, it.endpintId, Toast.LENGTH_SHORT).show()
            tv_show_xy.text = Arrays.toString(it.payload.asBytes())
        })


        nearbyViewModel.getDeviceId().observe(this, Observer {
            id = it.deviceId
            connectionClient.sendPayload(it.deviceId, Payload.fromBytes("Connected".toByteArray()))

        })
    }

    private fun initialiseGameObservers() {

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
            }
        })
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
        stopTiltObserver()
        if (data == arrowValue) setScoreFor("Passed") else setScoreFor("Failed")
        connectionClient.sendPayload(id, Payload.fromBytes(data.toByteArray()))
    }

    private fun setScoreFor(result: String) {
        if (result == "Passed") {
            totalScoreA += 1

        }
        if (result == "Failed") {
            totalScoreA += 0
        }

        tv_a_score.text = totalScoreA.toString()
        if (totalScoreA == 10) {
            stopTiltObserver()
            btn_restart_game.visibility = View.VISIBLE
        } else {
            tv_arrow_value.text = "-"
            initialiseGameObservers()
            initialiseArrowValue()
            generateRandomSeconds()
            startGame()
        }
    }

    private fun startCountDown() {
        val runnable = Runnable { countDown.postValue(1) }
        countdownService.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS)
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
        randomArrowInterval = Random.nextInt(2..8)
    }

    private fun stopTiltObserver() {
        tiltViewModel.getAccelerometerData().removeObservers(this)
        startGameObserver.removeObservers(this)
        countDown.removeObservers(this)
    }

    private fun resetGame() {
        totalScoreA = 0
        tv_a_score.text = totalScoreA.toString()
        totalScoreB = 0
        tv_b_score.text = totalScoreB.toString()
        initialiseGameObservers()

        initializeTiltViewModel()
        initialiseArrowValue()
        generateRandomSeconds()
        startGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionClient.stopDiscovery()
        connectionClient.stopAdvertising()
    }
}
