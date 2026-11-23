package com.emaxsoftware.popreel.testing

import com.emaxsoftware.popreel.core.audio.GameSound
import com.emaxsoftware.popreel.core.audio.GameSounds
import com.emaxsoftware.popreel.core.data.SettingsRepository
import com.emaxsoftware.popreel.core.data.TriviaRepository
import com.emaxsoftware.popreel.core.model.AppSettings
import com.emaxsoftware.popreel.core.model.ArcadeStats
import com.emaxsoftware.popreel.core.model.ThemeMode
import com.emaxsoftware.popreel.core.model.TriviaQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeTriviaRepository(var questions: List<TriviaQuestion>) : TriviaRepository {
    override suspend fun newRound(size: Int): Result<List<TriviaQuestion>> = Result.success(questions.take(size))
}

class FakeSettingsRepository : SettingsRepository {
    override val settings = MutableStateFlow(AppSettings())
    override val arcadeStats = MutableStateFlow(ArcadeStats())
    override suspend fun setThemeMode(mode: ThemeMode) = settings.update { it.copy(themeMode = mode) }
    override suspend fun setGameSoundEnabled(enabled: Boolean) = settings.update { it.copy(gameSoundEnabled = enabled) }
    override suspend fun recordTriviaRound(score: Int, bestStreak: Int) = arcadeStats.update {
        it.copy(
            triviaTotalPoints = it.triviaTotalPoints + score,
            triviaBestScore = maxOf(it.triviaBestScore, score),
            triviaBestStreak = maxOf(it.triviaBestStreak, bestStreak),
            triviaRoundsPlayed = it.triviaRoundsPlayed + 1,
        )
    }
    override suspend fun recordPuzzleSolved() = arcadeStats.update { it.copy(puzzlesSolved = it.puzzlesSolved + 1) }
}

class RecordingGameSounds : GameSounds {
    val played = mutableListOf<GameSound>()
    override fun play(sound: GameSound) {
        played += sound
    }
}

fun testQuestion(id: String, answer: Int = 0) = TriviaQuestion(
    id = id,
    movieId = "movie-$id",
    kind = "director",
    question = "Who directed $id?",
    options = listOf("A", "B", "C", "D"),
    answerIndex = answer,
)
