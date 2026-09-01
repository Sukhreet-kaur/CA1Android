package com.example.ca1android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.ca1android.ui.theme.CA1AndroidTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CA1AndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    VideoPlayerScreen()
                }
            }
        }
    }
}

@Composable
fun VideoPlayerScreen() {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(value = true) }
    var isMuted by remember { mutableStateOf(value = false) }
    var showControls by remember { mutableStateOf(value = true) }

    val scope = rememberCoroutineScope()
    var hideControlsJob by remember { mutableStateOf<Job?>(null) }

    fun scheduleHideControls() {
        hideControlsJob?.cancel()
        showControls = true
        hideControlsJob = scope.launch {
            delay(3000L)
            showControls = false
        }
    }
    val videoUri =
        "android.resource://${context.packageName}/${R.raw.my_video}"
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            val videoUri =
                "android.resource://${context.packageName}/${R.raw.my_video}"
            val mediaItem = MediaItem.fromUri(videoUri)
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Video Player",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text("Is Playing $isPlaying")
            Text("Is MUted $isMuted")
            Spacer(modifier = Modifier.height(24.dp))

            // Centered Video Container with rounded corners & elevation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clickable {
                        scheduleHideControls()
                    },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            useController = false
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Control Buttons managed via CoroutineScope launch
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                if (isPlaying) {
                                    player.pause()
                                } else {
                                    player.play()
                                }
                                isPlaying = !isPlaying
                                scheduleHideControls()
                            }
                        }
                    ) {
                        Text(if (isPlaying) "Pause" else "Play")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                if (isMuted) {
                                    player.volume = 1.0f
                                } else {
                                    player.volume = 0.0f
                                }
                                isMuted = !isMuted
                                scheduleHideControls()
                            }
                        }
                    ) {
                        Text(if (isMuted) "Unmute" else "Mute")
                    }
                }
            }
        }
    }
}
