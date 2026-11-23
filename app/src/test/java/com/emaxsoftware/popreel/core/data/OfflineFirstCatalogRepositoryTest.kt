package com.emaxsoftware.popreel.core.data

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.emaxsoftware.popreel.core.database.PopReelDatabase
import com.emaxsoftware.popreel.core.network.PopReelApiService
import com.emaxsoftware.popreel.core.network.dto.CatalogDto
import com.emaxsoftware.popreel.core.network.dto.MovieDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class OfflineFirstCatalogRepositoryTest {

    private lateinit var db: PopReelDatabase
    private val api = FakeApi()
    private lateinit var repository: OfflineFirstCatalogRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), PopReelDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = OfflineFirstCatalogRepository(api, db.movieDao(), Json { ignoreUnknownKeys = true }, UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `successful sync stores movies and genres`() = runTest {
        api.response = { CatalogDto(listOf(dto("a", listOf("Drama", "Crime")), dto("b", listOf("Drama")))) }

        assertTrue(repository.sync().isSuccess)

        assertEquals(CatalogSyncState.Synced, repository.syncState.value)
        assertEquals(2, repository.movieCount.first())
        assertEquals(listOf("Crime", "Drama"), repository.genres.first())
    }

    @Test
    fun `network failure maps to DataError and keeps the saved catalog`() = runTest {
        api.response = { CatalogDto(listOf(dto("a"))) }
        repository.sync()
        api.response = { throw IOException("offline") }

        val error = repository.sync().exceptionOrNull()

        assertTrue(error is DataError.Network)
        assertTrue(repository.syncState.value is CatalogSyncState.Failed)
        assertEquals(1, repository.movieCount.first())
    }

    @Test
    fun `http failure carries the server message`() = runTest {
        api.response = {
            throw HttpException(
                Response.error<CatalogDto>(503, """{"error":"Maintenance"}""".toResponseBody("application/json".toMediaType())),
            )
        }

        val error = repository.sync().exceptionOrNull() as DataError.Server

        assertEquals(503, error.code)
        assertTrue(error.message!!.contains("Maintenance"))
    }

    private class FakeApi : PopReelApiService {
        var response: () -> CatalogDto = { CatalogDto(emptyList()) }
        override suspend fun catalog(): CatalogDto = response()
    }

    private fun dto(id: String, genres: List<String> = listOf("Drama")) =
        MovieDto(id = id, title = id, genres = genres, voteCount = 10, popularity = 1.0)
}
