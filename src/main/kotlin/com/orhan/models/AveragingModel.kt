package com.orhan.models

import com.google.gson.Gson
import com.orhan.extensions.roundOffDecimal
import com.orhan.memory.*
import com.orhan.parser.Price
import com.orhan.theme.color_default
import com.orhan.theme.color_green
import com.orhan.theme.color_red
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

const val FRAME_SIZE = 20

class AveragingModel {

    fun getVoterStatsWithoutAffixes(voter: Voter): String {
        return voter.decision.name + " ${voter.decision.symbol}" + "${voter.buyerPercentage}%/${voter.sellerPercentage}%" + " : " + getActionSuggestion(voter)
    }

    fun getVoterStats(voter: Voter): String {
        return color_default + " : Decision  : " + voter.decision.color + voter.decision.name + " ${voter.decision.symbol}" +
                color_default + ": Percentiles : $color_green${voter.buyerPercentage}%$color_default/$color_red${voter.sellerPercentage}%" +
                color_default + " - Suggestion : " + getActionSuggestion(voter)
    }

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

        val trueCount = trendCount.count { it > 0 }
        val falseCount = trendCount.count { it <= 0 }

        val lastColumn = pricesList.map { it.last() }

        val marketRegime = detectMarketRegime(pricesList[pricesList.size - 1].toList())

        return Voter(
            sellerPercentage = ((falseCount.toFloat() / trendCount.size.toFloat()) * 100).toInt(),
            buyerPercentage = ((trueCount.toFloat() / trendCount.size.toFloat()) * 100).toInt(),
            marketRegime = marketRegime,
            decision = if (trueCount > falseCount) VoterDecision.BUY else VoterDecision.SELL
        )
    }

    private fun calculateATR(prices: List<Price>): Float {
        val atrValues = mutableListOf<Float>()

        prices.forEach {price ->
            val tr = maxOf(
                price.high - price.low,
                abs(price.high - price.close),
                abs(price.low - price.close)
            )
            atrValues.add(tr)
        }

        return if (atrValues.isEmpty()) 0f else atrValues.average().toFloat()
    }

    private fun detectMarketRegime(prices: List<Price>): MarketRegime {
        val averageTrueRange = calculateATR(prices)
        return when {
            averageTrueRange < 0.5 -> MarketRegime.RANGING
            averageTrueRange > 1.0 -> MarketRegime.CHOPPY
            else -> MarketRegime.TRENDING
        }

    }

    private fun getActionSuggestion(voter: Voter): String {
        val action = when (voter.marketRegime) {
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

        return action
    }
}