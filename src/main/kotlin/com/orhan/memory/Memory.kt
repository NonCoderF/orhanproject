package com.orhan.memory

import com.orhan.parser.Price
import com.orhan.theme.color_default
import com.orhan.theme.color_green
import com.orhan.theme.color_red

const val FRAME_SIZE = 30

class Memory {

    private var pricesList : MutableList<Price> = mutableListOf()

    private var count : Int = 0

    fun execute(
        vararg prices: Price
    ): Boolean {

        count++

        pricesList.addAll(prices)

        if (count > FRAME_SIZE) {
            prices.dropLast(1)
        }

        val isOnUptrend = pricesList.map { it.close - it.open }

        val trueCount = isOnUptrend.count { it > 0 }
        val falseCount = isOnUptrend.count { it <= 0 }

        return trueCount > falseCount
    }

    fun getVoterStats(voters : Boolean) : String = if (voters) "${color_green}▲$color_default" else "${color_red}▼$color_default"

}