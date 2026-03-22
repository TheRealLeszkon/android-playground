package com.example.androidplayground.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun NavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {

        // Home
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }

        // Feature List
        composable(Screen.FeatureList.route) {
            FeatureListScreen(navController)
        }

        // Demo List
        composable(Screen.DemoList.route) {
            DemoListScreen(navController)
        }

        // Feature Detail (Dynamic)
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

        // Demo Detail (Dynamic)
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

@Composable
fun DemoDetailScreen(demoName: String, navController: NavHostController) {
    TODO("Not yet implemented")
}

@Composable
fun FeatureDetailScreen(featureName: String, navController: NavHostController) {
    TODO("Not yet implemented")
}

@Composable
fun DemoListScreen(x0: NavHostController) {
    TODO("Not yet implemented")
}

@Composable
fun FeatureListScreen(x0: NavHostController) {
    TODO("Not yet implemented")
}

@Composable
fun HomeScreen(x0: NavHostController) {
    TODO("Not yet implemented")
}