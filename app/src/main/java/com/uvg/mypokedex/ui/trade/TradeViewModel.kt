package com.uvg.mypokedex.ui.trade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uvg.mypokedex.data.repository.PokemonRepository
import com.uvg.mypokedex.data.repository.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TradeViewModel(private val pokemonRepository: PokemonRepository) : ViewModel() {

    private val _tradeState = MutableStateFlow<UiState<Map<String, Any>>>(UiState.Loading)
    val tradeState: StateFlow<UiState<Map<String, Any>>> = _tradeState

    private val _createTradeState = MutableStateFlow<UiState<String>>(UiState.Loading)
    val createTradeState: StateFlow<UiState<String>> = _createTradeState

    fun createTrade(offeredPokemonId: Int) {
        viewModelScope.launch {
            val result = pokemonRepository.createTradeRequest(offeredPokemonId)
            result.fold(
                onSuccess = { tradeId -> _createTradeState.value = UiState.Success(tradeId) },
                onFailure = { exception -> _createTradeState.value = UiState.Error(exception.message ?: "Unknown error") }
            )
        }
    }

    fun listenForTradeUpdates(tradeId: String) {
        viewModelScope.launch {
            pokemonRepository.listenForTradeUpdates(tradeId).collect {
                _tradeState.value = it
            }
        }
    }

    fun acceptTrade(tradeId: String, offeredPokemonId: Int) {
        viewModelScope.launch {
            val result = pokemonRepository.acceptTrade(tradeId, offeredPokemonId)
            // The result of the trade acceptance will be reflected in the tradeState flow
        }
    }
}