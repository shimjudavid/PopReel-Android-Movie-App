package com.emaxsoftware.popreel.feature.trivia

import com.emaxsoftware.popreel.core.model.TriviaQuestion

enum class TriviaPhase { Loading, Unavailable, Question, Revealed, Finished }

data class TriviaUiState(
    val phase: TriviaPhase = TriviaPhase.Loading,
    val questions: List<TriviaQuestion> = emptyList(),
    val index: Int = 0,
    val selected: Int? = null,
    /** The player's locked answer; null after a timeout. */
    val lockedAnswer: Int? = null,
    val eliminated: Set<Int> = emptySet(),
    val fiftyFiftyUsed: Boolean = false,
    val timeLeftMillis: Long = TriviaRules.QUESTION_MILLIS,
    val score: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val correctCount: Int = 0,
    val lastPoints: Int = 0,
    val paused: Boolean = false,
    val revealHeld: Boolean = false,
) {
    val current: TriviaQuestion? get() = questions.getOrNull(index)
    val questionNumber: Int get() = index + 1
    val total: Int get() = questions.size
    val timeFraction: Float get() = timeLeftMillis / TriviaRules.QUESTION_MILLIS.toFloat()
    val secondsLeft: Int get() = ((timeLeftMillis + 999) / 1000).toInt()
    val answeredCorrectly: Boolean get() = current?.let { lockedAnswer == it.answerIndex } == true
    val potentialPoints: Int get() = TriviaRules.points(timeLeftMillis, streak + 1)
}

/** Scoring and timing, kept pure for testing. */
object TriviaRules {
    const val QUESTIONS_PER_ROUND = 10
    const val QUESTION_MILLIS = 20_000L
    const val TICK_MILLIS = 100L
    const val REVEAL_MILLIS = 2_500L
    const val REVEAL_AFTER_HOLD_MILLIS = 1_500L

    private const val BASE_POINTS = 100
    private const val MAX_SPEED_BONUS = 100
    private const val STREAK_STEP = 50
    private const val MAX_STREAK_BONUS = 200

    /** Points for a correct answer with [timeLeftMillis] remaining that makes the streak [newStreak]. */
    fun points(timeLeftMillis: Long, newStreak: Int): Int {
        val speed = (MAX_SPEED_BONUS * timeLeftMillis / QUESTION_MILLIS).toInt()
        val streakBonus = (STREAK_STEP * (newStreak - 1)).coerceIn(0, MAX_STREAK_BONUS)
        return BASE_POINTS + speed + streakBonus
    }
}
