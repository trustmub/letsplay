package com.trustathanas.letsplay.utilities

import kotlin.math.roundToLong

/**
 * Extension function for rounding the sensor data values to 2 decimal places
 */
fun Float.roundByTwoDecimals(): Double {
    return "%.5f".format(this).toDouble()
}

fun String.convertsToPercentage(): Int {
    return (this.toDouble() * 100).toInt()
}