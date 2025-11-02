package com.uvg.mypokedex.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uvg.mypokedex.data.repository.PokemonRepository

class FavoritesViewModelFactory(private val pokemonRepository: PokemonRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(pokemonRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}