package com.example.batterycalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val estimator = remember { Estimator() }
    var state by remember { mutableStateOf(BatteryReader.read(context)) }
    var estimate by remember { mutableStateOf(Estimate(null, null, null)) }

    LaunchedEffect(Unit) {
        while (true) {
            val s = BatteryReader.read(context)
            state = s
            estimate = estimator.add(s)
            delay(5_000L)
        }
    }

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