package com.orhan.models

import com.orhan.memory.MarketRegime
import com.orhan.memory.Voter
import com.orhan.memory.VoterDecision
import com.orhan.memory.getActionSuggestion
import com.orhan.parser.Price
import com.orhan.theme.color_default
import kotlin.math.abs

const val RSI_FRAME_SIZE = 60

class RSIModel {

    private val pricesList: MutableList<Array<out Price>> = mutableListOf()

    fun getVoterStatsWithoutAffixes(voter: Voter): String {
        return voter.decision.name + " ${voter.decision.symbol}" + "${voter.buyerPercentage}%/${voter.sellerPercentage}%" + " : " + getActionSuggestion(
            voter
        )
    }

    fun getVoterStats(voter: Voter): String {
        return color_default + " - RSI : " + getActionSuggestion(voter)
    }

    fun execute(vararg prices: Price): Voter {

        if (pricesList.size > RSI_FRAME_SIZE) {
            pricesList.removeAt(0)
        }

        pricesList.add(prices)

        val priceList = pricesList.last().toList()
        val gains = mutableListOf<Float>()
        val losses = mutableListOf<Float>()

        for (i in 1 until priceList.size) {
            val change = priceList[i].close - priceList[i - 1].close
            if (change > 0) {
                gains.add(change)
            } else {
                losses.add(abs(change))
            }
        }

        val avgGain = if (gains.isNotEmpty()) gains.average().toFloat() else 0f
        val avgLoss = if (losses.isNotEmpty()) losses.average().toFloat() else 0f
        val rs = if (avgLoss != 0f) avgGain / avgLoss else 0f
        val rsi = 100 - (100 / (1 + rs))

        val decision = when {
            rsi > 70 -> VoterDecision.SELL
            rsi < 30 -> VoterDecision.BUY
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
