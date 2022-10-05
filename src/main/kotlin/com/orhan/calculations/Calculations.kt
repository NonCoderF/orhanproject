package com.orhan.calculations

import com.orhan.extensions.roundOffDecimal
import com.orhan.parser.Price
import com.orhan.parser.parsePrice
import io.ktor.client.*
import kotlinx.coroutines.*

fun calculateTrend(price : Price): String{

    val open = price.open.roundOffDecimal()
    val diff = (price.close - price.open).roundOffDecimal()
    val diffInPercentage = ((price.close - price.open)/price.open).roundOffDecimal()

    val diffChar = if (diff > 0) "▲" else if (diff < 0) "▼" else "▶"

    return "$diffChar $open (${diff}/${diffInPercentage}%), ${price.time} : ${price.interval}"
}