package com.orhan.models

import com.orhan.memory.MarketRegime
import com.orhan.memory.Voter
import com.orhan.memory.VoterDecision
import com.orhan.memory.getActionSuggestion
import com.orhan.parser.Price
import com.orhan.theme.color_default

const val EMA_FRAME_SIZE = 60

class EMACrossoverModel {

    private val pricesList: MutableList<Array<out Price>> = mutableListOf()

    fun getVoterStatsWithoutAffixes(voter: Voter): String {
        return voter.decision.name + " ${voter.decision.symbol}" + "${voter.buyerPercentage}%/${voter.sellerPercentage}%" + " : " + getActionSuggestion(
            voter
        )
    }

    fun getVoterStats(voter: Voter): String {
        return color_default + " - EMA : " + getActionSuggestion(voter)
    }

    fun execute(vararg prices: Price): Voter {

        if (pricesList.size > EMA_FRAME_SIZE) {
            pricesList.removeAt(0)
        }

        pricesList.add(prices)

        val priceList = pricesList.last().toList().map { it.close }

        val shortTermEMA = calculateEMA(priceList, 12)
        val longTermEMA = calculateEMA(priceList, 26)

        val decision = when {
            shortTermEMA > longTermEMA -> VoterDecision.BUY
            shortTermEMA < longTermEMA -> VoterDecision.SELL
            else -> VoterDecision.NOTHING
        }

        return Voter(
            sellerPercentage = if (decision == VoterDecision.SELL) 100 else 0,
            buyerPercentage = if (decision == VoterDecision.BUY) 100 else 0,
            marketRegime = MarketRegime.TRENDING,
            decision = decision
        )
    }

    private fun calculateEMA(prices: List<Float>, period: Int): Float {
        val smoothing = 2f / (period + 1)
        var ema = prices.first()

        for (price in prices) {
            ema = (price - ema) * smoothing + ema
        }

        return ema
    }
}
