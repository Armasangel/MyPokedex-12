package com.uvg.mypokedex.data.repository

import android.content.Context
import com.uvg.mypokedex.data.connectivity.ConnectivityObserver
import com.uvg.mypokedex.data.connectivity.NetworkConnectivityObserver
import com.uvg.mypokedex.data.datastore.UserPreferences
import com.uvg.mypokedex.data.local.database.PokemonDatabase
import com.uvg.mypokedex.data.local.entity.toCache
import com.uvg.mypokedex.data.local.entity.toDomain
import com.uvg.mypokedex.data.model.Pokemon
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.uvg.mypokedex.data.remote.PokemonRemoteDataSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PokemonRepository(
    private val context: Context,
    private val remoteDataSource: PokemonRemoteDataSource = PokemonRemoteDataSource(),
    private val connectivityObserver: ConnectivityObserver = NetworkConnectivityObserver(context),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val database = PokemonDatabase.getDatabase(context)
    private val pokemonDao = database.pokemonDao()
    private val userPreferences = UserPreferences(context)

    val isConnected: Flow<Boolean> = connectivityObserver.observe().map { status ->
        status == ConnectivityObserver.Status.Available
    }

    fun getSortPreferences(): Flow<Pair<String, Boolean>> {
        return combine(
            userPreferences.sortType,
            userPreferences.isAscending
        ) { sortType, isAscending ->
            Pair(sortType, isAscending)
        }
    }

    suspend fun saveSortPreferences(sortType: String, isAscending: Boolean) {
        userPreferences.saveSortPreferences(sortType, isAscending)
    }

    fun getPokemonList(limit: Int = 20, offset: Int = 0): Flow<UiState<List<Pokemon>>> = flow {
        emit(UiState.Loading)

        try {
            val cachedCount = pokemonDao.getPokemonCount()

            if (cachedCount > 0) {
                pokemonDao.getAllPokemon().collect { cachedPokemon ->
                    val domainList = cachedPokemon.map { it.toDomain() }
                    if (domainList.isNotEmpty()) {
                        emit(UiState.Success(domainList))
                    }
                }
            }

            if (connectivityObserver.isConnected()) {
                val result = remoteDataSource.getPokemonList(limit, offset)

                result.fold(
                    onSuccess = { pokemonList ->
                        val cachedList = pokemonList.map { it.toCache() }
                        pokemonDao.insertAllPokemon(cachedList)

                        if (pokemonList.isNotEmpty()) {
                            emit(UiState.Success(pokemonList))
                        } else {
                            emit(UiState.Empty)
                        }
                    },
                    onFailure = { exception ->
                        if (cachedCount == 0) {
                            emit(UiState.Error(exception.message ?: "Error desconocido"))
                        }
                    }
                )
            } else if (cachedCount == 0) {
                emit(UiState.Error("No hay conexión a internet y no hay datos en caché"))
            }

        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Error desconocido"))
        }
    }

    fun getPokemonById(id: Int): Flow<UiState<Pokemon>> = flow {
        emit(UiState.Loading)

        try {
            val cachedPokemon = pokemonDao.getPokemonById(id)

            if (cachedPokemon != null) {
                emit(UiState.Success(cachedPokemon.toDomain()))
            }

            if (connectivityObserver.isConnected()) {
                val result = remoteDataSource.getPokemonById(id)

                result.fold(
                    onSuccess = { pokemon ->
                        pokemonDao.insertPokemon(pokemon.toCache())
                        emit(UiState.Success(pokemon))
                    },
                    onFailure = { exception ->
                        if (cachedPokemon == null) {
                            emit(UiState.Error(exception.message ?: "Error desconocido"))
                        }
                    }
                )
            } else if (cachedPokemon == null) {
                emit(UiState.Error("No hay conexión a internet y no hay datos en caché"))
            }

        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Error desconocido"))
        }
    }

    fun searchPokemonByName(name: String): Flow<UiState<Pokemon>> = flow {
        emit(UiState.Loading)

        try {
            val cachedPokemon = pokemonDao.searchPokemonByName(name.lowercase())

            if (cachedPokemon != null) {
                emit(UiState.Success(cachedPokemon.toDomain()))
            }

            if (connectivityObserver.isConnected()) {
                val result = remoteDataSource.searchPokemonByName(name)

                result.fold(
                    onSuccess = { pokemon ->
                        pokemonDao.insertPokemon(pokemon.toCache())
                        emit(UiState.Success(pokemon))
                    },
                    onFailure = { exception ->
                        if (cachedPokemon == null) {
                            emit(UiState.Error(exception.message ?: "Pokémon no encontrado"))
                        }
                    }
                )
            } else if (cachedPokemon == null) {
                emit(UiState.Error("No hay conexión a internet y no hay datos en caché"))
            }

        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Error al buscar"))
        }
    }

    suspend fun forceSync(): Result<Unit> {
        return if (connectivityObserver.isConnected()) {
            try {
                val result = remoteDataSource.getPokemonList(limit = 100, offset = 0)
                result.fold(
                    onSuccess = { pokemonList ->
                        val cachedList = pokemonList.map { it.toCache() }
                        pokemonDao.insertAllPokemon(cachedList)
                        Result.success(Unit)
                    },
                    onFailure = { exception ->
                        Result.failure(exception)
                    }
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("No hay conexión a internet"))
        }
    }

    suspend fun clearCache() {
        pokemonDao.deleteAllPokemon()
    }

    fun getFavoritePokemons(): Flow<UiState<List<Pokemon>>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(UiState.Error("User not logged in"))
            awaitClose { }
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(UiState.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val favoriteIds = snapshot.get("favorites") as? List<Long> ?: emptyList()
                    if (favoriteIds.isNotEmpty()) {
                        GlobalScope.launch {
                            val pokemons = pokemonDao.getPokemonsByIds(favoriteIds)
                            trySend(UiState.Success(pokemons.map { it.toDomain() }))
                        }
                    } else {
                        trySend(UiState.Success(emptyList()))
                    }
                } else {
                    trySend(UiState.Success(emptyList()))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addPokemonToFavorites(pokemonId: Int): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
        return try {
            firestore.collection("users").document(userId)
                .update("favorites", FieldValue.arrayUnion(pokemonId.toLong())).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removePokemonFromFavorites(pokemonId: Int): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
        return try {
            firestore.collection("users").document(userId)
                .update("favorites", FieldValue.arrayRemove(pokemonId.toLong())).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTradeRequest(offeredPokemonId: Int): Result<String> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
        return try {
            val tradeRequest = hashMapOf(
                "offeringUserId" to userId,
                "offeredPokemonId" to offeredPokemonId,
                "receivingUserId" to null,
                "receivedPokemonId" to null,
                "status" to "pending",
                "createdAt" to FieldValue.serverTimestamp()
            )
            val document = firestore.collection("trades").add(tradeRequest).await()
            Result.success(document.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenForTradeUpdates(tradeId: String): Flow<UiState<Map<String, Any>>> = callbackFlow {
        val listener = firestore.collection("trades").document(tradeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(UiState.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    trySend(UiState.Success(snapshot.data ?: emptyMap()))
                } else {
                    trySend(UiState.Error("Trade not found"))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun acceptTrade(tradeId: String, offeredPokemonId: Int): Result<Unit> {
        val receivingUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

        return try {
            firestore.runTransaction { transaction ->
                val tradeRef = firestore.collection("trades").document(tradeId)
                val tradeSnapshot = transaction.get(tradeRef)

                val offeringUserId = tradeSnapshot.getString("offeringUserId")
                val offeredPokemonIdFromDb = tradeSnapshot.getLong("offeredPokemonId")?.toInt()

                if (offeringUserId == null || offeredPokemonIdFromDb == null) {
                    throw Exception("Invalid trade data")
                }

                // Update trade status
                transaction.update(tradeRef, "receivingUserId", receivingUserId)
                transaction.update(tradeRef, "receivedPokemonId", offeredPokemonId)
                transaction.update(tradeRef, "status", "completed")

                // Update favorites for both users
                val offeringUserRef = firestore.collection("users").document(offeringUserId)
                transaction.update(offeringUserRef, "favorites", FieldValue.arrayRemove(offeredPokemonIdFromDb.toLong()))
                transaction.update(offeringUserRef, "favorites", FieldValue.arrayUnion(offeredPokemonId.toLong()))

                val receivingUserRef = firestore.collection("users").document(receivingUserId)
                transaction.update(receivingUserRef, "favorites", FieldValue.arrayRemove(offeredPokemonId.toLong()))
                transaction.update(receivingUserRef, "favorites", FieldValue.arrayUnion(offeredPokemonIdFromDb.toLong()))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    data object Empty : UiState<Nothing>()
}