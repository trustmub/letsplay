package com.trustathanas.letsplay.activities

import android.annotation.SuppressLint
import android.os.Bundle
import com.trustathanas.letsplay.R
import com.trustathanas.letsplay.activities.common.BaseActivity


class MainActivity : BaseActivity() {
//    var timer: Timer

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

