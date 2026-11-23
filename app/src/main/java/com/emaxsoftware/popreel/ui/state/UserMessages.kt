package com.emaxsoftware.popreel.ui.state

import com.emaxsoftware.popreel.core.data.DataError
import com.emaxsoftware.popreel.core.model.MovieChoice

fun DataError.userMessage(): String = when (this) {
    is DataError.Network -> "Couldn't reach the movie catalog. Check your connection."
    is DataError.Server -> "The catalog service had a problem (error $code). Try again shortly."
    is DataError.MalformedResponse -> "The catalog sent data PopReel couldn't read."
    is DataError.Local -> "Couldn't read saved data on this device."
}

const val INITIAL_SETUP_TITLE = "Movie catalog not downloaded yet"
const val INITIAL_SETUP_MESSAGE =
    "PopReel needs an internet connection once to download the catalog. After that, browsing and your choices work offline."

fun choiceSavedMessage(title: String, choice: MovieChoice?): String = when (choice) {
    MovieChoice.Liked -> "Liked “$title”"
    MovieChoice.Disliked -> "Disliked “$title”"
    MovieChoice.Unseen -> "Marked “$title” as not seen"
    null -> "Cleared your choice for “$title”"
}

fun choiceUndoneMessage(title: String, choice: MovieChoice): String = when (choice) {
    MovieChoice.Liked -> "Undid like for “$title”"
    MovieChoice.Disliked -> "Undid dislike for “$title”"
    MovieChoice.Unseen -> "Undid “not seen” for “$title”"
}

const val CHOICE_SAVE_FAILED = "Couldn't save your choice. Try again."
