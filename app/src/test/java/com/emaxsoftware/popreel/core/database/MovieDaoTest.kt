package com.emaxsoftware.popreel.core.database

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.emaxsoftware.popreel.core.database.entity.GenreEntity
import com.emaxsoftware.popreel.core.database.entity.MovieChoiceEntity
import com.emaxsoftware.popreel.core.database.entity.MovieEntity
import com.emaxsoftware.popreel.core.model.MovieChoice
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class MovieDaoTest {

    private lateinit var db: PopReelDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), PopReelDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() = db.close()

    private suspend fun seed(vararg movies: MovieEntity) =
        db.movieDao().replaceCatalog(movies.toList(), listOf(GenreEntity("Drama")))

    @Test
    fun `genre filter matches whole genre names only`() = runTest {
        seed(
            movie("scifi", genres = "|Science Fiction|"),
            movie("fiction", genres = "|Fiction|"),
        )
        val ids = db.movieDao().observeTrending("Fiction", excludeMarked = false, limit = 10).first().map { it.id }
        assertEquals(listOf("fiction"), ids)
    }

    @Test
    fun `excludeMarked drops movies with any choice, including unseen`() = runTest {
        seed(movie("a", popularity = 3.0), movie("b", popularity = 2.0), movie("c", popularity = 1.0))
        db.movieChoiceDao().upsert(MovieChoiceEntity("a", MovieChoice.Liked, 1))
        db.movieChoiceDao().upsert(MovieChoiceEntity("c", MovieChoice.Unseen, 2))

        val dao = db.movieDao()
        assertEquals(listOf("b"), dao.observeTrending(null, excludeMarked = true, limit = 10).first().map { it.id })
        assertEquals(listOf("a", "b", "c"), dao.observeTrending(null, excludeMarked = false, limit = 10).first().map { it.id })
    }

    @Test
    fun `top rated ranks well-voted films first and skips thinly voted ones`() = runTest {
        seed(
            movie("obscure", rating = 9.9, votes = 12),
            movie("niche", rating = 9.0, votes = 300),
            movie("classic", rating = 8.5, votes = 20_000),
            movie("popular", rating = 8.0, votes = 5_000),
        )
        val ids = db.movieDao().observeTopRated(null, excludeMarked = false, limit = 10).first().map { it.id }
        assertEquals(listOf("classic", "popular", "niche"), ids)
    }

    @Test
    fun `search matches title or director, escapes wildcards`() = runTest {
        seed(
            movie("inception", title = "Inception", director = "Christopher Nolan"),
            movie("percent", title = "100% Wolf"),
            movie("other", title = "Other", director = "Someone Else"),
        )
        val dao = db.movieDao()
        assertEquals(listOf("inception"), dao.observeSearch("%nolan%", null, 10).first().map { it.id })
        assertEquals(listOf("percent"), dao.observeSearch("%100\\%%", null, 10).first().map { it.id })
    }

    @Test
    fun `replacing the catalog keeps the user's choices`() = runTest {
        seed(movie("a"))
        db.movieChoiceDao().upsert(MovieChoiceEntity("a", MovieChoice.Disliked, 1))

        seed(movie("a"), movie("b"))

        assertEquals(listOf("a"), db.movieChoiceDao().observeAll().first().map { it.movieId })
        assertEquals(2, db.movieDao().observeCount().first())
    }

    private fun movie(
        id: String,
        title: String = id,
        genres: String = "|Drama|",
        director: String? = null,
        rating: Double = 7.0,
        votes: Int = 1_000,
        popularity: Double = 1.0,
    ) = MovieEntity(
        id = id,
        title = title,
        year = 2020,
        synopsis = "",
        genres = genres,
        director = director,
        cast = "",
        runtimeMinutes = null,
        rating = rating,
        posterUrl = "",
        sourceUrl = null,
        voteCount = votes,
        popularity = popularity,
    )
}
