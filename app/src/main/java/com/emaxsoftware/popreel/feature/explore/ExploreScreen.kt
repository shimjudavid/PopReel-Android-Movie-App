package com.emaxsoftware.popreel.feature.explore

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emaxsoftware.popreel.core.data.CatalogSyncState
import com.emaxsoftware.popreel.core.data.DataError
import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.model.MovieChoice
import com.emaxsoftware.popreel.ui.components.CatalogSearchBar
import com.emaxsoftware.popreel.ui.components.CatalogStatusRow
import com.emaxsoftware.popreel.ui.components.EmptyBlock
import com.emaxsoftware.popreel.ui.components.ErrorBlock
import com.emaxsoftware.popreel.ui.components.FeaturedHeroCard
import com.emaxsoftware.popreel.ui.components.GenreChipRow
import com.emaxsoftware.popreel.ui.components.LoadingBlock
import com.emaxsoftware.popreel.ui.components.MoviePosterCard
import com.emaxsoftware.popreel.ui.components.PosterCardStyle
import com.emaxsoftware.popreel.ui.components.SectionHeader
import com.emaxsoftware.popreel.ui.components.SortChip
import com.emaxsoftware.popreel.ui.preview.SampleCatalog
import com.emaxsoftware.popreel.ui.state.CatalogStatus
import com.emaxsoftware.popreel.ui.state.INITIAL_SETUP_MESSAGE
import com.emaxsoftware.popreel.ui.state.INITIAL_SETUP_TITLE
import com.emaxsoftware.popreel.ui.state.ListState
import com.emaxsoftware.popreel.ui.state.userMessage
import com.emaxsoftware.popreel.ui.theme.PopReelTheme
import java.io.IOException

/**
 * Explore Catalog (Home).
 *
 * @param contentPadding insets from the parent Scaffold, applied inside the grid so content
 *   scrolls edge-to-edge behind the system and app bars.
 */
@Composable
fun ExploreScreen(
    contentPadding: PaddingValues,
    onOpenMovie: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExploreContent(
        uiState = uiState,
        query = viewModel.query,
        contentPadding = contentPadding,
        onOpenMovie = onOpenMovie,
        actions = viewModel,
        modifier = modifier,
    )
}

@Composable
private fun ExploreContent(
    uiState: ExploreUiState,
    query: String,
    contentPadding: PaddingValues,
    onOpenMovie: (String) -> Unit,
    actions: ExploreActions,
    modifier: Modifier = Modifier,
) {
    var twoColumns by rememberSaveable { mutableStateOf(true) }
    val openDetails: (Movie) -> Unit = { onOpenMovie(it.id) }

    val spacing = PopReelTheme.spacing
    val gutter = spacing.gutter
    val layoutDirection = LocalLayoutDirection.current
    // Wider windows (tablets, unfolded foldables) get more poster columns.
    val wide = LocalConfiguration.current.screenWidthDp >= 600
    val columns = when {
        !twoColumns -> if (wide) 2 else 1
        wide -> 4
        else -> 2
    }
    val edge = Modifier
        .fillMaxWidth()
        .padding(horizontal = gutter)

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(layoutDirection),
            end = contentPadding.calculateEndPadding(layoutDirection),
            top = contentPadding.calculateTopPadding() + spacing.xs,
            bottom = contentPadding.calculateBottomPadding() + spacing.xl,
        ),
        horizontalArrangement = Arrangement.spacedBy(gutter),
    ) {
        fullWidthItem("status") {
            CatalogStatusRow(
                label = uiState.catalog.label,
                status = uiState.catalog.connection,
                modifier = edge.padding(bottom = spacing.md),
            )
        }

        when {
            uiState.catalog.needsInitialSetup -> fullWidthItem("setup") {
                ErrorBlock(
                    title = INITIAL_SETUP_TITLE,
                    message = INITIAL_SETUP_MESSAGE,
                    onRetry = actions::onRetrySync,
                    modifier = edge,
                )
            }

            uiState.catalog.isSettingUp -> fullWidthItem("setting-up") {
                LoadingBlock(
                    message = "Downloading the movie catalog…",
                    modifier = edge.height(240.dp),
                )
            }

            else -> catalogContent(
                uiState = uiState,
                query = query,
                columns = columns,
                gutter = gutter,
                edge = edge,
                actions = actions,
                onOpen = openDetails,
                onToggleLayout = { twoColumns = !twoColumns },
                twoColumns = twoColumns,
            )
        }
    }
}

private fun LazyGridScope.catalogContent(
    uiState: ExploreUiState,
    query: String,
    columns: Int,
    gutter: Dp,
    edge: Modifier,
    actions: ExploreActions,
    onOpen: (Movie) -> Unit,
    onToggleLayout: () -> Unit,
    twoColumns: Boolean,
) {
    fullWidthItem("search") {
        CatalogSearchBar(query = query, onQueryChange = actions::onQueryChange, modifier = edge)
    }

    fullWidthItem("genres") {
        GenreChipRow(
            genres = uiState.genres,
            selected = uiState.selectedGenre,
            onSelect = actions::onGenreSelect,
            modifier = Modifier.padding(top = 12.dp, bottom = 20.dp),
        )
    }

    val results = uiState.searchResults
    if (results != null) {
        fullWidthItem("search-header") {
            SectionHeader(
                title = "Results",
                icon = Icons.Outlined.Search,
                subtitle = if (results.isLoading) {
                    "Searching for “${query.trim()}”…"
                } else {
                    "${results.items.size} films matching “${query.trim()}”"
                },
                modifier = edge.padding(bottom = 16.dp),
            )
        }
        movieGrid(
            keyPrefix = "result",
            section = results,
            choices = uiState.choices,
            columns = columns,
            gutter = gutter,
            edge = edge,
            emptyMessage = "No titles or directors match “${query.trim()}”.",
            onOpen = onOpen,
        )
        return
    }

    fullWidthItem("feed-header") {
        SectionHeader(
            title = "Curated Feed",
            icon = Icons.Filled.LocalFireDepartment,
            leadingIcon = true,
            action = {
                SortChip(
                    options = CatalogSort.entries.map { it.label },
                    selected = uiState.sort.label,
                    onSelect = { label -> actions.onSortSelect(CatalogSort.entries.first { it.label == label }) },
                )
            },
            modifier = edge.padding(bottom = 16.dp),
        )
    }

    val feed = uiState.feed
    when {
        feed.isLoading -> fullWidthItem("feed-loading") {
            LoadingBlock(edge.height(336.dp))
        }

        feed.error != null -> fullWidthItem("feed-error") {
            ErrorBlock(feed.error.userMessage(), actions::onRetrySync, edge)
        }

        feed.items.isEmpty() -> fullWidthItem("feed-empty") {
            EmptyBlock("Nothing to show for this genre yet.", edge)
        }

        else -> {
            uiState.featured?.let { featured ->
                fullWidthItem("hero") {
                    FeaturedHeroCard(
                        movie = featured,
                        choice = uiState.choices[featured.id],
                        onOpenDetails = { onOpen(featured) },
                        modifier = edge.height(336.dp),
                    )
                }
            }
            if (uiState.spotlight.isNotEmpty()) {
                fullWidthItem("spotlight-header") {
                    SectionHeader(
                        title = "Spotlight & Velocity",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        subtitle = uiState.sort.subtitle,
                        modifier = edge.padding(top = 32.dp, bottom = 16.dp),
                    )
                }
                fullWidthItem("spotlight-row") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = gutter),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(uiState.spotlight, key = { it.id }) { movie ->
                            MoviePosterCard(
                                movie = movie,
                                choice = uiState.choices[movie.id],
                                onClick = { onOpen(movie) },
                                style = PosterCardStyle.Compact,
                                modifier = Modifier.width(144.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    fullWidthItem("acclaimed-header") {
        SectionHeader(
            title = "Critically Acclaimed",
            icon = Icons.Outlined.Verified,
            subtitle = "Top rated films with 1k+ votes",
            action = {
                OutlinedIconButton(
                    onClick = onToggleLayout,
                    shape = MaterialTheme.shapes.extraSmall,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceContainerHighest),
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        if (twoColumns) Icons.Outlined.GridView else Icons.Outlined.ViewAgenda,
                        contentDescription = if (twoColumns) "Switch to list" else "Switch to grid",
                        tint = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                }
            },
            modifier = edge.padding(top = 32.dp, bottom = 16.dp),
        )
    }
    movieGrid(
        keyPrefix = "acclaimed",
        section = uiState.acclaimed,
        choices = uiState.choices,
        columns = columns,
        gutter = gutter,
        edge = edge,
        emptyMessage = "No acclaimed films in this genre yet.",
        onOpen = onOpen,
    )
}

private fun LazyGridScope.movieGrid(
    keyPrefix: String,
    section: ListState<Movie>,
    choices: Map<String, MovieChoice>,
    columns: Int,
    gutter: Dp,
    edge: Modifier,
    emptyMessage: String,
    onOpen: (Movie) -> Unit,
) {
    when {
        section.isLoading -> fullWidthItem("$keyPrefix-loading") { LoadingBlock(edge.height(200.dp)) }

        section.error != null -> fullWidthItem("$keyPrefix-error") {
            EmptyBlock(section.error.userMessage(), edge)
        }

        section.isEmptyResult -> fullWidthItem("$keyPrefix-empty") { EmptyBlock(emptyMessage, edge) }

        else -> itemsIndexed(section.items, key = { _, movie -> "$keyPrefix-${movie.id}" }) { index, movie ->
            // The grid is full-bleed, so only the screen-edge side of each column gets the gutter.
            val column = index % columns
            MoviePosterCard(
                movie = movie,
                choice = choices[movie.id],
                onClick = { onOpen(movie) },
                style = PosterCardStyle.Detailed,
                modifier = Modifier.padding(
                    start = if (column == 0) gutter else 0.dp,
                    end = if (column == columns - 1) gutter else 0.dp,
                    bottom = 16.dp,
                ),
            )
        }
    }
}

private fun LazyGridScope.fullWidthItem(
    key: String,
    content: @Composable LazyGridItemScope.() -> Unit,
) = item(key = key, span = { GridItemSpan(maxLineSpan) }, content = content)

@Preview(showBackground = true, backgroundColor = 0xFF10131C, heightDp = 1600)
@Composable
private fun ExploreContentPreview() {
    PopReelTheme(darkTheme = true) {
        ExploreContent(
            uiState = ExploreUiState(
                catalog = CatalogStatus(movieCount = 9919, sync = CatalogSyncState.Synced),
                genres = listOf(ExploreUiState.ALL_GENRES, "Action", "Drama", "Science Fiction"),
                feed = ListState.loaded(listOf(SampleCatalog.featured) + SampleCatalog.spotlight),
                acclaimed = ListState.loaded(SampleCatalog.criticallyAcclaimed),
                choices = mapOf(SampleCatalog.spotlight.first().id to MovieChoice.Liked),
            ),
            query = "",
            contentPadding = PaddingValues(),
            onOpenMovie = {},
            actions = object : ExploreActions {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF10131C, heightDp = 640)
@Composable
private fun ExploreFirstLaunchOfflinePreview() {
    PopReelTheme(darkTheme = true) {
        ExploreContent(
            uiState = ExploreUiState(
                catalog = CatalogStatus(
                    movieCount = 0,
                    sync = CatalogSyncState.Failed(
                        DataError.Network(IOException()),
                    ),
                ),
            ),
            query = "",
            contentPadding = PaddingValues(),
            onOpenMovie = {},
            actions = object : ExploreActions {},
        )
    }
}
