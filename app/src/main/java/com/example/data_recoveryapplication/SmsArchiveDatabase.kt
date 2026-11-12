package com.example.data_recoveryapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

class SmsArchiveDatabase(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    "sms_recovery_archive.db",
    null,
    3
) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE archived_sms (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sender TEXT NOT NULL,
                message TEXT NOT NULL,
                dateTime TEXT NOT NULL,
                type TEXT NOT NULL,
                fingerprint TEXT NOT NULL UNIQUE,
                firstSeen INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {

        if (oldVersion < 3) {

            db.execSQL(
                """
                CREATE TABLE archived_sms_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sender TEXT NOT NULL,
                    message TEXT NOT NULL,
                    dateTime TEXT NOT NULL,
                    type TEXT NOT NULL,
                    fingerprint TEXT NOT NULL UNIQUE,
                    firstSeen INTEGER NOT NULL
                )
                """.trimIndent()
            )

            val cursor = db.query(
                "archived_sms",
                arrayOf(
                    "sender",
                    "message",
                    "dateTime",
                    "type",
                    "firstSeen"
                ),
                null,
                null,
                null,
                null,
                null
            )

            cursor.use {

                while (it.moveToNext()) {

                    val sender = it.getString(0)
                    val message = it.getString(1)
                    val dateTime = it.getString(2)
                    val type = it.getString(3)
                    val firstSeen = it.getLong(4)

                    val fingerprint =
                        createFingerprint(
                            sender,
                            message,
                            dateTime,
                            type
                        )

                    val values = ContentValues().apply {

                        put("sender", sender)
                        put("message", message)
                        put("dateTime", dateTime)
                        put("type", type)
                        put("fingerprint", fingerprint)
                        put("firstSeen", firstSeen)
                    }

                    db.insertWithOnConflict(
                        "archived_sms_new",
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_IGNORE
                    )
                }
            }

            db.execSQL(
                "DROP TABLE archived_sms"
            )

            db.execSQL(
                """
                ALTER TABLE archived_sms_new
                RENAME TO archived_sms
                """.trimIndent()
            )
        }
    }

    /*
     * Archive one SMS.
     */
    fun archiveSms(
        sms: SmsRecoveryResult
    ) {

        if (
            sms.type != "Inbox" &&
            sms.type != "Sent"
        ) {
            return
        }

        val fingerprint =
            createFingerprint(
                sms.sender,
                sms.message,
                sms.dateTime,
                sms.type
            )

        val values =
            ContentValues().apply {

                put("sender", sms.sender)
                put("message", sms.message)
                put("dateTime", sms.dateTime)
                put("type", sms.type)
                put("fingerprint", fingerprint)
                put(
                    "firstSeen",
                    System.currentTimeMillis()
                )
            }

        writableDatabase.insertWithOnConflict(
            "archived_sms",
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        )
    }

    /*
     * Archive all currently available Inbox + Sent SMS.
     */
    fun archiveAll(
        messages: List<SmsRecoveryResult>
    ) {

        writableDatabase.beginTransaction()

        try {

            messages.forEach { sms ->
                archiveSms(sms)
            }

            writableDatabase.setTransactionSuccessful()

        } finally {

            writableDatabase.endTransaction()
        }
    }

    /*
     * Return all archived SMS.
     */
    fun getAllArchivedSms():
            List<SmsRecoveryResult> {

        val results =
            mutableListOf<SmsRecoveryResult>()

        val cursor =
            readableDatabase.query(
                "archived_sms",

                arrayOf(
                    "sender",
                    "message",
                    "dateTime",
                    "type"
                ),

                null,
                null,
                null,
                null,

                "id DESC"
            )

        cursor.use {

            while (it.moveToNext()) {

                results.add(
                    SmsRecoveryResult(

                        sender =
                            it.getString(0),

                        message =
                            it.getString(1),

                        dateTime =
                            it.getString(2),

                        type =
                            it.getString(3),

                        recoveryStatus =
                            "Archived",

                        confidence =
                            "Stored Evidence",

                        source =
                            SmsRecoverySource.LIVE_SMS_PROVIDER
                    )
                )
            }
        }

        return results
    }

    /*
     * Find SMS that existed in the archive
     * but no longer exist in the current SMS provider.
     */
    fun findDeletedSms(
        currentMessages:
        List<SmsRecoveryResult>
    ): List<SmsRecoveryResult> {

        /*
         * Get all SMS that were previously archived.
         */
        val archivedMessages =
            getAllArchivedSms()

        /*
         * Create fingerprints for SMS that
         * currently exist on the phone.
         */
        val currentFingerprints =
            currentMessages
                .filter { sms ->

                    sms.type == "Inbox" ||
                            sms.type == "Sent"
                }
                .map { sms ->

                    createFingerprint(
                        sms.sender,
                        sms.message,
                        sms.dateTime,
                        sms.type
                    )
                }
                .toSet()

        /*
         * Anything archived but missing from
         * the current provider is considered
         * a deleted candidate.
         */
        return archivedMessages
            .filter { archived ->

                val fingerprint =
                    createFingerprint(
                        archived.sender,
                        archived.message,
                        archived.dateTime,
                        archived.type
                    )

                fingerprint !in currentFingerprints
            }
            .map { deleted ->

                deleted.copy(

                    recoveryStatus =
                        "Deleted / Recovered",

                    confidence =
                        "Stored Evidence"
                )
            }
    }

    /*
     * Create SHA-256 fingerprint.
     */
    private fun createFingerprint(
        sender: String,
        message: String,
        dateTime: String,
        type: String
    ): String {

        val raw =
            buildString {

                append(sender.trim())
                append("|")

                append(message.trim())
                append("|")

                append(dateTime.trim())
                append("|")

                append(type.trim())
            }

        val digest =
            MessageDigest.getInstance(
                "SHA-256"
            )

        val hash =
            digest.digest(
                raw.toByteArray(
                    Charsets.UTF_8
                )
            )

        return hash.joinToString("") { byte ->

            "%02x".format(byte)
        }
    }
}