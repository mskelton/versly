package dev.mskelton.versly.testutil

import dev.mskelton.versly.persistence.PlanDay
import dev.mskelton.versly.persistence.PlanProvider
import dev.mskelton.versly.persistence.Reading

class TestPlanProvider(
    private val readings: List<Reading> = emptyList(),
    private val dayNumber: Int = 1,
) : PlanProvider {
    override fun getCurrentPlanDay(): PlanDay? = if (readings.isEmpty()) null else PlanDay(dayNumber = dayNumber, readings = readings)
}
