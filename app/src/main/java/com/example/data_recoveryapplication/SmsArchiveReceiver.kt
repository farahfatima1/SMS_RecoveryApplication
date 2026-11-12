
package com.example.data_recoveryapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmsArchiveReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val messages =
            Telephony.Sms.Intents.getMessagesFromIntent(intent)

        if (messages.isEmpty()) {
            return
        }

        val archiveDatabase =
            SmsArchiveDatabase(context.applicationContext)

        try {

            for (smsMessage in messages) {

                val sender =
                    smsMessage.displayOriginatingAddress
                        ?: "Unknown"

                val message =
                    smsMessage.displayMessageBody
                        ?: ""

                val timestamp =
                    smsMessage.timestampMillis

                val dateTime =
                    if (timestamp > 0L) {

                        SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.getDefault()
                        ).format(
                            Date(timestamp)
                        )

                    } else {

                        "Unknown date"
                    }

                val sms =
                    SmsRecoveryResult(
                        sender = sender,
                        message = message,
                        dateTime = dateTime,
                        type = "Inbox",
                        recoveryStatus = "Automatically Archived",
                        confidence = "Archived Evidence",
                        source = SmsRecoverySource.LIVE_SMS_PROVIDER
                    )

                archiveDatabase.archiveSms(sms)
            }

        } finally {

            archiveDatabase.close()
        }
    }
}