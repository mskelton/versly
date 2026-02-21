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

interface PlanProvider {
    fun getReadingsForToday(): List<Reading>
}

class AssetPlanProvider
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : PlanProvider {
        override fun getReadingsForToday(): List<Reading> {
            val today = LocalDate.now().toString()
            val days =
                context.assets
                    .open("plan.json")
                    .bufferedReader()
                    .use { JSONObject(it.readText()) }
                    .getJSONObject("plan")
                    .getJSONArray("days")

            val day =
                (0 until days.length())
                    .map { days.getJSONObject(it) }
                    .find { it.getString("date") == today }

            val readings = day?.getJSONArray("readings") ?: return emptyList()

            return (0 until readings.length())
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
        }
    }
