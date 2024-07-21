package com.orhan.models

import com.orhan.parser.Price
import com.orhan.memory.MarketRegime
import com.orhan.memory.Voter
import com.orhan.memory.VoterDecision
import com.orhan.memory.getActionSuggestion
import com.orhan.theme.color_default
import kotlin.math.pow
import kotlin.math.sqrt

private const val BOLLINGER_FRAME_SIZE = 1200

class BollingerBandsModel {

    private val pricesList: MutableList<Array<out Price>> = mutableListOf()

    fun getVoterStatsWithoutAffixes(voter: Voter): String {
        return voter.decision.name + " ${voter.decision.symbol}" + "${voter.buyerPercentage}%/${voter.sellerPercentage}%" + " : " + getActionSuggestion(voter)
    }

    fun getVoterStats(voter: Voter): String {
        return color_default + " - Bollinger : " + getActionSuggestion(voter)
    }

    fun execute(vararg prices: Price): Voter {

        if (pricesList.size > BOLLINGER_FRAME_SIZE) {
            pricesList.removeAt(0)
        }

        pricesList.add(prices)

        val priceList = pricesList.last().toList()
        val sma = priceList.map { it.close }.average()
        val stdDev = sqrt(priceList.map { (it.close - sma).pow(2) }.average())
        val upperBand = sma + (2 * stdDev)
        val lowerBand = sma - (2 * stdDev)

        val currentPrice = prices.last().close
        val decision = when {
            currentPrice > upperBand -> VoterDecision.SELL
            currentPrice < lowerBand -> VoterDecision.BUY
            else -> VoterDecision.NOTHING
        }

        return Voter(
            sellerPercentage = if (decision == VoterDecision.SELL) 100 else 0,
            buyerPercentage = if (decision == VoterDecision.BUY) 100 else 0,
            marketRegime = MarketRegime.TRENDING,
            decision = decision
        )
    }
}
