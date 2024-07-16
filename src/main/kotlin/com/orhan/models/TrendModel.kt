package com.orhan.models

import com.google.gson.Gson
import com.orhan.parser.Price
import com.orhan.utils.TrendUtils

const val TREND_MODEL_FRAME_SIZE = 10

class TrendModel {

    private var pricesList: MutableList<Array<out Price>> = mutableListOf()

    fun execute(vararg prices: Price
    ): String {

        if (pricesList.size > TREND_MODEL_FRAME_SIZE) {
            pricesList.removeAt(0)
        }

        pricesList.add(prices)

        val price = pricesList.map { it.last() }

        val relhighs: List<Int> = TrendUtils.getRatingRelativePoint("high", price.map { it.high })
        val rellows: List<Int> = TrendUtils.getRatingRelativePoint("low", price.map { it.low })

        val highIdx = TrendUtils.getHighsAndLows(relhighs, price.size, 4, price.size / 10).toMutableList()
        val lowIdx = TrendUtils.getHighsAndLows(rellows, price.size, 4, price.size / 10).toMutableList()

        val o: Pair<Int, Int> = TrendUtils.getHighestHighLowestLow(price.map { it.close.toDouble() },0, price.size - 1)

        highIdx.add(o.first)
        lowIdx.add(o.second)

        val trendPairs: List<List<Int>> = TrendUtils.generatePairs(highIdx, lowIdx)
        val morphedTrendPairs: List<List<Int>> = TrendUtils.morphPairs(trendPairs)
        val slopes: List<Pair<Double, Double>> = TrendUtils.getSlopes(trendPairs, price.map { it.close.toDouble() })
        val patterns: List<TrendUtils.TrendPatterns> = TrendUtils.getTrendPatterns(slopes, 12, 15)

        return Gson().toJson(patterns)

    }

}