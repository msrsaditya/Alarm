package com.example.data
import com.squareup.moshi.JsonClass
enum class MissionType { COLOR_TILES, TYPING, MATH, STEP, SHAKE }
enum class Difficulty { SUPER_EASY, EASY, MEDIUM, HARD, SUPER_HARD }
@JsonClass(generateAdapter = true)
data class MissionConfig(
    val type: MissionType,
    val difficulty: Difficulty? = Difficulty.MEDIUM,
    val repetition: Int = 3,
    val targetCount: Int? = null,
    val customPhrases: List<String>? = null
)