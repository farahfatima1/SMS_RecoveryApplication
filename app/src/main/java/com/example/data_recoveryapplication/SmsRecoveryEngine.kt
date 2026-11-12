package com.example.data_recoveryapplication

import android.content.ContentResolver
import android.provider.Telephony
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmsRecoveryEngine(
    private val contentResolver: ContentResolver
) {

    fun scanAvailableSms(): List<SmsRecoveryResult> {

        val results = mutableListOf<SmsRecoveryResult>()

        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE
        )

        contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC"
        )?.use { cursor ->

            val addressIndex =
                cursor.getColumnIndex(Telephony.Sms.ADDRESS)

            val bodyIndex =
                cursor.getColumnIndex(Telephony.Sms.BODY)

            val dateIndex =
                cursor.getColumnIndex(Telephony.Sms.DATE)

            val typeIndex =
                cursor.getColumnIndex(Telephony.Sms.TYPE)

            while (cursor.moveToNext()) {

                val sender =
                    if (addressIndex >= 0) {
                        cursor.getString(addressIndex) ?: "Unknown"
                    } else {
                        "Unknown"
                    }

                val message =
                    if (bodyIndex >= 0) {
                        cursor.getString(bodyIndex) ?: ""
                    } else {
                        ""
                    }

                val timestamp =
                    if (dateIndex >= 0) {
                        cursor.getLong(dateIndex)
                    } else {
                        0L
                    }

                val smsType =
                    if (typeIndex >= 0) {
                        cursor.getInt(typeIndex)
                    } else {
                        Telephony.Sms.MESSAGE_TYPE_INBOX
                    }

                val dateTime =
                    if (timestamp > 0) {
                        SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.getDefault()
                        ).format(Date(timestamp))
                    } else {
                        "Unknown date"
                    }

                val type =
                    when (smsType) {

                        Telephony.Sms.MESSAGE_TYPE_INBOX ->
                            "Inbox"

                        Telephony.Sms.MESSAGE_TYPE_SENT ->
                            "Sent"

                        Telephony.Sms.MESSAGE_TYPE_DRAFT ->
                            "Draft"

                        Telephony.Sms.MESSAGE_TYPE_OUTBOX ->
                            "Outbox"

                        Telephony.Sms.MESSAGE_TYPE_FAILED ->
                            "Failed"

                        Telephony.Sms.MESSAGE_TYPE_QUEUED ->
                            "Queued"

                        else ->
                            "Unknown"
                    }

                results.add(
                    SmsRecoveryResult(
                        sender = sender,
                        message = message,
                        dateTime = dateTime,
                        type = type,
                        recoveryStatus = "Currently Available",
                        confidence = "Verified",
                        source = SmsRecoverySource.LIVE_SMS_PROVIDER
                    )
                )
            }
        }

        return results
    }
}
