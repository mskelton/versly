package dev.mskelton.versly.testutil

import dev.mskelton.versly.persistence.PlanProvider
import dev.mskelton.versly.persistence.Reading

class TestPlanProvider(
    private val readings: List<Reading> = emptyList(),
) : PlanProvider {
    override fun getReadingsForToday(): List<Reading> = readings
}
