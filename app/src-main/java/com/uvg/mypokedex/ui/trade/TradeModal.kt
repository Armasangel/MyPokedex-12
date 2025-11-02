package com.uvg.mypokedex.ui.trade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.uvg.mypokedex.data.repository.UiState

@Composable
fun TradeModal(
    tradeState: UiState<Map<String, Any>>,
    onDismiss: () -> Unit,
    onAcceptTrade: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (tradeState) {
                    is UiState.Loading -> Text("Loading trade...")
                    is UiState.Success -> {
                        val tradeData = tradeState.data
                        val status = tradeData["status"] as? String
                        Text("Trade Status: $status")
                        if (status == "pending") {
                            Button(onClick = onAcceptTrade) {
                                Text("Accept Trade")
                            }
                        }
                    }
                    is UiState.Error -> Text("Error: ${tradeState.message}")
                    is UiState.Empty -> Text("Trade not found")
                }
            }
        }
    }
}