package id.antasari.p8_multimedia_nimanda.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import id.antasari.p8_multimedia_nimanda.ui.gallery.CameraGalleryScreen
import id.antasari.p8_multimedia_nimanda.ui.home.HomeScreen
import id.antasari.p8_multimedia_nimanda.ui.player.AudioPlayerScreen
import id.antasari.p8_multimedia_nimanda.ui.recorder.AudioRecorderScreen
import id.antasari.p8_multimedia_nimanda.ui.video.VideoPlayerScreen

@Composable
fun AppNavHost(startDestination: String = Screen.Home.route) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // HOME
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        // AUDIO RECORDER
        composable(Screen.AudioRecorder.route) {
            AudioRecorderScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        // AUDIO PLAYER
        composable(
            route = Screen.AudioPlayer.route,
            arguments = listOf(
                navArgument("audioPath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val audioPath = Uri.decode(
                backStackEntry.arguments?.getString("audioPath") ?: ""
            )
            AudioPlayerScreen(
                onBack = { navController.popBackStack() },
                audioPath = audioPath
            )
        }

        // VIDEO PLAYER
        composable(
            route = Screen.VideoPlayer.route,
            arguments = listOf(
                navArgument("videoPath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val videoPath = Uri.decode(
                backStackEntry.arguments?.getString("videoPath") ?: ""
            )
            VideoPlayerScreen(
                onBack = { navController.popBackStack() },
                videoPath = videoPath
            )
        }

        // CAMERA & GALLERY
        composable(Screen.CameraGallery.route) {
            CameraGalleryScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }
    }
}