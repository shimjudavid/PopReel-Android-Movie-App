package com.emaxsoftware.popreel.core.audio

import android.media.AudioManager
import android.media.ToneGenerator
import javax.inject.Inject
import javax.inject.Singleton

enum class GameSound { Correct, Wrong, Complete, Tap }

interface GameSounds {
    fun play(sound: GameSound)
}

/** Short system tones; no audio assets to ship. Callers check the user's sound setting. */
@Singleton
class ToneGameSounds @Inject constructor() : GameSounds {

    private val tones: ToneGenerator? by lazy {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (_: RuntimeException) {
            null // Some devices refuse a ToneGenerator; the games stay playable silently.
        }
    }

    override fun play(sound: GameSound) {
        val (tone, durationMs) = when (sound) {
            GameSound.Correct -> ToneGenerator.TONE_PROP_ACK to 150
            GameSound.Wrong -> ToneGenerator.TONE_PROP_NACK to 200
            GameSound.Complete -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD to 300
            GameSound.Tap -> ToneGenerator.TONE_PROP_BEEP to 40
        }
        tones?.startTone(tone, durationMs)
    }
}
