package com.orhan.calculations

import com.orhan.extensions.roundOffDecimal
import com.orhan.extensions.roundOffDecimalTo3Places
import com.orhan.parser.Price

fun calculate(price : Price): String{

    val diff = (price.close - price.open).roundOffDecimal()
    val diffInPercentage = ((price.close - price.open)/price.open).roundOffDecimal()

    val diffCloseChar = if (diff > 0) "▲" else if (diff < 0) "▼" else "▶"

    val diffHigh = ((price.high - price.open)).roundOffDecimal()
    val diffLow = ((price.low - price.open)).roundOffDecimal()

    val diffHighChar = if(diffHigh > 0) "↑" else "↓"
    val diffLowChar = if(diffLow > 0) "↑" else "↓"

    println(price)

    return "$diffCloseChar " +
            "${price.close.roundOffDecimal()} " +
            "(${diff}/${diffInPercentage}%), " +
            "H $diffHighChar $diffHigh, " +
            "L $diffLowChar $diffLow" +
            "${price.time} : ${price.interval}"
}