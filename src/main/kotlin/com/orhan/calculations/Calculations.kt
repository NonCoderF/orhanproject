package com.orhan.calculations

import com.orhan.extensions.roundOffDecimal
import com.orhan.extensions.roundOffDecimalTo3Places
import com.orhan.parser.Price

fun calculate(price : Price): String{

    val diff = (price.close - price.open).roundOffDecimal()
    val diffInPercentage = ((price.close - price.open)/price.open).roundOffDecimal()

    val diffCloseChar = if (diff > 0) "▲" else if (diff < 0) "▼" else "▶"

    val diffHighPercentage = ((price.high - price.open)/price.open).roundOffDecimalTo3Places()
    val diffLowPercentage = ((price.low - price.open)/price.open).roundOffDecimalTo3Places()

    val diffHighPercentageChar = if(diffHighPercentage > 0) "↑" else "↓"
    val diffLowPercentageChar = if(diffLowPercentage > 0) "↑" else "↓"

    return "$diffCloseChar " +
            "${price.close.roundOffDecimal()} " +
            "(${diff}/${diffInPercentage}%), " +
            "H $diffHighPercentageChar $diffHighPercentage" +
            "L $diffLowPercentageChar $diffLowPercentage" +
            "${price.time} : ${price.interval}"
}