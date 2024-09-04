package com.example.racetracker.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random

class RaceParticipant(
    val name: String,
    val maxProgress: Int = 100,
    private var progressDelayMillis: Long = Random.nextLong(300, 700),  // Tiempo inicial aleatorio
    private val progressIncrement: Int = 1,
    private val initialProgress: Int = 0
) {
    var currentProgress by mutableStateOf(initialProgress)
        private set

    suspend fun run() {
        try {
            while (isActive && currentProgress < maxProgress) {
                delay(progressDelayMillis)
                currentProgress += progressIncrement

                when (currentProgress) {
                    in (maxProgress * 0.15).toInt()..(maxProgress * 0.30).toInt() -> {
                        progressDelayMillis = Random.nextLong(200, 600)
                    }
                    in (maxProgress * 0.30).toInt()..(maxProgress * 0.45).toInt() -> {
                        progressDelayMillis = Random.nextLong(150, 550)
                    }
                    in (maxProgress * 0.45).toInt()..(maxProgress * 0.60).toInt() -> {
                        progressDelayMillis = Random.nextLong(150, 500)
                    }
                    in (maxProgress * 0.60).toInt()..(maxProgress * 0.75).toInt() -> {
                        progressDelayMillis = Random.nextLong(100, 450)
                    }
                    in (maxProgress * 0.75).toInt()..(maxProgress * 0.90).toInt() -> {
                        progressDelayMillis = Random.nextLong(150, 300)
                    }
                }
            }
        } catch (e: CancellationException) {
            Log.e("RaceParticipant", "$name: ${e.message}")
        }
    }

    fun reset() {
        currentProgress = 0
    }
}

val RaceParticipant.progressFactor: Float
    get() = (currentProgress / maxProgress.toFloat()).coerceIn(0f, 1f)
