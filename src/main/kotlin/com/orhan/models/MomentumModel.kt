package com.orhan.models

import com.orhan.memory.Voter
import com.orhan.memory.VoterDecision
import com.orhan.memory.MarketRegime
import com.orhan.memory.getActionSuggestion
import com.orhan.parser.Price
import com.orhan.theme.color_default
import kotlin.math.max
import kotlin.math.min

const val MOM_FRAME_SIZE = 10

class MomentumModel {

    private val pricesList: MutableList<Array<out Price>> = mutableListOf()

    fun getVoterStatsWithoutAffixes(voter: Voter): String {
        return voter.decision.name + " ${voter.decision.symbol}" + "${voter.buyerPercentage}%/${voter.sellerPercentage}%" + " : " + getActionSuggestion(voter)
    }

    fun getVoterStats(voter: Voter): String {
        return color_default + " - Momentum : " + getActionSuggestion(voter)
    }

    fun execute(vararg prices: Price): Voter {

        if (pricesList.size > MOM_FRAME_SIZE) {
            pricesList.removeAt(0)
        }
        pricesList.add(prices)

        val flattenedPrices: List<Price> = pricesList.last().toList()

        val roc = calculateRoC(flattenedPrices, 14)
        val rsi = calculateRSI(flattenedPrices, 14)

        val decision = when {
            roc > 0 && rsi < 30 -> VoterDecision.BUY
            roc < 0 && rsi > 70 -> VoterDecision.SELL
            else -> VoterDecision.NOTHING
        }

        val buyerPercentage = max(0, 100 + roc.toInt())
        val sellerPercentage = max(0, 100 - roc.toInt())

        return Voter(
            sellerPercentage = sellerPercentage,
            buyerPercentage = buyerPercentage,
            marketRegime = detectMarketRegime(flattenedPrices),
            decision = decision
        )
    }

    private fun calculateRoC(prices: List<Price>, period: Int): Float {
        return if (prices.size < period + 1) {
            0f
        } else {
            val pastPrice = prices[prices.size - period - 1].close
            val currentPrice = prices.last().close
            ((currentPrice - pastPrice) / pastPrice) * 100
        }
    }

    private fun calculateRSI(prices: List<Price>, period: Int): Float {
        if (prices.size < period + 1) return 0f

        var gainSum = 0f
        var lossSum = 0f

        for (i in 1..period) {
            val change = prices[prices.size - i].close - prices[prices.size - i - 1].close
            if (change > 0) {
                gainSum += change
            } else {
                lossSum -= change
            }
        }

        val avgGain = gainSum / period
        val avgLoss = lossSum / period

        return 100 - (100 / (1 + (avgGain / avgLoss)))
    }

    private fun detectMarketRegime(prices: List<Price>): MarketRegime {
        val averageTrueRange = calculateATR(prices)
        return when {
            averageTrueRange < 0.5 -> MarketRegime.RANGING
            averageTrueRange > 1.0 -> MarketRegime.CHOPPY
            else -> MarketRegime.TRENDING
        }
    }

    private fun calculateATR(prices: List<Price>): Float {
        val atrValues = mutableListOf<Float>()
        prices.forEach { price ->
            val tr = max(price.high - price.low, max(price.high - price.close, price.low - price.close))
            atrValues.add(tr)
        }
        return if (atrValues.isEmpty()) 0f else atrValues.average().toFloat()
    }
}
