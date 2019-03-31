package com.trustathanas.letsplay.utilities
/**
 * Extension function for rounding the sensor data values to 2 decimal places
 */
fun Float.roundByTwoDecimals(): String {
    return "%.2f".format(this)
}

fun String.convertsToPercentage(): Int {
    return (this.toDouble() * 100).toInt()
}