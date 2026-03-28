package com.example.androidplayground.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.androidplayground.ui.demos.DemoDetailScreen
import com.example.androidplayground.ui.demos.DemoListScreen
import com.example.androidplayground.ui.features.FeatureDetailScreen
import com.example.androidplayground.ui.features.FeatureListScreen
import com.example.androidplayground.ui.home.HomeScreen

@Composable
fun NavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {

        composable(Screen.Home.route) {
            HomeScreen(navController)
        }

        composable(Screen.FeatureList.route) {
            FeatureListScreen(navController)
        }

        composable(Screen.DemoList.route) {
            DemoListScreen(navController)
        }

        composable(
            route = Screen.FeatureDetail.route,
            arguments = listOf(
                navArgument("featureName") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val featureName =
                backStackEntry.arguments?.getString("featureName") ?: ""

            FeatureDetailScreen(
                featureName = featureName,
                navController = navController
            )
        }

        composable(
            route = Screen.DemoDetail.route,
            arguments = listOf(
                navArgument("demoName") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val demoName =
                backStackEntry.arguments?.getString("demoName") ?: ""

            DemoDetailScreen(
                demoName = demoName,
                navController = navController
            )
        }
    }
}