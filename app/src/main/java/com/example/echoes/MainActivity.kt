package com.example.echoes.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.echoes.AudioManager
import com.example.echoes.GameEngine
import com.example.echoes.ui.GameScreen

class MainActivity : ComponentActivity() {
    private lateinit var audio: AudioManager
    private lateinit var engine: GameEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audio = AudioManager(this)
        engine = GameEngine(audio, this)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = androidx.compose.ui.graphics.Color(0xFF0a0a1a)) {
                    GameScreen(engine)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::audio.isInitialized) audio.release()
        if (::engine.isInitialized) engine.stop()
    }
}