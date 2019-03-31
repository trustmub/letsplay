package com.trustathanas.letsplay.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.trustathanas.letsplay.models.AccelerometerModel
import com.trustathanas.letsplay.repositories.TiltRepository

class TiltViewModel(private val repository: TiltRepository) : ViewModel() {

    private lateinit var accelerometerData: MutableLiveData<AccelerometerModel>
    init {
        this.setAccelerometerData()
    }

    fun getAccelerometerData(): LiveData<AccelerometerModel> = accelerometerData

    private fun setAccelerometerData(){
        accelerometerData = repository.getAccelerometerValues() as MutableLiveData<AccelerometerModel>
    }

}