package com.orhan.memory

import com.orhan.extensions.roundOffDecimal
import com.orhan.parser.Price
import com.orhan.theme.color_default
import com.orhan.theme.color_green
import com.orhan.theme.color_red
import com.orhan.theme.color_yellow
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

const val FRAME_SIZE = 10

enum class VoterDecision(val color: String, val symbol: String) {
    BUY(color_green, "▲"), SELL(color_red, "▼")
}

enum class MarketRegime(val color: String) {
    TRENDING(color_green), // Market is trending
    RANGING(color_yellow), // Market is ranging (sideways)
    CHOPPY(color_red) // Market is choppy (volatile)
}

data class Voter(
    var sellerPercentage: Int = 0,
    var buyerPercentage: Int = 0,
    var volumePercentage: Int = 0,
    var volatilitySD: Float = 0f,
    var volatilityATR: Float = 0f,
    var marketRegime: MarketRegime = MarketRegime.TRENDING,
    var decision: VoterDecision = VoterDecision.BUY
)

class Memory {

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

        val volatilitySD = calculateStandardDeviation(flattenedPrices.map { it.close })
        val volatilityATR = calculateATR(flattenedPrices, 14)

        val marketRegime = detectMarketRegime(pricesList[pricesList.size - 1].toList())

        return Voter(
            sellerPercentage = ((falseCount.toFloat() / trendCount.size.toFloat()) * 100).toInt(),
            buyerPercentage = ((trueCount.toFloat() / trendCount.size.toFloat()) * 100).toInt(),
            volumePercentage = ((volumeSum.toFloat() / lastVolumeSummation.toFloat()) * 100).toInt(),
            volatilitySD = volatilitySD,
            volatilityATR = volatilityATR,
            marketRegime = marketRegime,
            decision = if (trueCount > falseCount) VoterDecision.BUY else VoterDecision.SELL
        )
    }

    fun getVoterStats(voter: Voter): String {
        return color_default + " : Decision  : " + voter.decision.color + voter.decision.name + " ${voter.decision.symbol}" +
                color_default + ": Percentiles : $color_green${voter.buyerPercentage}%$color_default/$color_red${voter.sellerPercentage}%" +
                color_default + " - Interest : " + (if (voter.volumePercentage < 100) color_red else color_green) + " ${voter.volumePercentage}%" +
                color_default + " - Volatility : (${voter.volatilitySD.roundOffDecimal()}/${voter.volatilityATR.roundOffDecimal()})" +
                color_default + " - Suggestion : " + getActionSuggestion(voter)
    }

    private fun calculateStandardDeviation(prices: List<Float>): Float {
        val mean = prices.average()
        val variance = prices.map { (it - mean).pow(2) }.sum() / prices.size
        return sqrt(variance).toFloat()
    }

    private fun calculateATR(prices: List<Price>, period: Int): Float {
        val atrValues = mutableListOf<Float>()
        for (i in period until prices.size) {
            val tr = maxOf(prices[i].high - prices[i].low,
                abs(prices[i].high - prices[i - 1].close),
                abs(prices[i].low - prices[i - 1].close))
            atrValues.add(tr)
        }
        return atrValues.average().toFloat()
    }

    private fun detectMarketRegime(prices: List<Price>): MarketRegime {
        val averageTrueRange = calculateATR(prices, 14)
        return when {
            averageTrueRange < 0.5 -> MarketRegime.RANGING
            averageTrueRange > 1.0 -> MarketRegime.CHOPPY
            else -> MarketRegime.TRENDING
        }
    }

    private fun getActionSuggestion(voter: Voter): String {
        return when (voter.marketRegime) {
            MarketRegime.TRENDING -> {
                if (voter.sellerPercentage > voter.buyerPercentage) {
                    "Sell"
                } else {
                    "Buy"
                }
            }
            MarketRegime.RANGING -> "Retain position"
            MarketRegime.CHOPPY -> "High volatility"
        }
    }


}