package com.orhan.calculations

import com.orhan.extensions.roundOffDecimal
import com.orhan.parser.Price
import com.orhan.theme.color_default
import com.orhan.theme.color_green
import com.orhan.theme.color_red
import com.orhan.theme.color_yellow

data class Projectile(
    var priceProjectiles: String = "",
){
    override fun toString(): String {
        return priceProjectiles
    }
}

fun calculatePriceCharWithColor(price: Price): String {

    val diff = (price.close - price.open).roundOffDecimal()

    val diffCloseChar = when {
        diff > 0 -> "${color_green}▲${color_default}"
        diff < 0 -> "${color_red}▼${color_default}"
        else -> "${color_yellow}▶${color_default}"
    }

    val diffHigh = ((price.high - price.open)).roundOffDecimal()
    val diffLow = ((price.low - price.open)).roundOffDecimal()

    val diffHighChar = if (diffHigh > 0) "${color_green}↑${color_default}" else "${color_red}↓${color_default}"
    val diffLowChar = if (diffLow > 0) "${color_green}↑${color_default}" else "${color_red}↓${color_default}"

    return "$diffCloseChar$diffHighChar$diffLowChar"
}

fun calculatePriceCharWithoutColor(price: Price): String {

    val diff = (price.close - price.open).roundOffDecimal()

    val diffCloseChar = when {
        diff > 0 -> "▲"
        diff < 0 -> "▼"
        else -> "▶"
    }

//    val diffHigh = ((price.high - price.open)).roundOffDecimal()
//    val diffLow = ((price.low - price.open)).roundOffDecimal()
//
//    val diffHighChar = if (diffHigh > 0) "↑" else "↓"
//    val diffLowChar = if (diffLow > 0) "↑" else "↓"

    return diffCloseChar//$diffHighChar$diffLowChar"
}

fun getPriceProjectileStringWithColor(listOfPrices : List<Price>) = listOfPrices.map {
    "${calculatePriceCharWithColor(it)} - ${it.interval}"
    }.joinToString ( ", " )

fun getPriceProjectileStringWithoutColor(listOfPrices : List<Price>) = listOfPrices.map {
    "${calculatePriceCharWithoutColor(it)} - ${it.interval}"
}.joinToString ( ", " )