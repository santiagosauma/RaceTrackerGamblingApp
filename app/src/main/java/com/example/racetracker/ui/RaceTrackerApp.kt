package com.example.racetracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.racetracker.R
import com.example.racetracker.ui.theme.RaceTrackerTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun RaceTrackerApp() {
    val playerOne = remember {
        RaceParticipant(
            name = "N°1",
            progressIncrement = 1,
            progressDelayMillis = Random.nextLong(300, 700)
        )
    }
    val playerTwo = remember {
        RaceParticipant(
            name = "N°2",
            progressIncrement = 1,
            progressDelayMillis = Random.nextLong(300, 700)
        )
    }
    val playerThree = remember {
        RaceParticipant(
            name = "N°3",
            progressIncrement = 1,
            progressDelayMillis = Random.nextLong(300, 700)
        )
    }

    var raceInProgress by remember { mutableStateOf(false) }
    var selectedRunner by remember { mutableStateOf<String?>(null) }
    var winner by remember { mutableStateOf<String?>(null) }
    var hasWon by remember { mutableStateOf(false) }
    var jobPlayerOne: Job? by remember { mutableStateOf(null) }
    var jobPlayerTwo: Job? by remember { mutableStateOf(null) }
    var jobPlayerThree: Job? by remember { mutableStateOf(null) }
    var remainingLives by remember { mutableStateOf(3) }
    var score by remember { mutableStateOf(150) }
    var raceFinished by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    fun resetGame() {
        jobPlayerOne?.cancel()
        jobPlayerTwo?.cancel()
        jobPlayerThree?.cancel()

        playerOne.reset()
        playerTwo.reset()
        playerThree.reset()

        winner = null
        raceFinished = false
        raceInProgress = false
    }

    fun checkWinner() {
        if (winner == null) {
            winner = when {
                playerOne.currentProgress >= playerOne.maxProgress -> playerOne.name
                playerTwo.currentProgress >= playerTwo.maxProgress -> playerTwo.name
                playerThree.currentProgress >= playerThree.maxProgress -> playerThree.name
                else -> null
            }

            if (winner != null) {
                raceInProgress = false
                raceFinished = true
                hasWon = (winner == selectedRunner)
                if (hasWon) {
                    score += 100
                } else if (remainingLives > 0) {
                    remainingLives -= 1
                }
            }
        }
    }

    val startRace: () -> Unit = {
        if (selectedRunner != null && !raceInProgress) {
            winner = null
            raceInProgress = true
            raceFinished = false
            jobPlayerOne = coroutineScope.launch {
                playerOne.run()
                checkWinner()
            }
            jobPlayerTwo = coroutineScope.launch {
                playerTwo.run()
                checkWinner()
            }
            jobPlayerThree = coroutineScope.launch {
                playerThree.run()
                checkWinner()
            }
        }
    }

    val pauseRace: () -> Unit = {
        raceInProgress = false
        jobPlayerOne?.cancel()
        jobPlayerTwo?.cancel()
        jobPlayerThree?.cancel()
    }

    RaceTrackerScreen(
        playerOne = playerOne,
        playerTwo = playerTwo,
        playerThree = playerThree,
        isRunning = raceInProgress,
        onRunStateChange = { isRunning ->
            if (isRunning) startRace() else pauseRace()
        },
        selectedRunner = selectedRunner,
        onRunnerSelected = { if (!raceInProgress && !raceFinished) selectedRunner = it },
        hasWon = hasWon,
        winner = winner,
        remainingLives = remainingLives,
        score = score,
        raceFinished = raceFinished,
        onRestart = {
            resetGame()
        },
        onContinueWithPoints = {
            if (score >= 150) {
                score -= 150
                remainingLives = 3
                resetGame()
            }
        },
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimensionResource(R.dimen.padding_medium)),
    )
}

@Composable
private fun RaceTrackerScreen(
    playerOne: RaceParticipant,
    playerTwo: RaceParticipant,
    playerThree: RaceParticipant,
    isRunning: Boolean,
    onRunStateChange: (Boolean) -> Unit,
    selectedRunner: String?,
    onRunnerSelected: (String) -> Unit,
    hasWon: Boolean,
    winner: String?,
    remainingLives: Int,
    score: Int,
    raceFinished: Boolean,
    onRestart: () -> Unit,
    onContinueWithPoints: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Score: $score",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(dimensionResource(R.dimen.padding_medium))
        )

        Text(
            text = "$remainingLives ❤️",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(dimensionResource(R.dimen.padding_medium))
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (remainingLives == 0) {
            Text(
                text = "¡Perdiste Todo!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )

            Button(
                onClick = onContinueWithPoints,
                modifier = Modifier
                    .padding(top = dimensionResource(R.dimen.padding_medium))
                    .fillMaxWidth(),
                enabled = score >= 150
            ) {
                Text("Reiniciar (-150 pts)")
            }
        } else if (winner != null) {
            Text(
                text = if (hasWon) "¡Ganaste!" else "Perdiste",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(text = "Elige tu corredor", style = MaterialTheme.typography.headlineSmall)

        Row(
            modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_medium)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
        ) {
            listOf(playerOne.name, playerTwo.name, playerThree.name).forEach { runnerName ->
                Button(
                    onClick = { onRunnerSelected(runnerName) },
                    enabled = !isRunning && remainingLives > 0 && !raceFinished,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (runnerName == selectedRunner) Color(0xFFFFA500) else Color.White
                    )
                ) {
                    Text(runnerName, color = if (runnerName == selectedRunner) Color.White else Color.Black)
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StatusIndicator(playerOne)
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_large)))
            StatusIndicator(playerTwo)
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_large)))
            StatusIndicator(playerThree)
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))

        if (!isRunning && selectedRunner != null && winner == null) {
            RaceControls(
                isRunning = isRunning,
                onRunStateChange = onRunStateChange,
                modifier = Modifier.fillMaxWidth(),
                startEnabled = selectedRunner != null && remainingLives > 0
            )
        }

        if (raceFinished && remainingLives > 0) {
            Button(
                onClick = onRestart,
                modifier = Modifier
                    .padding(top = dimensionResource(R.dimen.padding_medium))
                    .fillMaxWidth(),
            ) {
                Text("Reiniciar")
            }
        }
    }
}

@Composable
private fun StatusIndicator(participant: RaceParticipant) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = participant.name)
        LinearProgressIndicator(
            progress = participant.progressFactor,
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.progress_indicator_height))
                .clip(RoundedCornerShape(dimensionResource(R.dimen.progress_indicator_corner_radius)))
        )
    }
}

@Composable
private fun RaceControls(
    onRunStateChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isRunning: Boolean,
    startEnabled: Boolean
) {
    Button(
        onClick = { onRunStateChange(!isRunning) },
        modifier = modifier,
        enabled = startEnabled
    ) {
        Text(if (isRunning) "Pausar" else "Iniciar")
    }
}

@Preview(showBackground = true)
@Composable
fun RaceTrackerAppPreview() {
    RaceTrackerTheme {
        RaceTrackerApp()
    }
}
