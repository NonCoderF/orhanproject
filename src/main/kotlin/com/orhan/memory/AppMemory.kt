package com.orhan.memory

import com.orhan.extensions.roundOffDecimal
import com.orhan.models.AveragingModel
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

class AppMemory {

    private val averagingModel = AveragingModel()

//    private val trendModel = TrendModel()

    fun execute(
        vararg prices: Price
    ): String {

        val averageModelString = averagingModel.getVoterStatsWithoutAffixes(averagingModel.execute(*prices))

        return averageModelString
    }
}