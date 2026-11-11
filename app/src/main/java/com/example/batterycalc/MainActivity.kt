package com.example.batterycalc

import androidx.activity.compose.rememberLauncherForActivityResult
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) { BatteryScreen() }
            }
        }
    }
}

@Composable
fun BatteryScreen() {
    val context = LocalContext.current
    var state by remember { mutableStateOf(BatteryReader.read(context)) }
    var samples by remember { mutableStateOf(SampleStore.load(context)) }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        ContextCompat.startForegroundService(context, Intent(context, BatteryService::class.java))
    }

    LaunchedEffect(Unit) {
        while (true) {
            state = BatteryReader.read(context)
            samples = SampleStore.load(context)
            delay(5_000L)
        }
    }

    val estimate = Estimator.estimate(samples)

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("${state.percent}%", fontSize = 64.sp)
        Text(if (state.isCharging) "Заряжается" else "Разряжается")

        HorizontalDivider()

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Напряжение")
            Text(state.voltageMv?.let { "$it мВ" } ?: "нет данных")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Температура")
            Text(state.temperatureC?.let { "%.1f °C".format(it) } ?: "нет данных")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Ток")
            Text(state.currentMa?.let { "$it мА" } ?: "нет данных")
        }

        HorizontalDivider()

        Text("Оценка", style = MaterialTheme.typography.titleMedium)
        Text(
            estimate.minutesToFull?.let { "До полной зарядки: ${formatMinutes(it)}" }
                ?: "До полной зарядки: считаю…"
        )
        Text(
            estimate.minutesToEmpty?.let { "До разрядки: ${formatMinutes(it)}" }
                ?: "До разрядки: считаю…"
        )
        estimate.ratePercentPerHour?.let {
            Text("Скорость: %.1f %%/час".format(it))
        }
    }
}

fun formatMinutes(min: Long): String {
    val h = min / 60
    val m = min % 60
    return if (h > 0) "$h ч $m мин" else "$m мин"
}