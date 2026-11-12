package com.example.data_recoveryapplication

import android.content.Context
import android.net.Uri
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmsDatabaseParser(
    private val context: Context
) {

    fun parseDatabase(uri: Uri): List<SmsRecoveryResult> {

        val results = mutableListOf<SmsRecoveryResult>()

        val databaseFile = copyUriToCache(uri) ?: return results

        return try {

            val database = SQLiteDatabase.openDatabase(
                databaseFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )

            database.use { db ->

                val cursor = db.rawQuery(
                    """
                    SELECT address, body, date, type
                    FROM sms
                    ORDER BY date DESC
                    """.trimIndent(),
                    null
                )

                cursor.use {

                    val addressIndex = it.getColumnIndex("address")
                    val bodyIndex = it.getColumnIndex("body")
                    val dateIndex = it.getColumnIndex("date")
                    val typeIndex = it.getColumnIndex("type")

                    while (it.moveToNext()) {

                        val sender =
                            if (addressIndex >= 0)
                                it.getString(addressIndex) ?: "Unknown"
                            else
                                "Unknown"

                        val message =
                            if (bodyIndex >= 0)
                                it.getString(bodyIndex) ?: ""
                            else
                                ""

                        val timestamp =
                            if (dateIndex >= 0)
                                it.getLong(dateIndex)
                            else
                                0L

                        val smsType =
                            if (typeIndex >= 0)
                                it.getInt(typeIndex)
                            else
                                0

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
                                1 -> "Inbox"
                                2 -> "Sent"
                                3 -> "Draft"
                                4 -> "Outbox"
                                5 -> "Failed"
                                6 -> "Queued"
                                else -> "Unknown"
                            }

                        if (sender.isNotBlank() && message.isNotBlank()) {

                            results.add(
                                SmsRecoveryResult(
                                    sender = sender,
                                    message = message,
                                    dateTime = dateTime,
                                    type = type,
                                    recoveryStatus = "Database Record",
                                    confidence = "High",
                                    source = SmsRecoverySource.FORENSIC_DATABASE
                                )
                            )
                        }
                    }
                }
            }

            results

        } catch (exception: Exception) {

            emptyList()
        }
    }

    private fun copyUriToCache(uri: Uri): File? {

        return try {

            val outputFile = File(
                context.cacheDir,
                "sms_recovery_database.db"
            )

            context.contentResolver.openInputStream(uri)?.use { input ->

                FileOutputStream(outputFile).use { output ->

                    input.copyTo(output)
                }
            }

            outputFile

        } catch (exception: Exception) {

            null
        }
    }
}