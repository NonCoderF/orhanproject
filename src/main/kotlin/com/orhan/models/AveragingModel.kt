package com.orhan.models

import com.orhan.extensions.roundOffDecimal
import com.orhan.memory.*
import com.orhan.parser.Price
import com.orhan.theme.color_default
import com.orhan.theme.color_green
import com.orhan.theme.color_red
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

const val FRAME_SIZE = 10
const val VOLATILITY_CONSTANT = 14

class AveragingModel {

    private var pricesList: MutableList<Array<out Price>> = mutableListOf()

    fun execute(
        vararg prices: Price
    ): Voter {

        if (pricesList.size > FRAME_SIZE) {
            pricesList.removeAt(0)
        }

        pricesList.add(prices)

        val flattenedPrices: List<Price> = pricesList.flatMap { it.toList() }

        val trendCount = flattenedPrices.map { it.close - it.open }

        val volumeSum = flattenedPrices.sumOf { it.volume } / pricesList.size
        val lastVolumeSummation = pricesList[pricesList.size - 1].sumOf { it.volume }

        val trueCount = trendCount.count { it > 0 }
        val falseCount = trendCount.count { it <= 0 }

        val volatilitySD = calculateStandardDeviation(pricesList[pricesList.size - 1].map { it.close })
        val volatilityATR = calculateATR(prices.map { it }, VOLATILITY_CONSTANT)

        val marketRegime = detectMarketRegime(pricesList[pricesList.size - 1].toList())

        return Voter(
            sellerPercentage = ((falseCount.toFloat() / trendCount.size.toFloat()) * 100).toInt(),
            buyerPercentage = ((trueCount.toFloat() / trendCount.size.toFloat()) * 100).toInt(),
            volumePercentage = ((volumeSum.toFloat() / lastVolumeSummation.toFloat()) * 100).toInt(),
            volatility = Pair(volatilitySD, volatilityATR),
            marketRegime = marketRegime,
            decision = if (trueCount > falseCount) VoterDecision.BUY else VoterDecision.SELL
        )
    }

    private fun calculateStandardDeviation(prices: List<Float>): Float {
        val mean = prices.average()
        val variance = prices.map { (it - mean).pow(2) }.sum() / prices.size
        return sqrt(variance).toFloat()
    }

    private fun calculateATR(prices: List<Price>, period: Int): Float {
        if (prices.size < period) return 0f  // Return 0 if insufficient data
        val atrValues = mutableListOf<Float>()
        for (i in period until prices.size) {
            val tr = maxOf(
                prices[i].high - prices[i].low,
                abs(prices[i].high - prices[i - 1].close),
                abs(prices[i].low - prices[i - 1].close)
            )
            atrValues.add(tr)
        }

        return if (atrValues.isEmpty()) 0f else atrValues.average().toFloat()
    }

    private fun detectMarketRegime(prices: List<Price>): MarketRegime {
        val averageTrueRange = calculateATR(prices, VOLATILITY_CONSTANT)
        return when {
            averageTrueRange < 0.5 -> MarketRegime.RANGING
            averageTrueRange > 1.0 -> MarketRegime.CHOPPY
            else -> MarketRegime.TRENDING
        }

    }
}