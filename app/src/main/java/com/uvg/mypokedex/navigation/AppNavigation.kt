package com.uvg.mypokedex.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.uvg.mypokedex.ui.detail.DetailScreen
import com.uvg.mypokedex.ui.detail.DetailViewModel
import com.uvg.mypokedex.ui.detail.DetailViewModelFactory
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.uvg.mypokedex.data.repository.AuthRepository
import com.uvg.mypokedex.data.repository.PokemonRepository
import com.uvg.mypokedex.ui.detail.DetailScreen
import com.uvg.mypokedex.ui.detail.DetailViewModel
import com.uvg.mypokedex.ui.detail.DetailViewModelFactory
import com.uvg.mypokedex.ui.auth.AuthViewModel
import com.uvg.mypokedex.ui.auth.AuthViewModelFactory
import com.uvg.mypokedex.ui.favorites.FavoritesScreen
import com.uvg.mypokedex.ui.favorites.FavoritesViewModel
import com.uvg.mypokedex.ui.favorites.FavoritesViewModelFactory
import com.uvg.mypokedex.ui.features.home.HomeScreen
import com.uvg.mypokedex.ui.features.home.HomeViewModel
import com.uvg.mypokedex.ui.features.home.HomeViewModelFactory
import com.uvg.mypokedex.ui.trade.TradeScreen
import com.uvg.mypokedex.ui.trade.TradeViewModel
import com.uvg.mypokedex.ui.trade.TradeViewModelFactory

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current.applicationContext
    val pokemonRepository = remember { PokemonRepository(context) }
    val authRepository = remember { AuthRepository() }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentRoute == AppScreens.Home.route,
                    onClick = {
                        navController.navigate(AppScreens.Home.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                    label = { Text("Favorites") },
                    selected = currentRoute == AppScreens.Favorites.route,
                    onClick = {
                        navController.navigate(AppScreens.Favorites.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppScreens.Home.route
        ) {
            composable(route = AppScreens.Home.route) {
                val viewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(
                        application = context as android.app.Application
                    )
                )

                HomeScreen(
                    viewModel = viewModel,
                    onPokemonClick = { pokemonId ->
                        navController.navigate(AppScreens.Detail.createRoute(pokemonId))
                    },
                    onTradeClick = {
                        navController.navigate(AppScreens.Trade.route)
                    }
                )
            }

            composable(route = AppScreens.Favorites.route) {
                val viewModel: FavoritesViewModel = viewModel(
                    factory = FavoritesViewModelFactory(pokemonRepository)
                )
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(authRepository)
                )
                FavoritesScreen(viewModel = viewModel, authViewModel = authViewModel)
            }

            composable(route = AppScreens.Trade.route) {
                val viewModel: TradeViewModel = viewModel(
                    factory = TradeViewModelFactory(pokemonRepository)
                )
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(authRepository)
                )
                TradeScreen(viewModel = viewModel, authViewModel = authViewModel)
            }

        composable(
            route = AppScreens.Detail.route,
            arguments = listOf(
                navArgument("pokemonId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val pokemonId = backStackEntry.arguments?.getInt("pokemonId") ?: 1

            val viewModel: DetailViewModel = viewModel(
                factory = DetailViewModelFactory(
                    application = androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
                )
            )

            DetailScreen(
                pokemonId = pokemonId,
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}