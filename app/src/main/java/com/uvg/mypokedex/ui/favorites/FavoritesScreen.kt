package com.uvg.mypokedex.ui.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.uvg.mypokedex.data.repository.UiState
import com.uvg.mypokedex.ui.auth.AuthViewModel
import com.uvg.mypokedex.ui.auth.LoginModal
import com.uvg.mypokedex.ui.components.PokemonCard

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    authViewModel: AuthViewModel
) {
    val favoritesState by viewModel.favoritesState.collectAsState()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    if (isAuthenticated == false) {
        LoginModal(
            onDismiss = {},
            onLogin = { authViewModel.signInAnonymously() }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("My Favorite Pokemons")
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = favoritesState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Text("No favorite pokemons yet.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn {
                            items(state.data) { pokemon ->
                                PokemonCard(pokemon = pokemon, onClick = {})
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Empty -> {
                    Text("No favorite pokemons yet.", modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}