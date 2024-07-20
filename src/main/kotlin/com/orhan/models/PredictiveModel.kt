package com.orhan.models

import com.orhan.parser.Price
import com.orhan.memory.MarketRegime
import com.orhan.memory.Voter
import com.orhan.memory.VoterDecision
import com.orhan.theme.color_default

const val PRE_FRAME_SIZE = 60

class PredictiveModel {

    private var pricesList: MutableList<Array<out Price>> = mutableListOf()

    fun getVoterStatsWithoutAffixes(voter: Voter): String {
        return voter.decision.name + " ${voter.decision.symbol}" + "${voter.buyerPercentage}%/${voter.sellerPercentage}%" + " : " + getActionSuggestion(voter)
    }

    fun getVoterStats(voter: Voter): String {
        return color_default + " - Prediction : " + getActionSuggestion(voter)
    }

    fun execute(
        vararg prices: Price
    ): Voter {

        if (pricesList.size > PRE_FRAME_SIZE) {
            pricesList.removeAt(0)
        }

        pricesList.add(prices)

        val flattenedPrices: List<Price> = pricesList.last().toList()

        val macdResults = calculateMACD(flattenedPrices)
        val trendCount = flattenedPrices.map { it.close - it.open }

        val trueCount = trendCount.count { it > 0 }
        val falseCount = trendCount.count { it <= 0 }

        val lastColumn = pricesList.map { it.last() }

        val marketRegime = detectMarketRegime(pricesList[pricesList.size - 1].toList())

        val buyProbability = (trueCount.toFloat() / trendCount.size.toFloat())
        val sellProbability = (falseCount.toFloat() / trendCount.size.toFloat())

        return Voter(
            sellerPercentage = (sellProbability * 100).toInt(),
            buyerPercentage = (buyProbability * 100).toInt(),
            marketRegime = marketRegime,
            decision = if (macdResults.macd > macdResults.signal) VoterDecision.BUY else VoterDecision.SELL
        )
    }

    private fun calculateEMA(prices: List<Float>, period: Int): List<Float> {
        val multiplier = 2.0f / (period + 1)
        val ema = mutableListOf<Float>()
        prices.forEachIndexed { index, price ->
            if (index == 0) {
                ema.add(price)
            } else {
                ema.add((price - ema[index - 1]) * multiplier + ema[index - 1])
            }
        }
        return ema
    }

    private fun calculateMACD(prices: List<Price>): MACDResult {
        val closePrices = prices.map { it.close }
        val ema12 = calculateEMA(closePrices, 12)
        val ema26 = calculateEMA(closePrices, 26)
        val macdLine = ema12.zip(ema26).map { it.first - it.second }
        val signalLine = calculateEMA(macdLine, 9)
        val macd = macdLine.last()
        val signal = signalLine.last()
        return MACDResult(macd, signal)
    }

    private fun calculateATR(prices: List<Price>): Float {
        val atrValues = mutableListOf<Float>()

        prices.forEach { price ->
            val tr = maxOf(
                price.high - price.low,
                kotlin.math.abs(price.high - price.close),
                kotlin.math.abs(price.low - price.close)
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

    private fun getActionSuggestion(voter: Voter): VoterDecision {
        return when (voter.marketRegime) {
            MarketRegime.TRENDING -> {
                if (voter.sellerPercentage > voter.buyerPercentage) {
                    VoterDecision.SELL
                } else {
                    VoterDecision.BUY
                }
            }
            MarketRegime.RANGING -> VoterDecision.NOTHING
            MarketRegime.CHOPPY -> VoterDecision.SELL
        }
    }
}

data class MACDResult(val macd: Float, val signal: Float)
