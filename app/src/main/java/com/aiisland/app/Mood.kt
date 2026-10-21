package com.aiisland.app

enum class Mood(val label: String) {
    NEUTRAL("Santai"),
    HAPPY("Gembira"),
    SLEEPY("Mengantuk"),
    SLEEP("Tidur"),
    DIZZY("Pening")
}

data class Phone(
    val battery: Int = 80,
    val charging: Boolean = false,
    val hour: Int = 12,
    val tilt: Float = 0f,
    val faceDown: Boolean = false,
    val dizzy: Boolean = false
)

fun moodOf(p: Phone): Mood = when {
    p.dizzy -> Mood.DIZZY
    p.faceDown -> Mood.SLEEP
    p.hour >= 23 || p.hour < 5 -> Mood.SLEEPY
    p.battery <= 15 && !p.charging -> Mood.SLEEPY
    p.charging -> Mood.HAPPY
    else -> Mood.NEUTRAL
}
