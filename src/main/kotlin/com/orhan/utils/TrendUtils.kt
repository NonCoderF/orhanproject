package com.orhan.utils

import java.util.*

object TrendUtils {

    fun getHighestHighLowestLow(price: List<Double>, fromIndex: Int, toIndex: Int): Pair<Int, Int> {
        return Pair(
            price.indexOf(Collections.max(price)) + fromIndex,
            price.indexOf(Collections.min(price)) + toIndex
        )
    }

    fun getRatingRelativePoint(type: String?, price: List<Float>): List<Int> {
        var coverage: Int
        val max_N : MutableList<Int> = mutableListOf()
        for (index in 1 until price.size) {
            var days = 0
            coverage = if (index <= price.size - index) index - 1 else price.size - index
            when (type) {
                "high" -> {
                    var j = 1
                    while (j < coverage) {
                        if (price[index] <= price[index - j] || price[index] <= price[index + j]) break
                        days = j
                        j++
                    }
                }

                "low" -> {
                    var j = 1
                    while (j < coverage) {
                        if (price[index] >= price[index - j] || price[index] >= price[index + j]) break
                        days = j
                        j++
                    }
                }
            }
            max_N.add(days)
        }
        return max_N
    }

    fun getHighsAndLows(max_N: List<Int>, count: Int, min_dist: Int, max_dist: Int): List<Int> {
        var index = 0
        val trend: MutableList<Int> = ArrayList()
        while (index < count - max_dist) {
            index = index + 1
            var best_so_far = 0
            var idx_best_so_far = 0
            for (i in index + min_dist until index + max_dist) {
                if (max_N[i] > best_so_far) {
                    idx_best_so_far = i
                    best_so_far = max_N[i]
                }
            }
            if (idx_best_so_far > 0) index = idx_best_so_far
            trend.add(index)
        }
        return trend
    }

    fun generatePairs(highIdx: List<Int>, lowIdx: List<Int>): List<List<Int>> {
        var first = 1
        val trendline_pairs: MutableList<List<Int>> = ArrayList()
        for (i in 1 until highIdx.size - 1) {
            for (j in first until lowIdx.size - 1) {
                if (lowIdx[j] < highIdx[i + 1] && highIdx[i] < lowIdx[j + 1]) {
                    val pairs: MutableList<Int> = ArrayList()
                    pairs.add(highIdx[i])
                    pairs.add(highIdx[i + 1])
                    pairs.add(lowIdx[j])
                    pairs.add(lowIdx[j + 1])
                    trendline_pairs.add(pairs)
                    first = j
                } else if (lowIdx[j] > highIdx[i + 1]) {
                    break
                }
            }
        }
        return trendline_pairs
    }

    fun morphPairs(trendPairs: List<List<Int>>): List<List<Int>> {
        if (trendPairs.size < 2) {
            return trendPairs
        }
        val tempTrendPairs: MutableList<List<Int>> = ArrayList()
        var i = 2
        while (i < trendPairs.size) {
            val pair: MutableList<Int> = ArrayList()
            pair.add(trendPairs[i][0])
            pair.add(trendPairs[2][1])
            pair.add(trendPairs[i][2])
            pair.add(trendPairs[2][3])
            tempTrendPairs.add(pair)
            i += 5
        }
        tempTrendPairs.reverse()
        tempTrendPairs.add(trendPairs[0])
        return tempTrendPairs
    }

    fun getSlopes(trendlinePairs: List<List<Int>>, prices: List<Double>): List<Pair<Double, Double>> {
        val slopes: MutableList<Pair<Double, Double>> = ArrayList()
        for (pairs in trendlinePairs) {
            val x1 = pairs[0].toDouble()
            val x2 = pairs[1].toDouble()
            val y1 = prices[pairs[0]]
            val y2 = prices[pairs[1]]
            val x3 = pairs[2].toDouble()
            val x4 = pairs[3].toDouble()
            val y3 = prices[pairs[2]]
            val y4 = prices[pairs[3]]
            var slope1 = Math.toDegrees(Math.atan((y1 - y2) / (x1 - x2)))
            var slope2 = Math.toDegrees(Math.atan((y3 - y4) / (x3 - x4)))
            if (java.lang.Double.isNaN(slope1)) slope1 = 0.0
            if (java.lang.Double.isNaN(slope2)) slope2 = 0.0
            slopes.add(Pair(slope1, slope2))
        }
        return slopes
    }

    fun getTrendPatterns(
        slopes: List<Pair<Double, Double>>,
        lineTolereance: Int,
        pairTolerance: Int
    ): List<TrendPatterns> {
        val patterns: MutableList<TrendPatterns> = ArrayList()
        for (slope in slopes) {
            var m1 = slope.first
            var m2 = slope.second
            if (Math.abs(m1) <= lineTolereance) m1 = 0.0
            if (Math.abs(m2) <= lineTolereance) m2 = 0.0
            if (m1 == 0.0 && m2 == 0.0) patterns.add(TrendPatterns.rectangle) else if (Math.abs(m1 - m2) <= pairTolerance) patterns.add(
                TrendPatterns.pricingChannel
            ) else if (m1 > 0 && m2 > 0 && Math.abs(m2) > Math.abs(m1)) patterns.add(TrendPatterns.risingWedge) else if (m1 < 0 && m2 < 0 && Math.abs(
                    m2
                ) < Math.abs(m1)
            ) patterns.add(TrendPatterns.fallingWedge) else if (m2 == 0.0 && m1 < 0) patterns.add(TrendPatterns.descendingTriangle) else if (m1 == 0.0 && m2 > 0) patterns.add(
                TrendPatterns.ascendingTriangle
            ) else if (m1 < 0 && m2 > 0 && Math.abs(m1 + m2) <= pairTolerance) patterns.add(TrendPatterns.symmetricalTriangle) else patterns.add(
                TrendPatterns.nil
            )
        }
        return patterns
    }

    enum class TrendPatterns {
        rectangle, pricingChannel, risingWedge, fallingWedge, descendingTriangle, ascendingTriangle, symmetricalTriangle, nil
    }
}