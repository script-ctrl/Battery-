package com.example.batterycalc

import android.content.Context

object SampleStore {
    private const val PREFS = "battery_samples"
    private const val KEY = "samples"
    private const val MAX_SAMPLES = 120

    fun load(context: Context): List<BatteryState> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return emptyList()
        return raw.split(";").mapNotNull { line ->
            val p = line.split(",")
            if (p.size < 2) return@mapNotNull null
            BatteryState(
                percent = p[0].toIntOrNull() ?: return@mapNotNull null,
                isCharging = p[1] == "1",
                timestampMs = p.getOrNull(2)?.toLongOrNull() ?: 0L
            )
        }
    }

    fun append(context: Context, state: BatteryState) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val current = load(context).toMutableList()
        current.add(state)
        val trimmed = current.takeLast(MAX_SAMPLES)
        val raw = trimmed.joinToString(";") {
            "${it.percent},${if (it.isCharging) 1 else 0},${it.timestampMs}"
        }
        prefs.edit().putString(KEY, raw).apply()
    }
}