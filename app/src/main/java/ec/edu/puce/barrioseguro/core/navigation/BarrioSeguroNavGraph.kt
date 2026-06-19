package ec.edu.puce.barrioseguro.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ec.edu.puce.barrioseguro.presentation.detail.DetalleScreen
import ec.edu.puce.barrioseguro.presentation.home.HomeScreen
import ec.edu.puce.barrioseguro.presentation.report.ReporteScreen
import ec.edu.puce.barrioseguro.presentation.map.MapScreen
import ec.edu.puce.barrioseguro.presentation.profile.ProfileScreen

@Composable
fun BarrioSeguroNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToReporte = {
                    navController.navigate(ReporteRoute)
                },
                onNavigateToDetalle = { incidenteId ->
                    navController.navigate(DetalleRoute(incidenteId))
                },
                onNavigateToMap = {
                    navController.navigate(MapRoute) {
                        popUpTo(HomeRoute) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(ProfileRoute) {
                        popUpTo(HomeRoute) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable<ReporteRoute> {
            ReporteScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMap = {
                    navController.navigate(MapRoute) {
                        popUpTo(HomeRoute) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(ProfileRoute) {
                        popUpTo(HomeRoute) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable<DetalleRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<DetalleRoute>()

            DetalleScreen(
                incidenteId = route.incidenteId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMap = {
                    navController.navigate(MapRoute) {
                        popUpTo(HomeRoute) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(ProfileRoute) {
                        popUpTo(HomeRoute) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable<MapRoute> {
            MapScreen(
                onNavigateToHome = {
                    navController.navigate(HomeRoute) {
                        popUpTo(HomeRoute) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(ProfileRoute) {
                        popUpTo(HomeRoute) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable<ProfileRoute> {
            ProfileScreen(
                onNavigateToHome = {
                    navController.navigate(HomeRoute) {
                        popUpTo(HomeRoute) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToMap = {
                    navController.navigate(MapRoute) {
                        popUpTo(HomeRoute) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}