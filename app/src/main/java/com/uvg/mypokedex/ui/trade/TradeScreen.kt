package com.uvg.mypokedex.ui.trade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uvg.mypokedex.data.repository.UiState
import com.uvg.mypokedex.ui.auth.AuthViewModel
import com.uvg.mypokedex.ui.auth.LoginModal

@Composable
fun TradeScreen(
    viewModel: TradeViewModel,
    authViewModel: AuthViewModel
) {
    val tradeState by viewModel.tradeState.collectAsState()
    val createTradeState by viewModel.createTradeState.collectAsState()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    var tradeId by remember { mutableStateOf("") }
    var pokemonToGiveId by remember { mutableStateOf("") }
    var pokemonToReceiveId by remember { mutableStateOf("") }

    if (isAuthenticated == false) {
        LoginModal(
            onDismiss = {},
            onLogin = { authViewModel.signInAnonymously() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Pokemon Trade")

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = pokemonToGiveId,
            onValue-Change = { pokemonToGiveId = it },
            label = { Text("Pokemon to give (ID)") }
        )
        Button(onClick = { viewModel.createTrade(pokemonToGiveId.toInt()) }) {
            Text("Create Trade")
        }

        (createTradeState as? UiState.Success)?.let {
            Text("Trade ID: ${it.data}")
        }

        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = tradeId,
            onValue-Change = { tradeId = it },
            label = { Text("Enter Trade ID") }
        )
        TextField(
            value = pokemonToReceiveId,
            onValueChange = { pokemonToReceiveId = it },
            label = { Text("Pokemon to offer (ID)") }
        )
        Button(onClick = { viewModel.acceptTrade(tradeId, pokemonToReceiveId.toInt()) }) {
            Text("Accept Trade")
        }

        (tradeState as? UiState.Success)?.let {
            Text("Trade status: ${it.data["status"]}")
        }
    }
}