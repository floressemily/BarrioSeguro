package ec.edu.puce.barrioseguro.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ec.edu.puce.barrioseguro.presentation.detail.DetalleScreen
import ec.edu.puce.barrioseguro.presentation.home.HomeScreen
import ec.edu.puce.barrioseguro.presentation.report.ReporteScreen

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
                }
            )
        }

        composable<ReporteRoute> {
            ReporteScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<DetalleRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<DetalleRoute>()

            DetalleScreen(
                incidenteId = route.incidenteId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}