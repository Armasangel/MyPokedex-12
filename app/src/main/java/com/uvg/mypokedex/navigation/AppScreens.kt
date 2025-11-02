package com.uvg.mypokedex.navigation

sealed class AppScreens(val route: String) {
    object Home : AppScreens("home")
    object Favorites : AppScreens("favorites")
    object Trade : AppScreens("trade")
    object Detail : AppScreens("detail/{pokemonId}") {
        fun createRoute(pokemonId: Int) = "detail/$pokemonId"
    }
}
