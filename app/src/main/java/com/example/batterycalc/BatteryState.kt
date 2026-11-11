package com.example.batterycalc

data class BatteryState(
    val percent: Int = 0,
    val isCharging: Boolean = false,
    val voltageMv: Int? = null,
    val temperatureC: Float? = null,
    val currentMa: Int? = null,
    val timestampMs: Long = 0L
)