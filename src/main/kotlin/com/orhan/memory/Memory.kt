package com.orhan.memory

import com.orhan.extensions.roundOffDecimal
import com.orhan.models.*
import com.orhan.parser.Price
import com.orhan.theme.color_default
import com.orhan.theme.color_green
import com.orhan.theme.color_red
import com.orhan.theme.color_yellow
import javazoom.jl.decoder.JavaLayerException
import javazoom.jl.player.advanced.AdvancedPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileNotFoundException

enum class VoterDecision(val color: String, val symbol: String) {
    BUY(color_green, "▲"), SELL(color_red, "▼"), NOTHING(color_red, "-")
}

enum class MarketRegime(val color: String) {
    TRENDING(color_green), // Market is trending
    RANGING(color_yellow), // Market is ranging (sideways)
    CHOPPY(color_red) // Market is choppy (volatile)
}

data class Voter(
    var sellerPercentage: Int = 0,
    var buyerPercentage: Int = 0,
    var volumeTrend: VolumeTrend = VolumeTrend.STABLE,
    var marketRegime: MarketRegime = MarketRegime.TRENDING,
    var decision: VoterDecision = VoterDecision.BUY
)
enum class VolumeTrend {
    INCREASED, DECREASED, STABLE
}

fun getActionSuggestion(voter: Voter): VoterDecision {
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

class Memory {

    private val averagingModel = AveragingModel()
    private val predictiveModel = PredictiveModel()
    private val momentumModel = MomentumModel()
    private val vwapModel = VWAPModel()
    private val bollingerModel = BollingerBandsModel()
    private val rsiModel = RSIModel()
    private val stochModel = StochasticOscillatorModel()
    private val emaModel = EMACrossoverModel()

    fun execute(
        vararg prices: Price
    ): String {

        val averageModelString = averagingModel.getVoterStats(averagingModel.execute(*prices))

        val predictiveModelString = predictiveModel.getVoterStats(predictiveModel.execute(*prices))

        val momentumModelString = momentumModel.getVoterStats(momentumModel.execute(*prices))

        val vwapModelString = vwapModel.getVoterStats(vwapModel.execute(*prices))

        val bollingerModelString = bollingerModel.getVoterStats(bollingerModel.execute(*prices))

        val rsiModelString = rsiModel.getVoterStats(rsiModel.execute(*prices))

        val stochModelString = stochModel.getVoterStats(stochModel.execute(*prices))

        val emaModelString = emaModel.getVoterStats(emaModel.execute(*prices))

        return "$averageModelString " +
                "$predictiveModelString " +
                "$momentumModelString " +
                "$vwapModelString " +
                "$bollingerModelString " +
                "$rsiModelString " +
                "$stochModelString " +
                "$emaModelString "
    }
}