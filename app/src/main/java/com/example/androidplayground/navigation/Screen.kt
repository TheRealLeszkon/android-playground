package com.example.androidplayground.navigation

sealed class Screen(val route: String) {

    object Home : Screen("home")
    object FeatureList : Screen("feature_list")
    object DemoList : Screen("demo_list")

    object FeatureDetail : Screen("feature_detail/{featureName}") {
        fun createRoute(featureName: String) =
            "feature_detail/$featureName"
    }

    object DemoDetail : Screen("demo_detail/{demoName}") {
        fun createRoute(demoName: String) =
            "demo_detail/$demoName"
    }
}

// Top-level helper functions
fun createFeatureRoute(featureName: String): String =
    Screen.FeatureDetail.createRoute(featureName)

fun createDemoRoute(demoName: String): String =
    Screen.DemoDetail.createRoute(demoName)