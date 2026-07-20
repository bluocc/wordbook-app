package com.wordbook.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.wordbook.app.ui.record.RecordScreen
import com.wordbook.app.ui.study.StudyScreen
import com.google.gson.Gson

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "首页", Icons.Filled.Home)
    data object History : Screen("history", "历史", Icons.Filled.List)
    data object Record : Screen("record", "记录", Icons.Filled.CalendarMonth)
}

val bottomTabs = listOf(Screen.Home, Screen.History, Screen.Record)

fun wordListToJson(words: List<WordEntity>): String {
    return Gson().toJson(words.map { it.id })
}

fun jsonToWordIds(json: String): List<Long> {
    return try {
        Gson().fromJson(json, Array<Long>::class.java).toList()
    } catch (e: Exception) {
        emptyList()
    }
}

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
                NavigationBar {
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
        NavHostControllerWithData(
            navController = navController,
            innerPadding = innerPadding
        )
    }
}

@Composable
private fun NavHostControllerWithData(
    navController: NavHostController,
    innerPadding: androidx.compose.foundation.layout.PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onStartStudy = { words ->
                    val json = wordListToJson(words)
                    navController.navigate("study/learning/$json")
                },
                onStartReview = { words ->
                    val json = wordListToJson(words)
                    navController.navigate("study/review/$json")
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onWordClick = { wordId ->
                    navController.navigate("detail/$wordId")
                }
            )
        }

        composable(Screen.Record.route) {
            RecordScreen()
        }

        composable(
            route = "study/{mode}/{wordIds}",
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
                navArgument("wordIds") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "learning"
            val wordIdsJson = backStackEntry.arguments?.getString("wordIds") ?: "[]"
            StudyScreen(
                mode = mode,
                wordIdsJson = wordIdsJson,
                onStartExample = { words ->
                    val json = wordListToJson(words)
                    navController.navigate("example/$json")
                },
                onFinish = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }

        composable(
            route = "example/{wordIds}",
            arguments = listOf(
                navArgument("wordIds") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val wordIdsJson = backStackEntry.arguments?.getString("wordIds") ?: "[]"
            ExampleScreen(
                wordIdsJson = wordIdsJson,
                onFinish = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }

        composable(
            route = "detail/{wordId}",
            arguments = listOf(
                navArgument("wordId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val wordId = backStackEntry.arguments?.getLong("wordId") ?: 0L
            DetailScreen(wordId = wordId, onBack = { navController.popBackStack() })
        }
    }
}
