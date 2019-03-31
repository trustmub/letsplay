package com.trustathanas.letsplay.activities

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.trustathanas.letsplay.R
import com.trustathanas.letsplay.activities.common.BaseFragment
import com.trustathanas.letsplay.utilities.convertsToPercentage
import com.trustathanas.letsplay.utilities.roundByTwoDecimals
import com.trustathanas.letsplay.viewModels.TiltViewModel
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import org.koin.android.viewmodel.ext.android.viewModel


class MainFragment : BaseFragment() {

    private val tiltViewModel: TiltViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        this.initializeTiltViewModel(view)
        return view
    }


    @SuppressLint("SetTextI18n")
    private fun initializeTiltViewModel(view: View) {
        tiltViewModel.getAccelerometerData().observe(this, Observer {
            view.textView.text = "x : ${it.x.roundByTwoDecimals()} y : ${it.y.roundByTwoDecimals()}"

            val x = it.x.roundByTwoDecimals()
            val y = it.y.roundByTwoDecimals()

            if (x.convertsToPercentage() in -300..300 && y.convertsToPercentage() in -300..300) tv_direction.text =
                "Neutral"

            if (x.convertsToPercentage() > 300) tv_direction.text = "Left"
            if (x.convertsToPercentage() < -300) tv_direction.text = "Right"

            if (y.convertsToPercentage() < -300) tv_direction.text = "Forward"
            if (y.convertsToPercentage() > 300) tv_direction.text = "Back"
        })
    }
}