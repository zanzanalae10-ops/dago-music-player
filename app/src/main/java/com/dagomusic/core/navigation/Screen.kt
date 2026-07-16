package com.dagomusic.core.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Library : Screen("library")
    object Search : Screen("search")
    object Playlists : Screen("playlists")
    object Settings : Screen("settings")
    object NowPlaying : Screen("now_playing")
}
