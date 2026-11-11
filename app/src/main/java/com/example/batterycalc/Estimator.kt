package com.example.batterycalc

data class Estimate(
    val minutesToFull: Long?,
    val minutesToEmpty: Long?,
    val ratePercentPerHour: Double?
)

class Estimator(private val windowMs: Long = 5 * 60_000L) {

    private val samples = ArrayDeque<BatteryState>()

    fun add(state: BatteryState): Estimate {
        samples.addLast(state)
        while (samples.size > 2 && state.timestampMs - samples.first().timestampMs > windowMs) {
            samples.removeFirst()
        }

        val first = samples.first()
        val dtMs = state.timestampMs - first.timestampMs
        if (dtMs < 60_000L || first.percent == state.percent) {
            return Estimate(null, null, null)
        }

        val ratePerHour = (state.percent - first.percent).toDouble() * 3_600_000.0 / dtMs

        return if (ratePerHour > 0) {
            val toFull = if (state.percent < 100) ((100 - state.percent) / ratePerHour * 60).toLong() else 0L
            Estimate(toFull, null, ratePerHour)
        } else {
            val toEmpty = (state.percent / -ratePerHour * 60).toLong()
            Estimate(null, toEmpty, ratePerHour)
        }
    }
}