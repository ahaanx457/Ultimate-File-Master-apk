package com.ultimate.filemanager.core.analytics

import android.content.Context
import org.json.JSONObject

class AnalyticsStore(
    context: Context
) {

    private val preferences =
        context.getSharedPreferences(
            "ufm_analytics",
            Context.MODE_PRIVATE
        )

    var enabled: Boolean

        get() =
            preferences.getBoolean(
                "enabled",
                false
            )

        set(value) =
            preferences
                .edit()
                .putBoolean(
                    "enabled",
                    value
                )
                .apply()

    fun record(
        event: String,
        properties: Map<String, String> =
            emptyMap()
    ) {

        if (!enabled) return

        val json =
            JSONObject().apply {

                put(
                    "event",
                    event
                )

                put(
                    "properties",
                    JSONObject(properties)
                )

                put(
                    "timestamp",
                    System.currentTimeMillis()
                )
            }

        val count =
            preferences.getLong(
                "event_count",
                0
            )

        preferences
            .edit()
            .putLong(
                "event_count",
                count + 1
            )
            .putString(
                "last_event",
                json.toString()
            )
            .apply()
    }
}
