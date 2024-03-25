package com.orhan.memory

import com.orhan.extensions.roundOffDecimal
import com.orhan.models.AveragingModel
import com.orhan.parser.Price
import com.orhan.theme.color_default
import com.orhan.theme.color_green
import com.orhan.theme.color_red
import com.orhan.theme.color_yellow
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

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

    fun execute(
        vararg prices: Price
    ): Voter {

        val averagingModel = AveragingModel()

        return averagingModel.execute(*prices)
    }

    fun getVoterStats(voter: Voter): String {
        return color_default + " : Decision  : " + voter.decision.color + voter.decision.name + " ${voter.decision.symbol}" +
                color_default + ": Percentiles : $color_green${voter.buyerPercentage}%$color_default/$color_red${voter.sellerPercentage}%" +
                color_default + " - Interest : " + (if (voter.volumePercentage < 100) color_red else color_green) + " ${voter.volumePercentage}%" +
                color_default + " - Volatility : (${voter.volatilitySD.roundOffDecimal()}/${voter.volatilityATR.roundOffDecimal()})" +
                color_default + " - Suggestion : " + getActionSuggestion(voter)
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