package com.orhan.models

import com.orhan.parser.Price
import com.orhan.memory.MarketRegime
import com.orhan.memory.Voter
import com.orhan.memory.VoterDecision
import com.orhan.memory.getActionSuggestion
import com.orhan.theme.color_default

const val VWAP_FRAME_SIZE = 10

class VWAPModel {

    fun getVoterStatsWithoutAffixes(voter: Voter): String {
        return voter.decision.name + " ${voter.decision.symbol}" + "${voter.buyerPercentage}%/${voter.sellerPercentage}%" + " : " + getActionSuggestion(voter)
    }

    fun getVoterStats(voter: Voter): String {
        return color_default + " - VWAP : " + getActionSuggestion(voter)
    }

    fun execute(vararg prices: Price): Voter {
        val totalVolume = prices.sumOf { it.volume }
        val p = prices.toList()
        val vwap = prices.sumOf { (it.close * it.volume).toLong() } / totalVolume

        val currentPrice = prices.last().close
        val decision = if (currentPrice > vwap) VoterDecision.BUY else VoterDecision.SELL

        return Voter(
            sellerPercentage = if (decision == VoterDecision.SELL) 100 else 0,
            buyerPercentage = if (decision == VoterDecision.BUY) 100 else 0,
            marketRegime = MarketRegime.TRENDING,
            decision = decision
        )
    }
}
