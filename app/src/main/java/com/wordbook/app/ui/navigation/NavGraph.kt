package com.wordbook.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wordbook.app.data.entity.WordEntity
import com.wordbook.app.ui.example.ExampleScreen
import com.wordbook.app.ui.detail.DetailScreen
import com.wordbook.app.ui.history.HistoryScreen
import com.wordbook.app.ui.home.HomeScreen
import com.wordbook.app.ui.study.StudyScreen
import com.wordbook.app.ui.wordlist.WordListScreen
import com.google.gson.Gson

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "首页", Icons.Filled.Home)
    data object History : Screen("history", "历史", Icons.Filled.List)
}

val bottomTabs = listOf(Screen.Home, Screen.History)

fun wordListToJson(words: List<WordEntity>): String = Gson().toJson(words.map { it.id })

fun jsonToWordIds(json: String): List<Long> =
    try { Gson().fromJson(json, Array<Long>::class.java).toList() } catch (_: Exception) { emptyList() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomTabs.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color.Transparent) {
                    bottomTabs.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(0)) },
            exitTransition = { fadeOut(animationSpec = tween(0)) },
            popEnterTransition = { fadeIn(animationSpec = tween(0)) },
            popExitTransition = { fadeOut(animationSpec = tween(0)) }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onStartStudy = { words ->
                        navController.navigate("wordlist/learning/${wordListToJson(words)}")
                    },
                    onStartReview = { words ->
                        navController.navigate("wordlist/review/${wordListToJson(words)}")
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    onWordClick = { id -> navController.navigate("detail/$id") },
                    onStartReview = { words ->
                        navController.navigate("study/review/${wordListToJson(words)}")
                    }
                )
            }

            composable(
                route = "wordlist/{mode}/{wordIds}",
                arguments = listOf(
                    navArgument("mode") { type = NavType.StringType },
                    navArgument("wordIds") { type = NavType.StringType }
                )
            ) { entry ->
                val mode = entry.arguments?.getString("mode") ?: "learning"
                val json = entry.arguments?.getString("wordIds") ?: "[]"
                WordListScreen(
                    wordIdsJson = json,
                    mode = mode,
                    onNext = { m, j -> navController.navigate("study/$m/$j") }
                )
            }

            composable(
                route = "study/{mode}/{wordIds}",
                arguments = listOf(
                    navArgument("mode") { type = NavType.StringType },
                    navArgument("wordIds") { type = NavType.StringType }
                )
            ) { entry ->
                val mode = entry.arguments?.getString("mode") ?: "learning"
                val json = entry.arguments?.getString("wordIds") ?: "[]"
                StudyScreen(
                    mode = mode,
                    wordIdsJson = json,
                    onStartExample = { words -> navController.navigate("example/${wordListToJson(words)}") },
                    onFinish = { navController.popBackStack(Screen.Home.route, inclusive = false) }
                )
            }

            composable(
                route = "example/{wordIds}",
                arguments = listOf(navArgument("wordIds") { type = NavType.StringType })
            ) { entry ->
                val json = entry.arguments?.getString("wordIds") ?: "[]"
                ExampleScreen(
                    wordIdsJson = json,
                    onFinish = { navController.popBackStack(Screen.Home.route, inclusive = false) }
                )
            }

            composable(
                route = "detail/{wordId}",
                arguments = listOf(navArgument("wordId") { type = NavType.LongType })
            ) { entry ->
                val wordId = entry.arguments?.getLong("wordId") ?: 0L
                DetailScreen(wordId = wordId, onBack = { navController.popBackStack() })
            }
        }
    }
}
