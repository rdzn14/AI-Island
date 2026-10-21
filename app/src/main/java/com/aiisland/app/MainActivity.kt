package com.aiisland.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private var on by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Screen() }
    }

    override fun onResume() {
        super.onResume()
        on = IslandService.running
    }

    @Composable
    private fun Screen() {
        MaterialTheme(colorScheme = darkColorScheme()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F1319))
                    .systemBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Robot(if (on) Mood.HAPPY else Mood.SLEEPY, 96.dp)
                Spacer(Modifier.height(16.dp))
                Text("AI Island", color = Color.White, fontSize = 24.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (on) "Island: hidup" else "Island: belum dihidupkan",
                    color = Color(0xFF9AA3B2)
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }) { Text("Buka tetapan Aksesibiliti") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:$packageName")
                        )
                    )
                }) { Text("Buka maklumat app") }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Hidupkan AI Island dalam Aksesibiliti. Kalau pilihan kelabu, buka maklumat app, tekan titik tiga dan pilih Benarkan tetapan terhad.",
                    color = Color(0xFF9AA3B2),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
