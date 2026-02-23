package dev.mskelton.versly.persistence

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.time.LocalDate
import javax.inject.Inject

data class Reading(
    val book: String,
    val chapter: String,
    val range: List<String>?,
)

data class PlanDay(
    val dayNumber: Int,
    val readings: List<Reading>,
)

interface PlanProvider {
    fun getCurrentPlanDay(): PlanDay?
}

class AssetPlanProvider
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : PlanProvider {
        override fun getCurrentPlanDay(): PlanDay? {
            val today = LocalDate.now().toString()
            val days =
                context.assets
                    .open("plan.json")
                    .bufferedReader()
                    .use { JSONObject(it.readText()) }
                    .getJSONObject("plan")
                    .getJSONArray("days")

            val dayList = (0 until days.length()).map { days.getJSONObject(it) }
            val dayIndex = dayList.indexOfFirst { it.getString("date") == today }

            if (dayIndex == -1) return null

            val day = dayList[dayIndex]
            val readings = day.getJSONArray("readings")

            val readingList =
                (0 until readings.length())
                    .map { readings.getJSONObject(it) }
                    .map {
                        val rangeObj = it.optJSONObject("range")
                        val range =
                            if (rangeObj != null) {
                                val start = rangeObj.getInt("start")
                                val end = rangeObj.getInt("end")
                                listOf(start.toString(), end.toString())
                            } else {
                                null
                            }

                        Reading(
                            book = it.getString("book"),
                            chapter = it.getString("chapter"),
                            range = range,
                        )
                    }

            return PlanDay(dayNumber = dayIndex + 1, readings = readingList)
        }
    }
