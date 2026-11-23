package com.emaxsoftware.popreel.ui.app

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emaxsoftware.popreel.core.model.ThemeMode
import com.emaxsoftware.popreel.feature.arcade.ArcadeScreen
import com.emaxsoftware.popreel.feature.details.MovieDetailsScreen
import com.emaxsoftware.popreel.feature.explore.ExploreScreen
import com.emaxsoftware.popreel.feature.genie.GenieScreen
import com.emaxsoftware.popreel.feature.puzzle.PosterPuzzleScreen
import com.emaxsoftware.popreel.feature.settings.SettingsScreen
import com.emaxsoftware.popreel.feature.swipe.SwipeScreen
import com.emaxsoftware.popreel.feature.trivia.TriviaScreen
import com.emaxsoftware.popreel.ui.components.PopReelBottomBar
import com.emaxsoftware.popreel.ui.components.PopReelDestination
import com.emaxsoftware.popreel.ui.components.PopReelNavigationRail
import com.emaxsoftware.popreel.ui.components.PopReelTopBar
import com.emaxsoftware.popreel.ui.navigation.Screen
import com.emaxsoftware.popreel.ui.navigation.ScreenStackSaver
import com.emaxsoftware.popreel.ui.theme.PopReelTheme
import kotlinx.coroutines.launch

@Composable
fun PopReelApp(viewModel: PopReelAppViewModel = hiltViewModel()) {
    LaunchedEffect(viewModel) { viewModel.onAppStarted() }
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val darkTheme = when (settings.themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    SystemBarAppearance(darkTheme)

    PopReelTheme(darkTheme = darkTheme) {
        AppContent(soundEnabled = settings.gameSoundEnabled, onToggleSound = viewModel::onToggleSound)
    }
}

/** Status/navigation bar icons follow the app theme, not just the system setting. */
@Composable
private fun SystemBarAppearance(darkTheme: Boolean) {
    val activity = LocalContext.current as? Activity
    LaunchedEffect(activity, darkTheme) {
        val componentActivity = activity as? ComponentActivity ?: return@LaunchedEffect
        val style = if (darkTheme) {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        }
        componentActivity.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }
}

@Composable
private fun AppContent(soundEnabled: Boolean, onToggleSound: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf(PopReelDestination.Explore) }
    val stack: SnapshotStateList<Screen> = rememberSaveable(saver = ScreenStackSaver) { SnapshotStateList() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showMessage: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }
    val push: (Screen) -> Unit = { stack.add(it) }
    val pop: () -> Unit = { if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex) }
    val openMovie: (String) -> Unit = { push(Screen.MovieDetails(it)) }

    BackHandler(enabled = stack.isNotEmpty(), onBack = pop)

    Box(Modifier.fillMaxSize()) {
        when (val top = stack.lastOrNull()) {
            null -> TabsScaffold(
                tab = tab,
                onTabSelect = { tab = it },
                soundEnabled = soundEnabled,
                onToggleSound = onToggleSound,
                onOpenSettings = { push(Screen.Settings) },
                onOpenMovie = openMovie,
                onPlayTrivia = { push(Screen.Trivia) },
                onPlayPuzzle = { push(Screen.PosterPuzzle) },
                onOpenSwipe = { tab = PopReelDestination.Swipe },
                onShowMessage = showMessage,
            )

            is Screen.MovieDetails -> MovieDetailsScreen(
                movieId = top.movieId,
                onBack = pop,
                onOpenMovie = openMovie,
                onShowMessage = showMessage,
            )

            Screen.Trivia -> TriviaScreen(onExit = pop)
            Screen.PosterPuzzle -> PosterPuzzleScreen(onExit = pop)
            Screen.Settings -> SettingsScreen(onBack = pop, onShowMessage = showMessage)
        }

        SnackbarHost(
            snackbarHostState,
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = if (stack.isEmpty()) 80.dp else 8.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabsScaffold(
    tab: PopReelDestination,
    onTabSelect: (PopReelDestination) -> Unit,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMovie: (String) -> Unit,
    onPlayTrivia: () -> Unit,
    onPlayPuzzle: () -> Unit,
    onOpenSwipe: () -> Unit,
    onShowMessage: (String) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // A top bar collapsed by scrolling one tab shouldn't stay hidden on the next.
    LaunchedEffect(tab) {
        scrollBehavior.state.heightOffset = 0f
        scrollBehavior.state.contentOffset = 0f
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
    // Material's medium width class starts at 600dp: switch to a navigation rail there.
    val useRail = maxWidth >= 600.dp
    Row(Modifier.fillMaxSize()) {
        if (useRail) PopReelNavigationRail(current = tab, onNavigate = onTabSelect)
        Scaffold(
            modifier = Modifier
                .weight(1f)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                PopReelTopBar(
                    soundEnabled = soundEnabled,
                    onSoundToggle = onToggleSound,
                    onSettingsClick = onOpenSettings,
                    scrollBehavior = scrollBehavior,
                )
            },
            bottomBar = { if (!useRail) PopReelBottomBar(current = tab, onNavigate = onTabSelect) },
        ) { innerPadding ->
            when (tab) {
                PopReelDestination.Explore -> ExploreScreen(innerPadding, onOpenMovie)
                PopReelDestination.Swipe -> SwipeScreen(innerPadding, onOpenMovie, onShowMessage)
                PopReelDestination.Genie -> GenieScreen(innerPadding, onOpenMovie, onOpenSwipe, onShowMessage)
                PopReelDestination.Arcade -> ArcadeScreen(innerPadding, onPlayTrivia, onPlayPuzzle)
            }
        }
    }
    }
}
