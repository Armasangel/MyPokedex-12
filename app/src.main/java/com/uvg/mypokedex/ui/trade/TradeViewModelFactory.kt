package com.uvg.mypokedex.ui.trade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uvg.mypokedex.data.repository.PokemonRepository

class TradeViewModelFactory(private val pokemonRepository: PokemonRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TradeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TradeViewModel(pokemonRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}