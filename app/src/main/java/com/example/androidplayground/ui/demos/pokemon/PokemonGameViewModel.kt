package com.example.androidplayground.ui.demos.pokemon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PokemonGameState(
    val pokemonName: String = "",
    val spriteUrl: String = "",
    val userGuess: String = "",
    val hintLevel: Int = 0,          // 0 = no hint, 1-3 = progressive
    val hintText: String = "",
    val streak: Int = 0,
    val revealed: Boolean = false,
    val guessedCorrectly: Boolean = false,
    val errorMessage: String = "",
    val isLoading: Boolean = true,
    val networkError: Boolean = false,
    val gen1Only: Boolean = false,
    val lastGuessResult: GuessResult = GuessResult.NONE
)

enum class GuessResult { NONE, CORRECT, WRONG }

class PokemonGameViewModel : ViewModel() {

    private val _state = MutableStateFlow(PokemonGameState())
    val state: StateFlow<PokemonGameState> = _state.asStateFlow()

    // Track which letter positions have been revealed as hints
    private var revealedPositions = mutableSetOf<Int>()

    init {
        loadNewPokemon()
    }

    fun updateGuess(guess: String) {
        _state.value = _state.value.copy(userGuess = guess, errorMessage = "", lastGuessResult = GuessResult.NONE)
    }

    fun toggleGen1Only() {
        _state.value = _state.value.copy(gen1Only = !_state.value.gen1Only)
    }

    fun submitGuess() {
        val s = _state.value
        if (s.revealed || s.guessedCorrectly || s.isLoading) return

        val guess = s.userGuess.trim().lowercase()
        if (guess.isBlank()) {
            _state.value = s.copy(errorMessage = "Type a name first")
            return
        }

        if (guess == s.pokemonName) {
            // Correct!
            _state.value = s.copy(
                guessedCorrectly = true,
                streak = s.streak + 1,
                errorMessage = "",
                userGuess = "",
                lastGuessResult = GuessResult.CORRECT
            )
            // Auto-advance after delay
            viewModelScope.launch {
                delay(2000L)
                loadNewPokemon()
            }
        } else {
            _state.value = s.copy(
                errorMessage = "Wrong! Try again",
                lastGuessResult = GuessResult.WRONG
            )
        }
    }

    fun requestHint() {
        val s = _state.value
        if (s.revealed || s.guessedCorrectly || s.isLoading) return
        if (s.pokemonName.isEmpty()) return

        val name = s.pokemonName
        val nextLevel = s.hintLevel + 1

        val hintText = when (nextLevel) {
            1 -> "Starts with \"${name.first().uppercase()}\""
            2 -> "Name has ${name.length} letters"
            3 -> {
                // Reveal a random unrevealed letter (not the first)
                val available = name.indices.filter { it != 0 && it !in revealedPositions }
                if (available.isNotEmpty()) {
                    val pos = available.random()
                    revealedPositions.add(pos)
                    "Letter ${pos + 1} is \"${name[pos]}\""
                } else {
                    "No more hints available"
                }
            }
            else -> s.hintText
        }

        _state.value = s.copy(
            hintLevel = nextLevel.coerceAtMost(3),
            hintText = hintText
        )
    }

    fun reveal() {
        val s = _state.value
        if (s.guessedCorrectly || s.isLoading) return

        _state.value = s.copy(
            revealed = true,
            streak = 0,
            errorMessage = "",
            userGuess = ""
        )

        viewModelScope.launch {
            delay(2500L)
            loadNewPokemon()
        }
    }

    fun retry() {
        loadNewPokemon()
    }

    private fun loadNewPokemon() {
        revealedPositions.clear()

        _state.value = _state.value.copy(
            isLoading = true,
            networkError = false,
            revealed = false,
            guessedCorrectly = false,
            hintLevel = 0,
            hintText = "",
            errorMessage = "",
            userGuess = "",
            pokemonName = "",
            spriteUrl = ""
        )

        viewModelScope.launch {
            val gen1 = _state.value.gen1Only
            // Retry up to 3 times if sprite is null
            var data: PokemonData? = null
            repeat(3) {
                data = PokemonApi.fetchPokemon(PokemonApi.randomId(gen1))
                if (data != null) return@repeat
            }

            if (data == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    networkError = true
                )
                return@launch
            }

            _state.value = _state.value.copy(
                isLoading = false,
                pokemonName = data!!.name,
                spriteUrl = data!!.spriteUrl
            )
        }
    }
}
