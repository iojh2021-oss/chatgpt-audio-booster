package com.alad.audiobooster

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BoosterScreen(this) }
    }
}

@Composable
private fun BoosterScreen(context: Context) {
    val audio = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    var enabled by remember { mutableStateOf(false) }
    var boost by remember { mutableFloatStateOf(0f) }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("ChatGPT Audio Booster", style = MaterialTheme.typography.headlineSmall)
        Text("Keep ChatGPT Voice in Phone mode and test the loudest safe system playback path.")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                enabled = !enabled
                if (enabled) audio.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
            }) { Text(if (enabled) "Booster ON" else "Booster OFF") }
        }
        Text("Volume boost: ${(boost * 100).toInt()}%")
        Slider(value = boost, onValueChange = { boost = it }, valueRange = 0f..1f)
        Text("Note: Android normally does not allow one app to capture another app's private audio output. This first version uses only permitted system audio controls.")
    }
}
