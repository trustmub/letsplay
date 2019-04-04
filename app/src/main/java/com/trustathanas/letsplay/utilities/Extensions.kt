package com.trustathanas.letsplay.utilities

import kotlin.math.roundToLong

/**
 * Extension function for rounding the sensor data values to 2 decimal places
 */
fun Float.roundByTwoDecimals(): Double {
    return "%.2f".format(this).toDouble()
}

fun String.convertsToPercentage(): Int {
    return (this.toDouble() * 100).toInt()
}

fun <E> List<E>.random(random: java.util.Random): E? = if (size > 0) get(random.nextInt(size)) else null
