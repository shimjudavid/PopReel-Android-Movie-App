package com.emaxsoftware.popreel.feature.details

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil3.compose.AsyncImage
import com.emaxsoftware.popreel.core.model.Movie
import com.emaxsoftware.popreel.core.model.MovieChoice
import com.emaxsoftware.popreel.core.model.formatCompactCount
import com.emaxsoftware.popreel.ui.components.BackTopBar
import com.emaxsoftware.popreel.ui.components.ChoiceSelector
import com.emaxsoftware.popreel.ui.components.EmptyBlock
import com.emaxsoftware.popreel.ui.components.LoadingBlock
import com.emaxsoftware.popreel.ui.components.MoviePosterCard
import com.emaxsoftware.popreel.ui.components.PosterCardStyle
import com.emaxsoftware.popreel.ui.preview.SampleCatalog
import com.emaxsoftware.popreel.ui.state.CHOICE_SAVE_FAILED
import com.emaxsoftware.popreel.ui.state.choiceSavedMessage
import com.emaxsoftware.popreel.ui.theme.PopReelTheme
import java.util.Locale

@Composable
fun MovieDetailsScreen(
    movieId: String,
    onBack: () -> Unit,
    onOpenMovie: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: MovieDetailsViewModel = hiltViewModel<MovieDetailsViewModel, MovieDetailsViewModel.Factory>(
        key = "details:$movieId",
    ) { factory -> factory.create(movieId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnShowMessage by rememberUpdatedState(onShowMessage)
    LaunchedEffect(viewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                currentOnShowMessage(
                    when (effect) {
                        is MovieDetailsEffect.ChoiceSaved -> choiceSavedMessage(effect.movieTitle, effect.choice)
                        MovieDetailsEffect.ChoiceFailed -> CHOICE_SAVE_FAILED
                    },
                )
            }
        }
    }
    MovieDetailsContent(uiState, onBack, onOpenMovie, onChoiceChange = viewModel::onChoiceChange)
}

@Composable
private fun MovieDetailsContent(
    uiState: MovieDetailsUiState,
    onBack: () -> Unit,
    onOpenMovie: (String) -> Unit,
    onChoiceChange: (Movie, MovieChoice?) -> Unit,
) {
    val context = LocalContext.current
    val loaded = uiState as? MovieDetailsUiState.Loaded
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BackTopBar(
                title = loaded?.movie?.title ?: "Movie details",
                onBack = onBack,
                actions = {
                    loaded?.movie?.let { movie ->
                        IconButton(onClick = { context.startActivity(shareIntent(movie)) }) {
                            Icon(Icons.Outlined.Share, contentDescription = "Share ${movie.title}")
                        }
                    }
                },
            )
        },
    ) { padding ->
        when (uiState) {
            MovieDetailsUiState.Loading -> LoadingBlock(Modifier.padding(padding).padding(16.dp).fillMaxWidth().height(240.dp))
            MovieDetailsUiState.NotFound -> EmptyBlock(
                "This movie isn't in your catalog anymore.",
                Modifier.padding(padding).fillMaxWidth(),
            )
            is MovieDetailsUiState.Loaded -> LazyColumn(
                contentPadding = padding,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
            ) {
                item { Hero(uiState.movie) }
                item {
                    // Readable line length on tablets and unfolded foldables.
                    DetailsBody(
                        state = uiState,
                        onOpenMovie = onOpenMovie,
                        onChoiceChange = { onChoiceChange(uiState.movie, it) },
                        modifier = Modifier.widthIn(max = 720.dp).padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Hero(movie: Movie) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxWidth()
            .widthIn(max = 900.dp)
            .aspectRatio(16f / 10f)
            .background(colors.surfaceContainerLowest),
    ) {
        AsyncImage(
            model = movie.heroImageUrl,
            contentDescription = "${movie.title} poster",
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(colors.surfaceContainer),
            error = ColorPainter(colors.surfaceContainer),
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(0f to Color.Transparent, 0.55f to colors.background.copy(alpha = 0.75f), 1f to colors.background)),
        )
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(0f to Color.Black.copy(alpha = 0.6f), 0.6f to Color.Transparent)))
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
        ) {
            movie.genres.substringBefore(" • ").ifBlank { null }?.let {
                Text(
                    it.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.primaryContainer,
                )
            }
            Text(
                movie.title,
                style = MaterialTheme.typography.displaySmall,
                color = colors.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailsBody(
    state: MovieDetailsUiState.Loaded,
    onOpenMovie: (String) -> Unit,
    onChoiceChange: (MovieChoice?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val movie = state.movie
    val colors = MaterialTheme.colorScheme
    val uriHandler = LocalUriHandler.current

    Column(modifier.padding(top = 12.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            val meta = colors.onSurfaceVariant
            if (movie.year > 0) Text("${movie.year}", style = MaterialTheme.typography.bodyMedium, color = meta)
            movie.runtime?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = meta) }
            movie.rating?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = colors.primaryContainer, modifier = Modifier.size(16.dp))
                    Text(
                        String.format(Locale.US, " %.1f / 10", it),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.onSurface,
                    )
                }
            }
            movie.voteCount?.let { Text("${formatCompactCount(it)} ratings", style = MaterialTheme.typography.bodyMedium, color = meta) }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("YOUR TAKE", style = MaterialTheme.typography.labelSmall, color = colors.outline)
            ChoiceSelector(selected = state.choice, onChoiceChange = onChoiceChange, modifier = Modifier.fillMaxWidth())
        }

        movie.synopsis?.let {
            Text(it, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface.copy(alpha = 0.85f))
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (movie.cast.isNotEmpty()) CreditLine("Starring", movie.cast.take(4).joinToString())
            movie.director?.let { CreditLine("Director", it) }
            val genres = movie.genres.split(" • ").filter { it.isNotBlank() }
            if (genres.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Genres", style = MaterialTheme.typography.bodyMedium, color = colors.outline)
                    genres.forEach { genre ->
                        Text(
                            genre,
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurface,
                            modifier = Modifier
                                .background(colors.onSurface.copy(alpha = 0.1f), CircleShape)
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                        )
                    }
                }
            }
        }

        movie.sourceUrl?.let { url ->
            SectionTitle("Source", "Catalog record")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceContainerLow, MaterialTheme.shapes.medium)
                    .border(1.dp, colors.surfaceContainerHigh, MaterialTheme.shapes.medium)
                    .clickable(onClickLabel = "Open source page") { uriHandler.openUri(url) }
                    .padding(16.dp),
            ) {
                Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, tint = colors.primaryContainer, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "View the full record on ${url.substringAfter("://").substringBefore('/').removePrefix("www.")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (state.similar.isNotEmpty()) {
            SectionTitle("More Like This", "${state.similar.size} picks")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.similar, key = { it.id }) { similar ->
                    MoviePosterCard(
                        movie = similar,
                        choice = state.similarChoices[similar.id],
                        onClick = { onOpenMovie(similar.id) },
                        style = PosterCardStyle.Compact,
                        modifier = Modifier.width(120.dp),
                    )
                }
            }
        }

        val people = buildList {
            movie.director?.let { add(it to "Director") }
            movie.cast.forEach { add(it to "Cast") }
        }
        if (people.isNotEmpty()) {
            SectionTitle("Cast & Crew", "${people.size} key people")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(people, key = { it.first + it.second }) { (name, role) -> PersonCard(name, role) }
            }
        }
    }
}

@Composable
private fun CreditLine(label: String, value: String) {
    val colors = MaterialTheme.colorScheme
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = colors.outline)) { append("$label: ") }
            withStyle(SpanStyle(color = colors.onSurface, fontWeight = FontWeight.Medium)) { append(value) }
        },
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun SectionTitle(title: String, trailing: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        Text(trailing, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
    }
}

/** The catalog has no headshots, so people are shown with initials. */
@Composable
private fun PersonCard(name: String, role: String) {
    val colors = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(104.dp)
            .background(colors.surfaceContainerLow, MaterialTheme.shapes.medium)
            .border(1.dp, colors.surfaceContainerHigh, MaterialTheme.shapes.medium)
            .padding(10.dp),
    ) {
        Box(
            Modifier
                .size(56.dp)
                .background(colors.surfaceContainerHighest, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() },
                style = MaterialTheme.typography.titleMedium,
                color = colors.primaryContainer,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(name, style = MaterialTheme.typography.labelMedium, color = colors.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(role, style = MaterialTheme.typography.bodySmall, color = colors.outline)
    }
}

private fun shareIntent(movie: Movie): Intent {
    val text = buildString {
        append(movie.title)
        if (movie.year > 0) append(" (${movie.year})")
        movie.sourceUrl?.let { append(" — $it") }
    }
    return Intent.createChooser(
        Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text),
        "Share ${movie.title}",
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF10131C, heightDp = 1400)
@Composable
private fun MovieDetailsPreview() {
    PopReelTheme(darkTheme = true) {
        MovieDetailsContent(
            uiState = MovieDetailsUiState.Loaded(
                movie = SampleCatalog.featured.copy(cast = listOf("Timothée Chalamet", "Zendaya"), sourceUrl = "https://en.wikipedia.org/wiki/Dune"),
                choice = MovieChoice.Liked,
                similar = SampleCatalog.criticallyAcclaimed.take(3),
                similarChoices = emptyMap(),
            ),
            onBack = {},
            onOpenMovie = {},
            onChoiceChange = { _, _ -> },
        )
    }
}
