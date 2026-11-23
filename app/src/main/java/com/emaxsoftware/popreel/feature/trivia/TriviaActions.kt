package com.emaxsoftware.popreel.feature.trivia

import androidx.compose.runtime.Stable

@Stable
interface TriviaActions {
    fun onPlayAgain() {}
    fun onSelect(option: Int) {}
    fun onLockIn() {}
    fun onFiftyFifty() {}
    fun onRevealHold(held: Boolean) {}
    fun onNext() {}
    fun onPause() {}
    fun onResume() {}
}
