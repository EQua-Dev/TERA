package com.androidstrike.tera.data

data class Rating(
    val avr_rating: Double = 0.0,
    val counts: Int = 0,
    val engagement: Double = 0.0,
    val fluency: Double = 0.0,
    val lecturer: String = "",
    val punctuality: Double = 0.0,
    val tech_tips: Double = 0.0
)