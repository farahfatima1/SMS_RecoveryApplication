package com.example.data_recoveryapplication

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class RecoveredSmsStore(context: Context) {

    private val preferences = context.getSharedPreferences(
        "recovered_sms_store",
        Context.MODE_PRIVATE
    )

    fun loadRecoveredMessages(): List<SmsRecoveryResult> {

        val data = preferences.getString(
            "recovered_messages",
            null
        ) ?: return emptyList()

        val jsonArray = JSONArray(data)
        val results = mutableListOf<SmsRecoveryResult>()

        for (i in 0 until jsonArray.length()) {

            val item = jsonArray.getJSONObject(i)

            results.add(
                SmsRecoveryResult(
                    sender = item.optString("sender"),
                    message = item.optString("message"),
                    dateTime = item.optString("dateTime"),
                    type = item.optString("type"),
                    recoveryStatus = "Recovered Deleted SMS",
                    confidence = item.optString(
                        "confidence",
                        "Snapshot Evidence"
                    ),
                    source = SmsRecoverySource.LIVE_SMS_PROVIDER
                )
            )
        }

        return results
    }

    fun addRecoveredMessages(
        newMessages: List<SmsRecoveryResult>
    ) {

        val existing = loadRecoveredMessages().toMutableList()

        for (newMessage in newMessages) {

            val alreadyExists = existing.any { oldMessage ->

                oldMessage.sender == newMessage.sender &&
                        oldMessage.message == newMessage.message &&
                        oldMessage.dateTime == newMessage.dateTime
            }

            if (!alreadyExists) {

                existing.add(
                    newMessage.copy(
                        recoveryStatus = "Recovered Deleted SMS",
                        confidence = "Snapshot Evidence"
                    )
                )
            }
        }

        val jsonArray = JSONArray()

        existing.forEach { sms ->

            jsonArray.put(
                JSONObject().apply {
                    put("sender", sms.sender)
                    put("message", sms.message)
                    put("dateTime", sms.dateTime)
                    put("type", sms.type)
                    put("confidence", sms.confidence)
                }
            )
        }

        preferences.edit()
            .putString(
                "recovered_messages",
                jsonArray.toString()
            )
            .apply()
    }
}