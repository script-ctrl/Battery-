package com.example.batterycalc

data class Estimate(
    val minutesToFull: Long?,
    val minutesToEmpty: Long?,
    val ratePercentPerHour: Double?
)

object Estimator {
    private const val WINDOW_MS = 10 * 60_000L

    fun estimate(samples: List<BatteryState>): Estimate {
        if (samples.size < 2) return Estimate(null, null, null)
        val last = samples.last()
        val windowStart = last.timestampMs - WINDOW_MS
        val window = samples.filter { it.timestampMs >= windowStart }
        val first = window.firstOrNull() ?: return Estimate(null, null, null)
        val dtMs = last.timestampMs - first.timestampMs
        if (dtMs < 60_000L || first.percent == last.percent) {
            return Estimate(null, null, null)
        }
        val ratePerHour = (last.percent - first.percent).toDouble() * 3_600_000.0 / dtMs
        return if (ratePerHour > 0) {
            val toFull = if (last.percent < 100) ((100 - last.percent) / ratePerHour * 60).toLong() else 0L
            Estimate(toFull, null, ratePerHour)
        } else {
            val toEmpty = (last.percent / -ratePerHour * 60).toLong()
            Estimate(null, toEmpty, ratePerHour)
        }
    }
}