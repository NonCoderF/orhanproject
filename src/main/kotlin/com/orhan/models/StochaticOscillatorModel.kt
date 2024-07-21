package com.orhan.models

import com.orhan.memory.MarketRegime
import com.orhan.memory.Voter
import com.orhan.memory.VoterDecision
import com.orhan.memory.getActionSuggestion
import com.orhan.parser.Price
import com.orhan.theme.color_default

const val STOCH_FRAME_SIZE = 60

class StochasticOscillatorModel {

    private val pricesList: MutableList<Array<out Price>> = mutableListOf()

    fun getVoterStatsWithoutAffixes(voter: Voter): String {
        return voter.decision.name + " ${voter.decision.symbol}" + "${voter.buyerPercentage}%/${voter.sellerPercentage}%" + " : " + getActionSuggestion(
            voter
        )
    }

    fun getVoterStats(voter: Voter): String {
        return color_default + " - Stoch : " + getActionSuggestion(voter)
    }

    fun execute(vararg prices: Price): Voter {

        if (pricesList.size > STOCH_FRAME_SIZE) {
            pricesList.removeAt(0)
        }

        pricesList.add(prices)

        val priceList = pricesList.last().toList()
        val high = priceList.maxOf { it.high }
        val low = priceList.minOf { it.low }
        val close = priceList.last().close

        val k = ((close - low) / (high - low)) * 100

        val decision = when {
            k > 80 -> VoterDecision.SELL
            k < 20 -> VoterDecision.BUY
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
