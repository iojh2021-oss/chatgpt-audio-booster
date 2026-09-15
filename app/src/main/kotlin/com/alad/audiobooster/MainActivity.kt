package com.alad.audiobooster

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private var boosterService: BoosterService? = null
    private var bound by mutableStateOf(false)

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            boosterService = (service as BoosterService.LocalBinder).getService()
            bound = true
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            boosterService = null
            bound = false
        }
    }

    private val notifPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            BoosterScreen(
                bound = bound,
                service = boosterService,
                onToggle = { turnOn -> toggleBooster(turnOn) }
            )
        }
    }

    private fun toggleBooster(turnOn: Boolean) {
        val intent = Intent(this, BoosterService::class.java)
        if (turnOn) {
            startForegroundService(intent)
            bindService(intent, connection, Context.BIND_AUTO_CREATE)

            val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audio.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                AudioManager.FLAG_SHOW_UI
            )
            audio.setStreamVolume(
                AudioManager.STREAM_VOICE_CALL,
                audio.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                0
            )
        } else {
            if (bound) {
                unbindService(connection)
                bound = false
                boosterService = null
            }
            stopService(intent)
        }
    }

    override fun onDestroy() {
        if (bound) unbindService(connection)
        super.onDestroy()
    }
}

@Composable
private fun BoosterScreen(
    bound: Boolean,
    service: BoosterService?,
    onToggle: (Boolean) -> Unit
) {
    var enabled by remember { mutableStateOf(false) }
    var boost by remember { mutableFloatStateOf(0.3f) }

    LaunchedEffect(bound, boost, enabled) {
        if (enabled && bound) {
            service?.setGain((boost * 3000).toInt())
        }
    }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("ChatGPT Audio Booster", style = MaterialTheme.typography.headlineSmall)
        Text("Keep ChatGPT Voice in Phone mode and boost the loudest safe system playback path.")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                enabled = !enabled
                onToggle(enabled)
            }) { Text(if (enabled) "Booster ON" else "Booster OFF") }
        }
        Text("Volume boost: ${(boost * 100).toInt()}%")
        Slider(value = boost, onValueChange = { boost = it }, valueRange = 0f..1f, enabled = enabled)
        if (enabled && !bound) {
            Text("Starting booster service...")
        }
        Text("Note: applies a system-wide loudness enhancement to all audio output (including ChatGPT Voice) plus maxes call/media volume. Does not record or capture app-private audio.")
    }
}
