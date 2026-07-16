package com.dagomusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dagomusic.core.navigation.Screen
import com.dagomusic.core.permissions.PermissionHelper
import com.dagomusic.core.ui.screens.*
import com.dagomusic.core.ui.theme.ArabicStrings
import com.dagomusic.core.ui.theme.DagoTheme
import com.dagomusic.core.ui.theme.EnglishStrings
import com.dagomusic.core.ui.theme.LocalAppStrings
import com.dagomusic.core.ui.viewmodel.MusicViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            val musicViewModel: MusicViewModel = viewModel()
            val themeMode by musicViewModel.dataStore.themeMode.collectAsState(initial = "SYSTEM")
            val materialYou by musicViewModel.dataStore.materialYou.collectAsState(initial = true)
            val appLanguage by musicViewModel.dataStore.appLanguage.collectAsState(initial = "en")

            val context = LocalContext.current
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    musicViewModel.scanLibrary()
                }
            }

            LaunchedEffect(Unit) {
                if (!PermissionHelper.hasStoragePermission(context)) {
                    permissionLauncher.launch(PermissionHelper.getRequiredPermission())
                }
            }

            val darkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            val strings = if (appLanguage == "ar") ArabicStrings else EnglishStrings
            val layoutDirection = if (appLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(
                LocalAppStrings provides strings,
                LocalLayoutDirection provides layoutDirection
            ) {
                DagoTheme(darkTheme = darkTheme, dynamicColor = materialYou) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        var currentTab by remember { mutableStateOf<Screen>(Screen.Home) }

                        NavHost(
                            navController = navController,
                            startDestination = Screen.Splash.route
                        ) {
                            composable(Screen.Splash.route) {
                                SplashScreen(
                                    onNavigateToHome = {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Splash.route) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable(Screen.Home.route) {
                                currentTab = Screen.Home
                                MainShell(
                                    viewModel = musicViewModel,
                                    currentScreen = Screen.Home,
                                    onTabSelected = { tab ->
                                        navController.navigate(tab.route)
                                    },
                                    onExpandPlayer = {
                                        navController.navigate(Screen.NowPlaying.route)
                                    }
                                ) { padding ->
                                    HomeScreen(
                                        viewModel = musicViewModel,
                                        onSongSelected = { song ->
                                            musicViewModel.playSong(song)
                                        }
                                    )
                                }
                            }

                            composable(Screen.Library.route) {
                                currentTab = Screen.Library
                                MainShell(
                                    viewModel = musicViewModel,
                                    currentScreen = Screen.Library,
                                    onTabSelected = { tab ->
                                        navController.navigate(tab.route)
                                    },
                                    onExpandPlayer = {
                                        navController.navigate(Screen.NowPlaying.route)
                                    }
                                ) { padding ->
                                    LibraryScreen(
                                        viewModel = musicViewModel,
                                        onSongSelected = { song ->
                                            musicViewModel.playSong(song)
                                        }
                                    )
                                }
                            }

                            composable(Screen.Search.route) {
                                currentTab = Screen.Search
                                MainShell(
                                    viewModel = musicViewModel,
                                    currentScreen = Screen.Search,
                                    onTabSelected = { tab ->
                                        navController.navigate(tab.route)
                                    },
                                    onExpandPlayer = {
                                        navController.navigate(Screen.NowPlaying.route)
                                    }
                                ) { padding ->
                                    SearchScreen(
                                        viewModel = musicViewModel,
                                        onSongSelected = { song ->
                                            musicViewModel.playSong(song)
                                        }
                                    )
                                }
                            }

                            composable(Screen.Playlists.route) {
                                currentTab = Screen.Playlists
                                MainShell(
                                    viewModel = musicViewModel,
                                    currentScreen = Screen.Playlists,
                                    onTabSelected = { tab ->
                                        navController.navigate(tab.route)
                                    },
                                    onExpandPlayer = {
                                        navController.navigate(Screen.NowPlaying.route)
                                    }
                                ) { padding ->
                                    PlaylistsScreen(
                                        viewModel = musicViewModel
                                    )
                                }
                            }

                            composable(Screen.Settings.route) {
                                currentTab = Screen.Settings
                                MainShell(
                                    viewModel = musicViewModel,
                                    currentScreen = Screen.Settings,
                                    onTabSelected = { tab ->
                                        navController.navigate(tab.route)
                                    },
                                    onExpandPlayer = {
                                        navController.navigate(Screen.NowPlaying.route)
                                    }
                                ) { padding ->
                                    SettingsScreen(
                                        viewModel = musicViewModel
                                    )
                                }
                            }

                            composable(Screen.NowPlaying.route) {
                                NowPlayingScreen(
                                    viewModel = musicViewModel,
                                    onBack = {
                                        navController.popBackStack()
                                    },
                                    onNavigateToEqualizer = {
                                        navController.navigate("equalizer")
                                    }
                                )
                            }

                            composable("equalizer") {
                                EqualizerScreen(
                                    viewModel = musicViewModel,
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
