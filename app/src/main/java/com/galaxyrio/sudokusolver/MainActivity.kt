package com.galaxyrio.sudokusolver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Surface


import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.galaxyrio.sudokusolver.ui.theme.SudokuSolverTheme
import com.galaxyrio.sudokusolver.ui.screen.InfoScreen
import com.galaxyrio.sudokusolver.ui.screen.PlayMenuScreen
import com.galaxyrio.sudokusolver.ui.screen.play.SudokuGameScreen
import com.galaxyrio.sudokusolver.ui.screen.SettingsScreen
import com.galaxyrio.sudokusolver.ui.screen.SettingsCategory
import com.galaxyrio.sudokusolver.ui.screen.Difficulty

import com.galaxyrio.sudokusolver.ui.screen.settings.AppearanceSettingsScreen
import com.galaxyrio.sudokusolver.ui.screen.settings.GameSettingsScreen
import com.galaxyrio.sudokusolver.ui.screen.settings.AssistanceSettingsScreen
import com.galaxyrio.sudokusolver.ui.screen.settings.FilesSettingsScreen
import com.galaxyrio.sudokusolver.ui.screen.settings.LanguageSettingsScreen
import com.galaxyrio.sudokusolver.ui.screen.settings.AboutSettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SudokuSolverTheme {
                SudokuSolverApp()
            }
        }
    }
}

@PreviewScreenSizes
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SudokuSolverApp() {
    val navController = rememberNavController()
    val motionScheme = MaterialTheme.motionScheme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = "home",
                enterTransition = {
                    slideInHorizontally(
                        animationSpec = motionScheme.defaultSpatialSpec(),
                        initialOffsetX = { it }
                    ) + fadeIn(
                        animationSpec = motionScheme.defaultEffectsSpec(),
                        initialAlpha = 1f
                    )
                },

                exitTransition = {
                    slideOutHorizontally(
                        animationSpec = motionScheme.defaultSpatialSpec(),
                        targetOffsetX = { -it / 3 }
                    ) + fadeOut(
                        animationSpec = motionScheme.defaultEffectsSpec(),
                        targetAlpha = 1f
                    )
                },

                popEnterTransition = {
                    slideInHorizontally(
                        animationSpec = motionScheme.defaultSpatialSpec(),
                        initialOffsetX = { -it / 3 }
                    ) + fadeIn(
                        animationSpec = motionScheme.defaultEffectsSpec(),
                        initialAlpha = 1f
                    )
                },

                popExitTransition = {
                    slideOutHorizontally(
                        animationSpec = motionScheme.defaultSpatialSpec(),
                        targetOffsetX = { it }
                    ) + fadeOut(
                        animationSpec = motionScheme.defaultEffectsSpec(),
                        targetAlpha = 0f
                    )
                },

                sizeTransform = {
                    SizeTransform(
                        clip = false,
                        sizeAnimationSpec = { _, _ ->
                            motionScheme.defaultSpatialSpec()
                        }
                    )
                }
            ) {
                composable(
                    route = "home",
                ) {
                    HomeScreen(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable,
                        onStartGame = { difficulty ->
                            navController.navigate("game/${difficulty.name}")
                        },
                        onContinueGame = { gameId ->
                            navController.navigate("game/MEDIUM?gameId=$gameId")
                        },
                        onNavigateToSettings = { category ->
                            navController.navigate("settings/${category.name}")
                        }
                    )
                }

                composable(
                    route = "game/{difficulty}?gameId={gameId}",
                    arguments = listOf(
                        navArgument("difficulty") { type = NavType.StringType },
                        navArgument("gameId") {
                            type = NavType.LongType
                            defaultValue = -1L
                        }
                    )
                ) { backStackEntry ->
                    val difficultyStr = backStackEntry.arguments?.getString("difficulty") ?: "MEDIUM"
                    val gameIdArg = backStackEntry.arguments?.getLong("gameId") ?: -1L

                    val difficulty = try {
                        Difficulty.valueOf(difficultyStr)
                    } catch (_: Exception) {
                        Difficulty.MEDIUM
                    }
                    val gameId = if (gameIdArg == -1L) null else gameIdArg

                    SudokuGameScreen(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable,
                        difficulty = difficulty,
                        gameId = gameId,
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                composable(
                    route = "settings/{category}",
                    arguments = listOf(navArgument("category") { type = NavType.StringType }),

                    ) { backStackEntry ->
                    val categoryStr = backStackEntry.arguments?.getString("category")
                    val category = SettingsCategory.entries.find { it.name == categoryStr }

                    val onBack: () -> Unit = { navController.popBackStack() }
                    val modifier = Modifier.fillMaxSize()

                    if (category != null) {
                        // Determine specific settings screen
                        when (category) {
                            SettingsCategory.APPEARANCE -> AppearanceSettingsScreen(onBack, modifier)
                            SettingsCategory.GAME -> GameSettingsScreen(onBack, modifier)
                            SettingsCategory.ASSISTANCE -> AssistanceSettingsScreen(onBack, modifier)
                            SettingsCategory.FILES -> FilesSettingsScreen(onBack, modifier)
                            SettingsCategory.LANGUAGE -> LanguageSettingsScreen(onBack, modifier)
                            SettingsCategory.ABOUT -> AboutSettingsScreen(onBack, modifier)
                        }
                    }
                }
                composable(
                    route = "info",
                ) {
                    InfoScreen(
                        modifier = Modifier.fillMaxSize()
                    )

                }


            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onStartGame: (Difficulty) -> Unit,
    onContinueGame: (String) -> Unit,
    onNavigateToSettings: (SettingsCategory) -> Unit
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.PLAY) }
    var gameDifficulty by rememberSaveable { mutableStateOf(Difficulty.MEDIUM) }
    val motionScheme = MaterialTheme.motionScheme

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        val modifier = Modifier.fillMaxSize()
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            AnimatedContent(
                targetState = currentDestination,
                label = "main_nav_transition",
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        (slideInHorizontally(
                            animationSpec = motionScheme.defaultSpatialSpec(),
                            initialOffsetX = { it }
                        ) + fadeIn(
                            animationSpec = motionScheme.defaultEffectsSpec()
                        )) togetherWith (
                            slideOutHorizontally(
                                animationSpec = motionScheme.fastSpatialSpec(),
                                targetOffsetX = { -it }
                            ) + fadeOut(
                                animationSpec = motionScheme.fastEffectsSpec()
                            )
                        )
                    } else {
                        (slideInHorizontally(
                            animationSpec = motionScheme.defaultSpatialSpec(),
                            initialOffsetX = { -it }
                        ) + fadeIn(
                            animationSpec = motionScheme.defaultEffectsSpec()
                        )) togetherWith (
                            slideOutHorizontally(
                                animationSpec = motionScheme.fastSpatialSpec(),
                                targetOffsetX = { it }
                            ) + fadeOut(
                                animationSpec = motionScheme.fastEffectsSpec()
                            )
                        )
                    }
                }
            ) { targetScreen ->
                when (targetScreen) {
                    AppDestinations.INFO -> InfoScreen(modifier)
                    AppDestinations.PLAY -> PlayMenuScreen(
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        modifier = modifier,
                        initialDifficulty = gameDifficulty,
                        onStartGame = { selectedDifficulty ->
                            gameDifficulty = selectedDifficulty
                            onStartGame(selectedDifficulty)
                        },
                        onContinueGame = { gameId ->
                            onContinueGame(gameId)
                        }
                    )
                    AppDestinations.SETTINGS -> SettingsScreen(
                        onNavigateTo = onNavigateToSettings,
                        modifier = modifier
                    )
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    INFO("Info", Icons.Default.Info),
    PLAY("Play", Icons.Default.SportsEsports),
    SETTINGS("Setting", Icons.Default.Settings),
}
