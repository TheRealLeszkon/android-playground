package com.example.androidplayground.ui.demos.pokemon

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Fetched Pokémon data.
 */
data class PokemonData(
    val name: String,
    val spriteUrl: String
)

/**
 * Fetches random Pokémon data from PokeAPI.
 */
object PokemonApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Fetches a Pokémon by ID. Returns null on any failure.
     */
    suspend fun fetchPokemon(id: Int): PokemonData? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://pokeapi.co/api/v2/pokemon/$id")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)

            val name = json.getString("name")
            val sprites = json.getJSONObject("sprites")
            val spriteUrl = sprites.optString("front_default", "")

            if (spriteUrl.isBlank()) return@withContext null

            PokemonData(
                name = name.lowercase().trim(),
                spriteUrl = spriteUrl
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Returns a random Pokémon ID in the valid range.
     */
    fun randomId(): Int = (1..1010).random()
}
