package com.example.androidplayground.ui.hapticlab

/**
 * Represents a saved custom waveform vibration pattern.
 */
data class SavedVibration(
    val name: String,
    val timings: LongArray,
    val amplitudes: IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SavedVibration) return false
        return name == other.name &&
                timings.contentEquals(other.timings) &&
                amplitudes.contentEquals(other.amplitudes)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + timings.contentHashCode()
        result = 31 * result + amplitudes.contentHashCode()
        return result
    }
}
