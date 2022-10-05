package com.orhan.extensions

import java.math.RoundingMode
import java.text.DecimalFormat


fun Double.roundOffDecimal(): Double {
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.CEILING
    return df.format(this).toDouble()
}

fun Float.roundOffDecimal(): Float {
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.CEILING
    return df.format(this).toFloat()
}

fun Float.roundOffDecimalTo3Places(): Float {
    val df = DecimalFormat("#.###")
    df.roundingMode = RoundingMode.CEILING
    return df.format(this).toFloat()
}