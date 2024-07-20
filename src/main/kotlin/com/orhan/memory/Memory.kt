package com.orhan.memory

import com.orhan.extensions.roundOffDecimal
import com.orhan.models.AveragingModel
import com.orhan.models.PredictiveModel
import com.orhan.models.TrendModel
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

class Memory {

    private val averagingModel = AveragingModel()
    private val predictiveModel = PredictiveModel()

    fun execute(
        vararg prices: Price
    ): String {

        val averageModelString = averagingModel.getVoterStats(averagingModel.execute(*prices))

        val predictiveModelString = predictiveModel.getVoterStats(predictiveModel.execute(*prices))

        return "$averageModelString $predictiveModelString"
    }
}