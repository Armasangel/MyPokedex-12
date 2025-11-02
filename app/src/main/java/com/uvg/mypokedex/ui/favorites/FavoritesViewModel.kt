package com.uvg.mypokedex.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uvg.mypokedex.data.model.Pokemon
import com.uvg.mypokedex.data.repository.PokemonRepository
import com.uvg.mypokedex.data.repository.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(private val pokemonRepository: PokemonRepository) : ViewModel() {

    private val _favoritesState = MutableStateFlow<UiState<List<Pokemon>>>(UiState.Loading)
    val favoritesState: StateFlow<UiState<List<Pokemon>>> = _favoritesState

    init {
        getFavoritePokemons()
    }

    private fun getFavoritePokemons() {
        viewModelScope.launch {
            pokemonRepository.getFavoritePokemons().collect {
                _favoritesState.value = it
            }
        }
    }
}